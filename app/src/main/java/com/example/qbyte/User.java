package com.example.qbyte;

public class User {
    private String name;
    private String email;
    private boolean isBlocked;

    // Constructor
    public User(String name, String email, boolean isBlocked) {
        this.name = name;
        this.email = email;
        this.isBlocked = isBlocked;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    // Setter for isBlocked
    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
