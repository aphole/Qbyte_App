package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText userName, userEmail, userPass, confirmPass;
    private Button userRegisterBtn;
    private FirebaseUser currentAdminUser;  // Variable to store the current admin user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Initialize Firebase Authentication and Firestore database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Store the current admin user before creating a new user
        currentAdminUser = auth.getCurrentUser();

        // Initialize views
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPass = findViewById(R.id.userPass);
        confirmPass = findViewById(R.id.confirmPass);
        userRegisterBtn = findViewById(R.id.userRegisterButton);

        // Set click listener for the register button
        userRegisterBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String password = userPass.getText().toString().trim();
        String confirmPassword = confirmPass.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            userName.setError("Full name is required");
            userName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            userEmail.setError("Email is required");
            userEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            userPass.setError("Password must be at least 6 characters long");
            userPass.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPass.setError("Passwords do not match");
            confirmPass.requestFocus();
            return;
        }

        if (currentAdminUser != null) {
            currentAdminUser.getIdToken(true)
                    .addOnSuccessListener(getTokenResult -> {
                        String adminToken = getTokenResult.getToken(); // Save the admin token

                        // Proceed to create a new user
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser newUser = auth.getCurrentUser();

                                        // Create user data to store in Firestore
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("fullName", name);
                                        userData.put("email", email);
                                        userData.put("isAdmin", false);  // Default to regular user
                                        userData.put("isBlocked", false); // Default to not blocked

                                        // Save the user's data in Firestore
                                        if (newUser != null) {
                                            db.collection("users")
                                                    .document(newUser.getUid())
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("Firestore", "User data successfully written!");

                                                        // Re-authenticate admin after user creation
                                                        reAuthenticateAdmin(adminToken);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w("Firestore", "Error writing user data", e);
                                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    } else {
                                        // Handle registration failure
                                        Exception e = task.getException();
                                        Log.w("FirebaseAuth", "createUserWithEmail:failure", e);
                                        if (e != null) {
                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseAuth", "Failed to fetch admin token", e);
                        Toast.makeText(this, "Failed to authenticate admin", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w("FirebaseAuth", "Current admin user is null");
            Toast.makeText(this, "Admin not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void reAuthenticateAdmin(String adminToken) {
        if (adminToken != null) {
            currentAdminUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FirebaseAuth", "Admin session restored");
                            Intent intent = new Intent(this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w("FirebaseAuth", "Admin session restore failed", task.getException());
                            Toast.makeText(this, "Failed to restore admin session", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.w("FirebaseAuth", "Admin token is null");
            Toast.makeText(this, "Admin authentication token is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
