package com.example.parcelease.models;

public class TimelineModel {
    private String status;
    private String location;
    private String timestamp;

    // ✅ Constructor
    public TimelineModel(String status, String location, String timestamp) {
        this.status = status;
        this.location = location;
        this.timestamp = timestamp;
    }

    // ✅ Getter methods with return types (String)
    public String getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // (Optional) Setter methods if needed
    public void setStatus(String status) {
        this.status = status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
