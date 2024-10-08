package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class UserLoginActivity extends AppCompatActivity {

    Button buttonLogin;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;
    FirebaseFirestore db;  // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        buttonLogin = findViewById(R.id.loginButton);
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        progressBar = findViewById(R.id.progress_Bar);
        progressBar.setVisibility(View.GONE);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

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

                // Authenticate with Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, check user role
                                    checkUserRole(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                                } else {
                                    Toast.makeText(UserLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void checkUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Boolean isAdmin = task.getResult().getBoolean("isAdmin");
                            if (isAdmin != null && isAdmin) {
                                // Show toast for admin login and redirect
                                Toast.makeText(UserLoginActivity.this, "This account is for admin use.", Toast.LENGTH_SHORT).show();
                                // Add a slight delay to ensure the user sees the toast before redirecting
                                progressBar.postDelayed(() -> {
                                    Intent intent = new Intent(UserLoginActivity.this, StartActivity.class);
                                    startActivity(intent);
                                    finish();
                                }, 1000);  // 1 second delay
                            } else {
                                // User is a regular user, redirect to User Dashboard
                                Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(UserLoginActivity.this, "User does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserLoginActivity.this, "Failed to get user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
