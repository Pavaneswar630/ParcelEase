package com.example.parcelease.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import android.widget.ImageButton;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText edtParcelId;
    private Button btnTrack;
    ImageButton btnBack;
    private TextView txtParcelId, txtPickupLocation, txtDropLocation, txtCurrentLocation,
            txtWeight, txtStatus, txtCreatedAt, txtETA;
    private RequestQueue requestQueue;
    private GoogleMap mMap;
    private MapView mapView;

    private static final String TRACKING_API_URL = ApiConfig.gettrackparcelUrl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        edtParcelId = findViewById(R.id.edtParcelId);
        btnTrack = findViewById(R.id.btnTrackParcel);
        txtParcelId = findViewById(R.id.txtParcelId);
        txtPickupLocation = findViewById(R.id.txtPickupLocation);
        txtDropLocation = findViewById(R.id.txtDropLocation);
        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        txtWeight = findViewById(R.id.txtWeight);
        txtStatus = findViewById(R.id.txtStatus);
        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        txtETA = findViewById(R.id.txtETA);
        mapView = findViewById(R.id.mapView);

        requestQueue = Volley.newRequestQueue(this);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnTrack.setOnClickListener(v -> {
            String parcelId = edtParcelId.getText().toString().trim();
            if (!parcelId.isEmpty()) {
                trackParcel(parcelId);
            } else {
                Toast.makeText(TrackingActivity.this, "Enter a Parcel ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void trackParcel(String parcelId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, TRACKING_API_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject trackingDetails = jsonObject.getJSONObject("tracking_details");
                            String status = trackingDetails.getString("status");
                            String currentLocation = trackingDetails.getString("current_location");
                            JSONArray previousLocations = trackingDetails.getJSONArray("previous_locations");
                            String eta = trackingDetails.getString("eta");

                            JSONObject parcelDetails = jsonObject.getJSONObject("parcel_details");
                            String pickup = parcelDetails.getString("pickup_location");
                            String drop = parcelDetails.getString("drop_location");
                            String weight = parcelDetails.getString("weight");
                            String createdAt = parcelDetails.getString("created_at");

                            txtParcelId.setText(parcelId);
                            txtPickupLocation.setText(pickup);
                            txtDropLocation.setText(drop);
                            txtCurrentLocation.setText(currentLocation);
                            txtWeight.setText(weight + " kg");
                            txtStatus.setText(status);
                            txtCreatedAt.setText(createdAt);
                            txtETA.setText("ETA: " + eta);

                            // Parse previous locations as coordinates
                            List<LatLng> previousCoords = new ArrayList<>();
                            for (int i = 0; i < previousLocations.length(); i++) {
                                JSONObject obj = previousLocations.getJSONObject(i);
                                double lat = obj.getDouble("latitude");
                                double lng = obj.getDouble("longitude");
                                previousCoords.add(new LatLng(lat, lng));
                            }

                            plotCoordinatesOnMap(previousCoords, pickup, currentLocation, drop);
                        } else {
                            Toast.makeText(TrackingActivity.this, "Invalid Parcel ID", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TrackingActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(TrackingActivity.this, "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("parcel_id", parcelId);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void plotCoordinatesOnMap(List<LatLng> previousCoords, String pickup, String currentLocation, String drop) {
        if (mMap == null) return;

        mMap.clear();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<String> places = List.of(pickup, currentLocation, drop);
        List<Boolean> customIcons = List.of(false, true, false);

        for (int i = 0; i < places.size(); i++) {
            String place = places.get(i);
            boolean isCurrent = customIcons.get(i);

            try {
                List<Address> addresses = geocoder.getFromLocationName(place, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    MarkerOptions marker = new MarkerOptions().position(latLng).title(place);
                    if (isCurrent) {
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.img_41);
                        Bitmap smallMarker = Bitmap.createScaledBitmap(icon, 100, 100, false);
                        marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    }
                    mMap.addMarker(marker);

                    if (i == 0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    }

                    previousCoords.add(latLng); // also add to polyline
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (previousCoords.size() >= 2) {
            mMap.addPolyline(new PolylineOptions().addAll(previousCoords).width(6).color(0xFF2196F3));
        }
    }

    // MapView lifecycle
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
