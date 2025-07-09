package com.example.parcelease.models;

public class ActivityItem {
    private String title;
    private String shipmentId;
    private String timestamp;
    private String type;

    public ActivityItem(String title, String shipmentId, String timestamp, String type) {
        this.title = title;
        this.shipmentId = shipmentId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }
}
