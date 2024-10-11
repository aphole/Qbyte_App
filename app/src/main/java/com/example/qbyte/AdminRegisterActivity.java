package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qbyte.AdminDashboardActivity;
import com.example.qbyte.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText adminName, emailInput, passwordInput, confirmPass;
    private Button adminRegisterBtn;

    TextView adminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adminName = findViewById(R.id.adminName);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPass = findViewById(R.id.confirmPass);
        adminRegisterBtn = findViewById(R.id.adminRegisterButton);
        adminLogin = findViewById(R.id.loginNow);

        // Redirect to Admin Login
        adminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });

        adminRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerAdmin();
            }
        });
    }

    private void registerAdmin() {
        String name = adminName.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPass.getText().toString();

        // Check if the password and confirm password fields match
        if (!password.equals(confirmPassword)) {
            confirmPass.setError("Passwords do not match");
            confirmPass.requestFocus();
            return; // Exit the method if passwords do not match
        }

        // Firebase Authentication logic here
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = auth.getCurrentUser();

                        // Store the full name and admin status in Firestore
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("fullName", name);
                        adminData.put("email", email);
                        adminData.put("isAdmin", true); // Indicate this user is an admin

                        assert user != null;
                        db.collection("admin")
                                .document(user.getUid())
                                .set(adminData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Admin data successfully written!");
                                    // Redirect to the admin dashboard or another activity
                                    Intent intent = new Intent(getApplicationContext(), AdminLoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FirebaseAuth", "createUserWithEmail:failure", task.getException());
                    }
                });
    }

}
