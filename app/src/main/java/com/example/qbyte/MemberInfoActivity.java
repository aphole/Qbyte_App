package com.example.qbyte;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MemberInfoActivity extends AppCompatActivity implements UserAdapter.OnUserBlockListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TabLayout tabLayout;
    private List<User> allUsers; // List to hold all users
    private List<User> blockedUsers; // List to hold blocked users
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_info);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize user lists
        allUsers = new ArrayList<>();
        blockedUsers = new ArrayList<>();

        // Set up RecyclerView
        userAdapter = new UserAdapter(allUsers, this, this); // Pass context and block listener
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch users from Firestore
        fetchAllUsers();
        fetchBlockedUsers();

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
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void fetchAllUsers() {
        db.collection("users")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    allUsers.clear(); // Clear the list to avoid duplication
                    assert queryDocumentSnapshots != null;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        Boolean isBlocked = document.getBoolean("isBlocked");

                        // Only add users to allUsers if they are not blocked
                        if (isBlocked == null || !isBlocked) {
                            allUsers.add(new User(name, email, false));
                        }
                    }

                    // Update the adapter if "All Users" tab is active
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        userAdapter.updateUserList(allUsers);
                    }
                });
    }


    private void fetchBlockedUsers() {
        db.collection("users")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    blockedUsers.clear(); // Clear the list to avoid duplication
                    assert queryDocumentSnapshots != null;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        Boolean isBlocked = document.getBoolean("isBlocked");

                        // Only add users to blockedUsers if they are blocked
                        if (isBlocked != null && isBlocked) {
                            blockedUsers.add(new User(name, email, true));
                        }
                    }

                    // Update the adapter if "Blocked Users" tab is active
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        userAdapter.updateUserList(blockedUsers);
                    }
                });
    }


    // Implement the onBlockUser method from OnUserBlockListener
    @Override
    public void onBlockUser(User user) {
        // Query the user document by email
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId(); // Get the document ID
                        db.collection("users").document(documentId)
                                .update("isBlocked", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "User successfully blocked.", Toast.LENGTH_SHORT).show();
                                    // Update your user lists...
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error blocking user.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onUnblockUser(User user) {
        // Query the user document by email
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId(); // Get the document ID
                        db.collection("users").document(documentId)
                                .update("isBlocked", false)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "User successfully unblocked.", Toast.LENGTH_SHORT).show();
                                    // Update your user lists...
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error unblocking user.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Helper method to refresh the displayed user list based on the currently selected tab.
     */
    private void updateDisplayedUserList() {
        if (tabLayout.getSelectedTabPosition() == 0) {
            // Display all users when the "All Users" tab is active
            userAdapter.updateUserList(allUsers);
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            // Display blocked users when the "Blocked Users" tab is active
            userAdapter.updateUserList(blockedUsers);
        }
    }
}
