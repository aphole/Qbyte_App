package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ImageView button;
    TextView textView;
    FirebaseUser user;
    CardView createUserCard;  // Declare the CardView for the "Create User Account" module

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dasboard);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        createUserCard = findViewById(R.id.module_create_user);  // Initialize the CardView

        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            finish();
        });

        // Set OnClickListener for the modules
        createUserCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, UserRegisterActivity.class);
            startActivity(intent);
        });
    }
}
