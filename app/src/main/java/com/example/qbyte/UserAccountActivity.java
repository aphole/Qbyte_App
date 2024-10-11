package com.example.qbyte;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAccountActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView changeProfileButton, fullName, email, accountCreatedDate;
    private Button logoutButton;

    FirebaseAuth auth;
    FirebaseFirestore db;

    // Declare ActivityResultLauncher for image picking
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        changeProfileButton = findViewById(R.id.change_profile_button);
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        accountCreatedDate = findViewById(R.id.account_created_date);
        logoutButton = findViewById(R.id.user_logout);

        // Set initial user data (from Firebase Authentication)
        setUserData();

        // Initialize the ActivityResultLauncher for picking an image
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profilePicture.setImageURI(selectedImageUri); // Set the selected image as the profile picture
                        }
                    }
                }
        );

        // Set onClickListener for the change profile button
        changeProfileButton.setOnClickListener(v -> openImageChooser());

        // Set onClickListener for logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Method to open image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Fetch and set user data from Firebase Auth and Firestore
    private void setUserData() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Set email and account creation date
            email.setText("Email: " + user.getEmail());
            accountCreatedDate.setText("Account Created: " + user.getMetadata().getCreationTimestamp());

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
