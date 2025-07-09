package com.example.parcelease.models;

public class Ticket {
    private String id;
    private String subject;
    private String message;
    private String status;
    private String createdAt;
    private String response;

    public Ticket(String id, String subject, String message, String status, String createdAt, String response) {
        this.id = id;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.response = response;
    }

    public String getId() { return id; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getResponse() { return response; }
}
