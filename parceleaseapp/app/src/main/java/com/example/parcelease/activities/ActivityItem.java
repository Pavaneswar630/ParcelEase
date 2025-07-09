package com.example.parcelease.activities;

public class ActivityItem {
    private String Title;
    private String Id;
    private String Time;
    private int imgIcon;

    public ActivityItem(String Title, String Id, String Time, int imgIcon) {
        this.Title = Title;
        this.Id = Id;
        this.Time = Time;
        this.imgIcon = imgIcon;
    }

    public String getTitle() {
        return Title;
    }

    public String getId() {
        return Id;
    }

    public String getTime() {
        return Time;
    }

    public int getImgIcon() {
        return imgIcon;
    }
}
