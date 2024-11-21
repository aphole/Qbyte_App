package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private CardView userInfo, activityLogs;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference lockStatusRef;
    private DatabaseReference activityLogsRef;
    private SwitchCompat switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lockStatusRef = FirebaseDatabase.getInstance().getReference("LockStatus");  // Reference for lock status
        activityLogsRef = FirebaseDatabase.getInstance().getReference("activityLogs");  // Reference for activity logs
        activityLogs = findViewById(R.id.module_activity_log); // activity logs module
        userInfo = findViewById(R.id.module_user_info); // user info module
        switchButton = findViewById(R.id.switchButton);

        // Listen for switch button state changes
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Fetch user details and update lock status only if triggered by the app
            updateLockStatus(isChecked);
        });

        // Navigate to UserAccountActivity when clicking user info card
        userInfo.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, UserAccountActivity.class));
        });

        // Navigate to ActivityLogsActivity when clicking activity logs card
        activityLogs.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, ActivityLogsActivity.class));
        });
    }

    private void updateLockStatus(boolean isChecked) {
        int lockStatusValue = isChecked ? 1 : 0;  // 1 for unlocked, 0 for locked
        String action = isChecked ? "Unlocked" : "Locked";  // Action text

        // Get current user ID
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Fetch the fullName from Firestore
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null) {
                            // Update lock status in Firebase
                            lockStatusRef.setValue(lockStatusValue);

                            // Log the action with full name and timestamp only for the first user who toggles
                            logAction(action, currentUserId, fullName);
                        } else {
                            Toast.makeText(UserDashboardActivity.this, "Full name not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserDashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Error fetching full name", Toast.LENGTH_SHORT).show());
    }

    private void logAction(String action, String userId, String fullName) {
        // Get the current date and time
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a unique log key based on the action and userId
        String logKey = userId + "_" + action + "_" + timestamp;

        // Check if the log entry already exists for this action and userId
        activityLogsRef.child(logKey).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                // Log entry doesn't exist, add it

                Map<String, Object> logEntry = new HashMap<>();
                logEntry.put("action", action);           // "Locked" or "Unlocked"
                logEntry.put("userId", userId);           // User ID of the person who triggered the action
                logEntry.put("fullName", fullName);       // Full name of the user
                logEntry.put("timestamp", timestamp);     // Date and time of the action

                // Push a new entry to `activityLogs` to record the action
                activityLogsRef.child(logKey).setValue(logEntry)
                        .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Failed to log action.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
