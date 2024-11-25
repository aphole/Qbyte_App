package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textView = findViewById(R.id.registerNow);
        buttonLogin = findViewById(R.id.adminLoginButton);
        editTextEmail = findViewById(R.id.adminEmail);
        editTextPassword = findViewById(R.id.adminPass);
        progressBar = findViewById(R.id.progressBar);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminRegisterActivity.class);
            startActivity(intent);
            finish();
        });

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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            db.collection("admin").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document != null && document.exists()) {
                                                Boolean isAdmin = document.getBoolean("isAdmin");
                                                Boolean isApproved = document.getBoolean("isApproved");

                                                if (isAdmin != null && isAdmin) {
                                                    if (isApproved != null && isApproved) {
                                                        // Redirect to Admin Dashboard
                                                        Toast.makeText(AdminLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Redirect to AccountReviewActivity
                                                        Toast.makeText(this, "Account not approved. Redirecting for review...", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), AccountReviewActivity.class);
                                                        startActivity(intent);
                                                        finish();

                                                        // Add a delay before redirecting back to login
                                                        new Handler().postDelayed(() -> {
                                                            Intent redirectIntent = new Intent(AdminLoginActivity.this, AdminLoginActivity.class);
                                                            startActivity(redirectIntent);
                                                            finish();
                                                        }, 3000); // 3 seconds delay
                                                    }
                                                } else {
                                                    Toast.makeText(this, "Access Denied. Not an Admin.", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();
                                                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
