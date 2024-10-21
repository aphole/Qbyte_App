package com.example.qbyte;

public class User {
    private String id; // Add other user attributes as needed
    private String name;
    private boolean isBlocked; // To determine if the user is blocked

    // Constructor
    public User(String id, String name, boolean isBlocked) {
        this.id = id;
        this.name = name;
        this.isBlocked = isBlocked;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isBlocked() {
        return isBlocked;
    }
}
