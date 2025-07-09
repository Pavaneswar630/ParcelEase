package com.example.parcelease.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "ParcelEaseSession";
    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void createLoginSession(int userId, String name, String location, String profilePicUrl) {
        editor.putInt("user_id", userId);
        editor.putString("user_name", name);
        editor.putString("user_location", location);
        editor.putString("profile_pic", profilePicUrl); // Save profile picture URL
        editor.apply();
    }

    public int getUserId() {
        return prefs.getInt("user_id", -1);
    }

    public String getUserName() {
        return prefs.getString("user_name", null);
    }

    public String getUserLocation() {
        return prefs.getString("user_location", null);
    }

    public String getProfilePic() {
        return prefs.getString("profile_pic", ""); // Default empty if no profile pic
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
