package com.example.parcelease.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parcelease.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private AutoCompleteTextView autoCompleteSearch;
    private Button btnConfirmLocation;

    private LatLng selectedLatLng;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_map_selection);

        autoCompleteSearch = findViewById(R.id.autoCompleteSearch);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        mapView = findViewById(R.id.mapView);

        // Initialize the MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent intent = new Intent();
                intent.putExtra("selected_latitude", selectedLatLng.latitude);
                intent.putExtra("selected_longitude", selectedLatLng.longitude);
                intent.putExtra("selected_address", autoCompleteSearch.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(MapSelectionActivity.this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Set initial position and zoom (example: your city)
        LatLng initialLatLng = new LatLng(12.9716, 77.5946); // Bangalore, India
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 12));

        // Listen for map clicks
        gMap.setOnMapClickListener(latLng -> {
            gMap.clear();
            gMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatLng = latLng;

            Geocoder geocoder = new Geocoder(MapSelectionActivity.this, Locale.getDefault());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new async geocoder API
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, new Geocoder.GeocodeListener() {
                    @Override
                    public void onGeocode(@NonNull List<Address> addresses) {
                        if (!addresses.isEmpty()) {
                            String address = addresses.get(0).getAddressLine(0);
                            runOnUiThread(() -> autoCompleteSearch.setText(address));
                        } else {
                            runOnUiThread(() -> autoCompleteSearch.setText("Unknown location"));
                        }
                    }
                    @Override
                    public void onError(@NonNull String errorMessage) {
                        runOnUiThread(() -> autoCompleteSearch.setText("Error: " + errorMessage));
                    }
                });
            } else {
                // Use deprecated sync geocoder API
                try {
                    @SuppressWarnings("deprecation")
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String address = addresses.get(0).getAddressLine(0);
                        autoCompleteSearch.setText(address);
                    } else {
                        autoCompleteSearch.setText("Unknown location");
                    }
                } catch (IOException e) {
                    autoCompleteSearch.setText("Error: " + e.getMessage());
                }
            }
        });
    }

    // Forward lifecycle methods to MapView

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}
