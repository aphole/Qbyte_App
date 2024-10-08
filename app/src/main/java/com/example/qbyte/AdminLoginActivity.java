package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class AdminLoginActivity extends AppCompatActivity {

    TextView textView;
    Button buttonLogin;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;
    FirebaseFirestore db; // Firestore instance to check admin role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        textView = findViewById(R.id.registerNow);
        buttonLogin = findViewById(R.id.adminLoginButton);
        editTextEmail = findViewById(R.id.adminEmail);
        editTextPassword = findViewById(R.id.adminPass);
        progressBar = findViewById(R.id.progressBar);

        // Redirect to Admin Registration
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminRegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(AdminLoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(AdminLoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication for Admin Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign-in success, now check if the user is an admin
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            // Retrieve user document from Firestore to check role
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document != null && document.exists()) {
                                                Boolean isAdmin = document.getBoolean("isAdmin");

                                                if (isAdmin != null && isAdmin) {
                                                    // User is an admin, proceed to Admin Dashboard
                                                    Toast.makeText(AdminLoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // User is not an admin, show error
                                                    Toast.makeText(AdminLoginActivity.this, "Access Denied. Not an Admin.", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut(); // Sign out the non-admin user
                                                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                // No user data found
                                                Toast.makeText(AdminLoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Firestore retrieval failed
                                            Toast.makeText(AdminLoginActivity.this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            // If sign-in fails
                            Toast.makeText(AdminLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
