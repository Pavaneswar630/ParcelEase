package com.example.parcelease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.adapters.ShipmentAdapter;
import com.example.parcelease.models.ShipmentModel;
import com.example.parcelease.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import android.util.Log;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

public class MyShipmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    ImageButton btnBack;
    private List<ShipmentModel> shipmentList;
    private ShipmentAdapter adapter;
    private RequestQueue requestQueue;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myshipments);

        recyclerView = findViewById(R.id.recyclerMyShipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        shipmentList = new ArrayList<>();
        adapter = new ShipmentAdapter(this, shipmentList);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);
        btnBack = findViewById(R.id.backbut);
        btnBack.setOnClickListener(v -> finish());
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);

        // Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyShipmentsActivity.this, HomeActivity.class));
                return true; // Already on Home
            } else if (itemId == R.id.nav_track) {
                startActivity(new Intent(MyShipmentsActivity.this, TrackingActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MyShipmentsActivity.this, ProfileActivity.class));
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
                Intent intent = new Intent(MyShipmentsActivity.this, ParcelBookingActivity.class);
                startActivity(intent);
            }
        });
        ImageButton backBtn = findViewById(R.id.backbut);

        backBtn.setOnClickListener(v -> finish());

        fetchShipments();
    }

    private void fetchShipments() {
        String url = ApiConfig.gethistoryUrl();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SHIPMENTS_RESPONSE", response);
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        if ("success".equals(responseObject.optString("status"))) {
                            shipmentList.clear();

                            if (responseObject.has("parcels")) {
                                JSONArray parcels = responseObject.getJSONArray("parcels");
                                parseShipments(parcels, "parcel", "ORD-");
                            }

                            if (responseObject.has("trucks")) {
                                JSONArray trucks = responseObject.getJSONArray("trucks");
                                parseShipments(trucks, "truck", "TRK-");
                            }

                            if (responseObject.has("incity")) {
                                JSONArray incity = responseObject.getJSONArray("incity");
                                parseShipments(incity, "incity", "INC-");
                            }

                            // Sort by most recent booking first
                            Collections.sort(shipmentList, new Comparator<ShipmentModel>() {
                                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                                @Override
                                public int compare(ShipmentModel o1, ShipmentModel o2) {
                                    try {
                                        return sdf.parse(o2.getDate()).compareTo(sdf.parse(o1.getDate())); // Descending
                                    } catch (Exception e) {
                                        return 0;
                                    }
                                }
                            });

                            adapter.notifyDataSetChanged();
                        } else {
                            showToast(responseObject.optString("message", "Unknown error occurred"));
                        }
                    } catch (Exception e) {
                        showToast("Parsing error: " + e.getMessage());
                    }
                },
                error -> showToast("Network error: " + (error.getMessage() != null ? error.getMessage() : "Unknown"))
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void parseShipments(JSONArray jsonArray, String type, String prefix) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                String parcel_id = obj.optString("parcel_id", "N/A");
                String date = obj.optString("created_at", "Unknown");
                String status = obj.optString("status", "Pending");
                String from = obj.optString("pickup_location", "Unknown");
                String to = obj.optString("drop_location", "Unknown");
                String weight = obj.optString("weight", "0kg");
                String size = obj.optString("package_type", "N/A");
                String amount = obj.optString("amount", "0.00");

                shipmentList.add(new ShipmentModel(
                        parcel_id,
                        date,
                        status,
                        from,
                        to,
                        weight,
                        size,
                        amount,
                        type,
                        parcel_id
                ));
            } catch (Exception e) {
                showToast("Error parsing " + type + " shipment: " + e.getMessage());
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(MyShipmentsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
