package com.example.qbyte;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "aphole"; // Replace with your admin password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);

        // Apply insets for EdgeToEdge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the buttons in the layout
        Button userButton = findViewById(R.id.userButton);
        Button adminButton = findViewById(R.id.adminButton);

        // Set onClickListener for the User button
        userButton.setOnClickListener(v -> {
            // Navigate to UserLoginActivity
            Intent intent = new Intent(StartActivity.this, UserLoginActivity.class);
            startActivity(intent);
        });

        // Set onClickListener for the Admin button
        adminButton.setOnClickListener(v -> {
            // Show password dialog before navigating to AdminLoginActivity
            showAdminPasswordDialog();
        });
    }

    private void showAdminPasswordDialog() {
        // Create an EditText to input the password
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter Admin Password");

        // Create a dialog
        new AlertDialog.Builder(this)
                .setTitle("Admin Access")
                .setMessage("Please enter the admin password to continue:")
                .setView(passwordInput)
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Check if the entered password is correct
                    String enteredPassword = passwordInput.getText().toString();
                    if (enteredPassword.equals(ADMIN_PASSWORD)) {
                        // Navigate to AdminLoginActivity if password is correct
                        Intent intent = new Intent(StartActivity.this, AdminLoginActivity.class);
                        startActivity(intent);
                    } else {
                        // Show an error message if the password is incorrect
                        showErrorDialog();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Access Denied")
                .setMessage("Incorrect password. Please try again.")
                .setPositiveButton("OK", null)
                .show();
    }
}
