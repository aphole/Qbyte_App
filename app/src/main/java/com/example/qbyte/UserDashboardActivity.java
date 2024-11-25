package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class UserDashboardActivity extends AppCompatActivity {

    private CardView userInfo, activityLogs;
    private FirebaseAuth mAuth;
    private DatabaseReference lockStatusRef;
    private DatabaseReference activityLogsRef;
    private FirebaseFirestore db;
    private SwitchCompat switchButton;

    private boolean isLogPending = false; // Flag to track if the log is pending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        lockStatusRef = FirebaseDatabase.getInstance().getReference("LockStatus");  // Lock status path
        activityLogsRef = FirebaseDatabase.getInstance().getReference("activityLogs");  // Activity logs path

        userInfo = findViewById(R.id.module_user_info);
        activityLogs = findViewById(R.id.module_activity_log);
        switchButton = findViewById(R.id.switchButton);

        // Setup the lock status listener for the current user
        setupLockStatusListener();

        // Listen for switch button state changes
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update lock status and log action when switch is toggled
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

    private void setupLockStatusListener() {
        // Listen to lock status changes globally
        lockStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer lockStatus = dataSnapshot.getValue(Integer.class);

                if (lockStatus != null) {
                    // Update the switch based on the lock status from Firebase
                    switchButton.setChecked(lockStatus == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this, "Error listening to lock status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLockStatus(boolean isChecked) {
        if (isLogPending) return;  // Skip if a log is already pending

        int lockStatusValue = isChecked ? 1 : 0;

        isLogPending = true; // Set pending flag

        lockStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentStatus = snapshot.getValue(Integer.class);

                if (currentStatus != null && currentStatus == lockStatusValue) {
                    // Status already matches; no update needed
                    isLogPending = false;
                    return;
                }

                // Proceed to update the lock status
                lockStatusRef.setValue(lockStatusValue)
                        .addOnSuccessListener(aVoid -> {
                            fetchFullNameAndLogAction(isChecked ? "Unlocked" : "Locked", isChecked);
                            isLogPending = false;
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UserDashboardActivity.this, "Failed to update lock status.", Toast.LENGTH_SHORT).show();
                            isLogPending = false;
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboardActivity.this, "Error reading lock status.", Toast.LENGTH_SHORT).show();
                isLogPending = false;
            }
        });
    }

    private void fetchFullNameAndLogAction(String action, boolean isChecked) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "Unknown";

        if (!currentUserId.equals("Unknown")) {
            // Fetch full name from Firestore for app-triggered actions
            db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String fullName = documentSnapshot.exists() ? documentSnapshot.getString("fullName") : "Unknown User";
                        logAction(action, currentUserId, fullName, "App");
                    })
                    .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show());
        } else {
            // Log action for unknown or unauthenticated users
            logAction(action, currentUserId, "Unknown User", "App");
        }
    }

    private void logAction(String action, String userId, String fullName, String source) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("action", action);
        logEntry.put("userId", userId);
        logEntry.put("fullName", fullName);
        logEntry.put("timestamp", timestamp);
        logEntry.put("source", source);

        activityLogsRef.push().setValue(logEntry)
                .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Failed to log action.", Toast.LENGTH_SHORT).show());
    }
}
