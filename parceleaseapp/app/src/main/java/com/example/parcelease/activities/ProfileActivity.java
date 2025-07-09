package com.example.parcelease.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.parcelease.R;
import com.example.parcelease.utils.SessionManager;
import com.example.parcelease.utils.VolleySingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtUserName, txtEmail, txtUserId, txtParcelsDelivered, txtActiveShipments, txtSavedAddresses, termsTextView, support;
    private ImageView profileImage;
    private Button btnSignOut;
    private SessionManager sessionManager;
    BottomNavigationView bottomNavigationView;

    private static final String PROFILE_API_URL = ApiConfig.getprofileUrl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        txtUserName = findViewById(R.id.txtUserName);
        txtEmail = findViewById(R.id.txtEmail);  // ✅ Fixed missing initialization
        txtUserId = findViewById(R.id.txtUserId);
        txtParcelsDelivered = findViewById(R.id.txtParcelsDelivered);
        txtActiveShipments = findViewById(R.id.txtActiveShipments);
        txtSavedAddresses = findViewById(R.id.txtSavedAddresses);
        btnSignOut = findViewById(R.id.btnSignOut);
        ImageButton backBtn = findViewById(R.id.btnBackprofile);
        TextView termsTextView = findViewById(R.id.terms);
        TextView support = findViewById(R.id.helpandsupport);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                return true; // Already on Home
            } else if (itemId == R.id.nav_track) {
                startActivity(new Intent(ProfileActivity.this, TrackingActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(ProfileActivity.this, MyShipmentsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {

                return true;
            }
            return false;
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 👉 Do whatever you want on FAB click
                // For example, navigate to BookParcelActivity:
                Intent intent = new Intent(ProfileActivity.this, ParcelBookingActivity.class);
                startActivity(intent);
            }
        });

        sessionManager = SessionManager.getInstance(this);

        // Load profile data only if internet is available
        if (isNetworkAvailable()) {
            loadProfileData();
        } else {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }

        // Logout button action
        btnSignOut.setOnClickListener(v -> {
            sessionManager.clearSession();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
        termsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TermsActivity.class);
            startActivity(intent);
        });
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        });
        support.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpCenterActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        String userId = String.valueOf(sessionManager.getUserId());

        // ✅ Log user_id for debugging
        Log.d("USER_ID", "Sending user_id: " + userId);

        StringRequest request = new StringRequest(Request.Method.POST, PROFILE_API_URL,
                response -> {
                    // ✅ Log the full API response for debugging
                    Log.d("PROFILE_RESPONSE", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject user = jsonObject.getJSONObject("user");

                            txtUserName.setText(user.optString("name", "Unknown User"));
                            txtUserId.setText("ID: #" + user.optString("id", "N/A"));
                            txtParcelsDelivered.setText(user.optString("parcels_delivered", "0") + "\nParcels Delivered");
                            txtActiveShipments.setText(user.optString("active_shipments", "0") + "\nActive Shipments");
                            txtSavedAddresses.setText(user.optString("saved_addresses", "0") + "\nSaved Addresses");
                            txtEmail.setText(user.optString("email", "No Email"));

                            // Load profile image
                            String imageUrl = user.optString("profile_image", "");
                            if (!imageUrl.isEmpty() && imageUrl.startsWith("http")) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.ic_profile);
                            }
                        } else {
                            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading profile: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // ✅ Fixed network check (No deprecated methods)
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null &&
                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }
}
