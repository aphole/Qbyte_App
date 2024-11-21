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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

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
    private SwitchCompat switchButton;

    private boolean isLogPending = false; // Flag to track if the log is pending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
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
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer lockStatus = dataSnapshot.getValue(Integer.class);

                if (lockStatus != null) {
                    // Update the switch based on the lock status from Firebase
                    switchButton.setChecked(lockStatus == 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this, "Error listening to lock status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLockStatus(boolean isChecked) {
        // Check if log is already pending
        if (isLogPending) {
            return;  // Skip if log is already pending
        }

        // Set the lock status in Firebase (0 for locked, 1 for unlocked)
        int lockStatusValue = isChecked ? 1 : 0;

        // Set a flag indicating that a log creation is pending
        isLogPending = true;

        // Use Firebase transaction to ensure atomicity of lock status change
        lockStatusRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // If lockStatus is already set, don't make any changes
                if (mutableData.getValue() != null) {
                    return Transaction.success(mutableData);
                }

                // Update the lock status
                mutableData.setValue(lockStatusValue);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    // Successfully set lock status
                    logAction(isChecked ? "Unlocked" : "Locked");

                    // Reset the flag after logging
                    isLogPending = false;
                } else {
                    // Error setting lock status
                    Toast.makeText(UserDashboardActivity.this, "Failed to update lock status.", Toast.LENGTH_SHORT).show();
                    isLogPending = false;
                }
            }
        });
    }

    private void logAction(String action) {
        // Get the current date and time
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Get the current user ID (the user who triggered the action)
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Create a new log entry with details
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("action", action);              // Action (locked or unlocked)
        logEntry.put("userId", currentUserId); // User ID of the person who triggered the action
        logEntry.put("timestamp", timestamp);       // Date and time of the action

        // Push a new entry to the activityLogs path to record the action
        activityLogsRef.push().setValue(logEntry)
                .addOnFailureListener(e -> Toast.makeText(UserDashboardActivity.this, "Failed to log action.", Toast.LENGTH_SHORT).show());
    }
}

