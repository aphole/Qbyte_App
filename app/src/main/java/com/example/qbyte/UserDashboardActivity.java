package com.example.qbyte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserDashboardActivity extends AppCompatActivity {


   CardView userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);

        userInfo = findViewById(R.id.module_user_info);

        userInfo.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, UserAccountActivity.class));
        });

    }
}