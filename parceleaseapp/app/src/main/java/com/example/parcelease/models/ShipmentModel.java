package com.example.parcelease.models;

public class ShipmentModel {
    public String orderId;
    public String date;
    public String status;
    public String fromAddress;
    public String toAddress;
    public String weight;
    public String size;
    public String amount;
    public String type; // "parcel", "truck", "incity"
    public String realId; // actual ID to pass to details API

    public ShipmentModel(String orderId, String date, String status, String fromAddress,
                         String toAddress, String weight, String size, String amount, String type, String realId) {
        this.orderId = orderId;
        this.date = date;
        this.status = status;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.weight = weight;
        this.size = size;
        this.amount = amount;
        this.type = type;
        this.realId = realId;
    }

    // ✅ Add this getter so MyShipmentsActivity can access date
    public String getDate() {
        return date;
    }
}
