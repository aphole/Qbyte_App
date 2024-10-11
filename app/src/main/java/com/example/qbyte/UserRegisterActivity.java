package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    EditText userName, userEmail, userPass, confirmPass;
    Button userRegisterBtn;
    FirebaseUser currentAdminUser;  // Variable to store the current admin user

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

    // Method to register a new user
    private void registerUser() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String password = userPass.getText().toString();
        String confirmPassword = confirmPass.getText().toString();

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPass.setError("Passwords do not match");
            confirmPass.requestFocus();
            return; // Exit the method if passwords do not match
        }

        // Save admin token before creating new user
        if (currentAdminUser != null) {
            currentAdminUser.getIdToken(true)
                    .addOnSuccessListener(getTokenResult -> {
                        String adminToken = getTokenResult.getToken();  // Save this token

                        // Proceed to create the new user
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        // New user created successfully
                                        FirebaseUser newUser = auth.getCurrentUser();

                                        // Create user data to store in Firestore
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("fullName", name);
                                        userData.put("email", email);
                                        userData.put("isAdmin", false);  // Mark as a regular user

                                        // Store the user's data in Firestore
                                        assert newUser != null;
                                        db.collection("users")
                                                .document(newUser.getUid())
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Firestore", "User data successfully written!");

                                                    // Re-authenticate admin using the token
                                                    reAuthenticateAdmin(adminToken);
                                                })
                                                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
                                    } else {
                                        // If registration fails, log the error
                                        Log.w("FirebaseAuth", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(UserRegisterActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
        }
    }

    // Method to re-authenticate the admin user using their token
    private void reAuthenticateAdmin(String adminToken) {
        if (adminToken != null) {
            // Reload the admin user to restore the session
            currentAdminUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Admin session restored, redirect to admin dashboard
                            Intent intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w("FirebaseAuth", "Admin session restore failed", task.getException());
                            Toast.makeText(UserRegisterActivity.this, "Failed to restore admin session", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.w("FirebaseAuth", "Admin token is null");
        }
    }

}
