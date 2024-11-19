package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class UserDashboardActivity extends AppCompatActivity {

    private CardView userInfo, activityLogs;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference lockStatusRef;
    private DatabaseReference activityLogsRef;
    private SwitchCompat switchButton;
    private String fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lockStatusRef = FirebaseDatabase.getInstance().getReference("LockStatus");  // Reference for lock status
        activityLogsRef = FirebaseDatabase.getInstance().getReference("activityLogs");  // Reference for activity logs

        // Link UI components
        activityLogs = findViewById(R.id.module_activity_log); // activity logs module
        userInfo = findViewById(R.id.module_user_info); // user info module
        switchButton = findViewById(R.id.switchButton);

        // Fetch user's full name from Firestore
        fetchUserFullName();

        // Setup real-time listener for lock status
        setupLockStatusListener();

        // Listen for switch button state changes and update lock status
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateLockStatus(isChecked));

        // Navigate to UserAccountActivity when clicking user info card
        userInfo.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, UserAccountActivity.class));
        });

        // Navigate to ActivityLogsActivity when clicking activity logs card
        activityLogs.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, ActivityLogsActivity.class));
        });
    }

    private void fetchUserFullName() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        fullname = documentSnapshot.getString("fullName");
                        if (fullname == null) {
                            fullname = "Unknown User";
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDashboardActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupLockStatusListener() {
        lockStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer status = dataSnapshot.getValue(Integer.class);
                if (status != null) {
                    // Reflect the change on the switch button
                    switchButton.setChecked(status == 1);

                    // Determine the action based on the lock status value
                    String action = (status == 1) ? "Unlocked" : "Locked";

                    // Log the change in activity logs
                    logAction(action);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this, "Error listening to lock status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLockStatus(boolean isChecked) {
        int lockStatusValue = isChecked ? 1 : 0;  // 1 for unlocked, 0 for locked
        String action = isChecked ? "Unlocked" : "Locked";  // Action text

        // Directly update the `lockStatus` value in the database
        lockStatusRef.setValue(lockStatusValue)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserDashboardActivity.this, "Lock status updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Failed to update lock status.", Toast.LENGTH_SHORT).show());
    }

    private void logAction(String action) {
        // Get the current date and time
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a new log entry with details
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("action", action);           // "Locked" or "Unlocked"
        logEntry.put("fullName", fullname != null ? fullname : "RFID/Keypad");  // User who made the change
        logEntry.put("device", "Mobile");          // Current device
        logEntry.put("timestamp", timestamp);      // Date and time of the action

        // Push a new entry to `activityLogs` to record the action
        activityLogsRef.push().setValue(logEntry)
                .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Failed to log action.", Toast.LENGTH_SHORT).show());
    }
}
