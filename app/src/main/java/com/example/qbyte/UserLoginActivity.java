package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserLoginActivity extends AppCompatActivity {

    Button buttonLogin;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;
    FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        buttonLogin = findViewById(R.id.loginButton);
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        progressBar = findViewById(R.id.progress_Bar);

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserLoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserLoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication for User Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign-in success, check if the user is blocked
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document != null && document.exists()) {
                                                Boolean isBlocked = document.getBoolean("isBlocked");

                                                if (isBlocked != null && isBlocked) {
                                                    // User is blocked
                                                    Toast.makeText(UserLoginActivity.this, "Your account is blocked. Please contact support.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), BlockedUserActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // User is not blocked, proceed to User Dashboard
                                                    Toast.makeText(UserLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), UserDashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                // No user data found
                                                Toast.makeText(UserLoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Firestore retrieval failed
                                            Toast.makeText(UserLoginActivity.this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // If sign-in fails
                            Toast.makeText(UserLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
