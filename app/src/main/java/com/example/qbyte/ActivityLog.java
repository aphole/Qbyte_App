package com.example.qbyte;

public class ActivityLog {
    private String logId;
    private String action;
    private String timestamp;
    private String device;
    private String fullName;

    public ActivityLog() {
        // Default constructor required for Firebase
    }

    public ActivityLog(String action, String timestamp, String device, String fullName) {
        this.action = action;
        this.timestamp = timestamp;
        this.device = device;
        this.fullName = fullName;
    }

    // Getters and setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
