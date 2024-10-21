package com.example.qbyte;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MemberInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TabLayout tabLayout;
    private List<User> allUsers; // List to hold all users
    private List<User> blockedUsers; // List to hold blocked users

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_info);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize user lists (replace with actual data fetching logic)
        allUsers = fetchAllUsers();
        blockedUsers = fetchBlockedUsers();

        // Set up RecyclerView
        userAdapter = new UserAdapter(allUsers); // Start with all users
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add tabs programmatically
        tabLayout.addTab(tabLayout.newTab().setText("Members").setContentDescription("All Users Tab"));
        tabLayout.addTab(tabLayout.newTab().setText("Blocked").setContentDescription("Blocked Users Tab"));

        // Add tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Load All Users
                    userAdapter.updateUserList(allUsers);
                } else if (tab.getPosition() == 1) {
                    // Load Blocked Users
                    userAdapter.updateUserList(blockedUsers);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Load all users by default
        loadAllUsers();
    }

    private void loadAllUsers() {
        // Logic to load all users into the RecyclerView
        userAdapter.updateUserList(allUsers);
    }

    private void loadBlockedUsers() {
        // Logic to load blocked users into the RecyclerView
        userAdapter.updateUserList(blockedUsers);
    }

    private List<User> fetchAllUsers() {
        // Dummy method to simulate fetching users from a database
        return new ArrayList<>(); // Replace with actual data fetching
    }

    private List<User> fetchBlockedUsers() {
        // Dummy method to simulate fetching blocked users from a database
        return new ArrayList<>(); // Replace with actual data fetching
    }
}
