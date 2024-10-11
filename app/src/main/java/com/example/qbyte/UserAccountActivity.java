package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserAccountActivity extends AppCompatActivity {

    private TextView fullName, email, accountCreatedDate;
    private Button logoutButton;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        accountCreatedDate = findViewById(R.id.account_created_date);
        logoutButton = findViewById(R.id.user_logout);

        // Set initial user data (from Firebase Authentication)
        setUserData();


        // Set onClickListener for logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Fetch and set user data from Firebase Auth and Firestore
    private void setUserData() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Set email
            email.setText("Email: " + user.getEmail());

            // Convert creation timestamp to Date
            long creationTimestamp = user.getMetadata().getCreationTimestamp();
            Date creationDate = new Date(creationTimestamp);

            // Format the Date to show Day, Month, and Year
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(creationDate);

            // Set account creation date
            accountCreatedDate.setText("Account Created: " + formattedDate);

            // Fetch additional user data from Firestore
            String uid = user.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Set full name from Firestore
                    String name = documentSnapshot.getString("fullName");
                    fullName.setText("Full Name: " + name);
                }
            }).addOnFailureListener(e -> {
                // Handle any errors here
                fullName.setText("Full Name: Unknown");
            });
        }
    }
}
