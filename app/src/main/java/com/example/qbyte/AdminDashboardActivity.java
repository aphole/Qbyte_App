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
    FirebaseUser user;
    CardView createUserCard, memberInfoCard, activityLogs;  // Declare the CardView for the "Create User Account" module

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dasboard);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        createUserCard = findViewById(R.id.module_create_user);  // Initialize the CardView
        memberInfoCard = findViewById(R.id.module_member_info);  // Initialize the CardView
        activityLogs = findViewById(R.id.module_activity_logs);

    user = auth.getCurrentUser();

       

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

        memberInfoCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MemberInfoActivity.class);
            startActivity(intent);
        });

        activityLogs.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ActivityLogsActivity.class);
            startActivity(intent);
        });


    }

}
