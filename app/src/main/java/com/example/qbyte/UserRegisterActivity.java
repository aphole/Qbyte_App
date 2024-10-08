package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the layout file (activity_user_register.xml)
        setContentView(R.layout.activity_user_register);

        // Initialize Firebase authentication and Firestore database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // Firebase Authentication logic
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful
                        FirebaseUser user = auth.getCurrentUser();

                        // Create user data to store in Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("fullName", name);
                        userData.put("email", email);
                        userData.put("isAdmin", false);  // Mark as a regular user

                        // Store the user's data in Firestore
                        assert user != null;
                        db.collection("users")
                                .document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "User data successfully written!");
                                    // Redirect regular user to User Dashboard
                                    Intent intent = new Intent(getApplicationContext(), UserDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
                    } else {
                        // If registration fails, log the error
                        Log.w("FirebaseAuth", "createUserWithEmail:failure", task.getException());
                    }
                });
    }
}
