package com.example.parcelease.activities;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.example.parcelease.utils.SessionManager;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import com.example.parcelease.utils.VolleySingleton;

import com.example.parcelease.R;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ParcelDetailsActivity extends AppCompatActivity {

    private TextView txtParcelId, txtPickupLocation, txtDropLocation, txtWeight, txtStatus, txtSenderName,
            txtSenderPhone, txtReceiverName, txtReceiverPhone, txtDeliveryTime, txtPackageType, txtCreatedAt,tvUserName;
    private String parcelId,type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcel_details);

        // Initialize TextViews
        txtParcelId = findViewById(R.id.txtParcelId);
        txtPickupLocation = findViewById(R.id.txtPickupLocation);
        txtDropLocation = findViewById(R.id.txtDropLocation);
        txtWeight = findViewById(R.id.txtWeight);
        txtStatus = findViewById(R.id.txtStatus);
        txtSenderName = findViewById(R.id.txtSenderName);
        txtSenderPhone = findViewById(R.id.txtSenderPhone);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtReceiverPhone = findViewById(R.id.txtReceiverPhone);
        txtDeliveryTime = findViewById(R.id.txtDeliveryTime);
        txtPackageType = findViewById(R.id.txtPackageType);
        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        tvUserName = findViewById(R.id.tv_recipient);
        // Get Parcel ID from intent
        parcelId = getIntent().getStringExtra("parcel_id");
        type = getIntent().getStringExtra("type");

        if (parcelId != null) {
            fetchParcelDetails(parcelId);
        } else {
            Toast.makeText(this, "Invalid Parcel ID", Toast.LENGTH_SHORT).show();
        }
        String userName = SessionManager.getInstance(getApplicationContext()).getUserName();

        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        } else {
            tvUserName.setText("Guest"); // Fallback if username is null
        }

    }

    private void fetchParcelDetails(String parcelId) {
        String url = ApiConfig.getbookingUrl();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Debugging: Print full response
                    System.out.println("API Response: " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");

                            // Set values to TextViews
                            txtParcelId.setText("Parcel ID: " + data.optString("parcel_id", "N/A"));
                            txtPickupLocation.setText("Pickup Location: " + data.optString("pickup_location", "N/A"));
                            txtDropLocation.setText("Drop Location: " + data.optString("drop_location", "N/A"));
                            txtWeight.setText("Weight: " + data.optString("weight", "0") + " kg");
                            txtStatus.setText("Status: " + data.optString("status", "Unknown"));
                            txtSenderName.setText("Sender Name: " + data.optString("sender_name", "N/A"));
                            txtSenderPhone.setText("Sender Phone: " + data.optString("sender_phone", "N/A"));
                            txtReceiverName.setText("Receiver Name: " + data.optString("receiver_name", "N/A"));
                            txtReceiverPhone.setText("Receiver Phone: " + data.optString("receiver_phone", "N/A"));
                            txtDeliveryTime.setText("Delivery Time: " + data.optString("delivery_time", "N/A"));
                            txtPackageType.setText("Package Type: " + data.optString("package_type", "N/A"));
                            txtCreatedAt.setText("Created At: " + data.optString("created_at", "N/A"));

                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching details", Toast.LENGTH_SHORT).show();
                    System.out.println("Volley Error: " + error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", "standard");
                params.put("parcel_id", parcelId);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

}
