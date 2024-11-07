package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

public class UserDashboardActivity extends AppCompatActivity {

    CardView userInfo;
    FirebaseAuth mAuth;
    FirebaseFirestore db;  // Firestore instance
    ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        userInfo = findViewById(R.id.module_user_info);

        // Start listening to user status
        listenToUserStatus();

        userInfo.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, UserAccountActivity.class));
        });
    }

    private void listenToUserStatus() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userListener = db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(UserDashboardActivity.this, "Error fetching user status.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean isBlocked = documentSnapshot.getBoolean("isBlocked");
                        if (isBlocked != null && isBlocked) {
                            // User is blocked, redirect to BlockedUserActivity
                            Intent intent = new Intent(UserDashboardActivity.this, BlockedUserActivity.class);
                            startActivity(intent);
                            finish(); // Close the current activity
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the listener to prevent memory leaks
        if (userListener != null) {
            userListener.remove();
        }
    }
}
