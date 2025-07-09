package com.example.parcelease.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Expressbookingactivity extends AppCompatActivity {

    private EditText etPickupLocation, etDropLocation, etSenderName, etSenderPhone, etSenderRemarks, etReceiverName, etReceiverPhone, etReceiverRemarks;
    private TextView weightText, totalAmountText;
    private Button bookParcel;
    private Switch switchSaveReceiver;
    private ImageButton increaseWeight, decreaseWeight;
    private AlertDialog loadingDialog;

    private int weight = 1;
    private double pricePerKg = 10.0;
    private float distanceKm = 0f;
    private String selectedSize = "";
    private String selectedTime = "";

    private LinearLayout sizeSmall, sizeMedium, sizeLarge;
    private LinearLayout time9to12, time12to15, time15to18, time18to20;

    private ImageView imgPickup, imgDrop;
    private TextView tvSelectSavedAddress;

    private final String parcelId = "EXP" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);

    private ActivityResultLauncher<Intent> pickupLocationLauncher;
    private ActivityResultLauncher<Intent> dropLocationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expressdelivery);

        ImageButton btnBack = findViewById(R.id.backbut);
        btnBack.setOnClickListener(v -> finish());

        // Initialize UI
        etPickupLocation = findViewById(R.id.pickloc);
        etDropLocation = findViewById(R.id.droploc);
        etSenderName = findViewById(R.id.sname);
        etSenderPhone = findViewById(R.id.sphone);
        etSenderRemarks = findViewById(R.id.sremarks);
        etReceiverName = findViewById(R.id.rname);
        etReceiverPhone = findViewById(R.id.rphone);
        etReceiverRemarks = findViewById(R.id.rremarks);
        weightText = findViewById(R.id.weight);
        switchSaveReceiver = findViewById(R.id.switchSaveReceiver1);
        totalAmountText = findViewById(R.id.amount);
        increaseWeight = findViewById(R.id.Increase);
        decreaseWeight = findViewById(R.id.Decrease);
        bookParcel = findViewById(R.id.book);

        imgPickup = findViewById(R.id.piLocation);
        imgDrop = findViewById(R.id.droLocation);

        sizeSmall = findViewById(R.id.S);
        sizeMedium = findViewById(R.id.M);
        sizeLarge = findViewById(R.id.L);

        time9to12 = findViewById(R.id.et9to12);
        time12to15 = findViewById(R.id.et12to5);
        time15to18 = findViewById(R.id.et15to18);
        time18to20 = findViewById(R.id.et18to20);
        tvSelectSavedAddress = findViewById(R.id.tvSelectSavedAddress);
        ImageButton backBtn = findViewById(R.id.backbut);
        backBtn.setOnClickListener(v -> finish());

        // Setup selection logic
        setSelectable(sizeSmall, "Small", "package");
        setSelectable(sizeMedium, "Medium", "package");
        setSelectable(sizeLarge, "Large", "package");

        setSelectable(time9to12, "bw9to12", "parcel");
        setSelectable(time12to15, "bw12to15", "parcel");
        setSelectable(time15to18, "bw15to18", "parcel");
        setSelectable(time18to20, "bw18to20", "parcel");

        updateWeightAndPrice();

        increaseWeight.setOnClickListener(v -> {
            weight++;
            updateWeightAndPrice();
        });

        decreaseWeight.setOnClickListener(v -> {
            if (weight > 1) {
                weight--;
                updateWeightAndPrice();
            }
        });

        // Book button
        bookParcel.setOnClickListener(view -> bookParcel());
        tvSelectSavedAddress.setOnClickListener(v -> fetchSavedAddressesAndShowDialog());

        // Register result launchers
        pickupLocationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String location = result.getData().getStringExtra("selected_address");
                        if (location != null) etPickupLocation.setText(location);
                    }
                });

        dropLocationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String location = result.getData().getStringExtra("selected_address");
                        if (location != null) etDropLocation.setText(location);
                    }
                });

        // Set image view click listeners
        imgPickup.setOnClickListener(v -> {
            Intent intent = new Intent(Expressbookingactivity.this, MapSelectionActivity.class);
            intent.putExtra("selection_type", "pickup");
            pickupLocationLauncher.launch(intent);
        });

        imgDrop.setOnClickListener(v -> {
            Intent intent = new Intent(Expressbookingactivity.this, MapSelectionActivity.class);
            intent.putExtra("selection_type", "drop");
            dropLocationLauncher.launch(intent);
        });
    }

    private void setSelectable(LinearLayout layout, String value, String type) {
        layout.setOnClickListener(view -> {
            if (type.equals("package")) {
                resetSelection(sizeSmall, sizeMedium, sizeLarge);
                selectedSize = value;
            } else if (type.equals("parcel")) {
                resetSelection(time9to12, time12to15, time15to18, time18to20);
                selectedTime = value;
            }
            layout.setBackgroundResource(R.drawable.bg_button_selected);
        });
    }
    private void calculateDistanceBetweenLocations(String pickup, String drop) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> pickupAddresses = geocoder.getFromLocationName(pickup, 1);
            List<Address> dropAddresses = geocoder.getFromLocationName(drop, 1);

            if (pickupAddresses == null || pickupAddresses.isEmpty() ||
                    dropAddresses == null || dropAddresses.isEmpty()) {
                Toast.makeText(this, "Unable to geocode one or both locations", Toast.LENGTH_SHORT).show();
                return;
            }

            Address pickupAddress = pickupAddresses.get(0);
            Address dropAddress = dropAddresses.get(0);

            float[] results = new float[1];
            Location.distanceBetween(
                    pickupAddress.getLatitude(), pickupAddress.getLongitude(),
                    dropAddress.getLatitude(), dropAddress.getLongitude(),
                    results
            );

            distanceKm = results[0] / 1000;

            Toast.makeText(this, "Distance: " + String.format(Locale.US, "%.2f", distanceKm) + " km", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding distance", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveReceiverAddress() {
        String url = ApiConfig.getsaveaddressUrl();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("name", etReceiverName.getText().toString());
                params.put("phone", etReceiverPhone.getText().toString());
                params.put("location", etDropLocation.getText().toString());
                params.put("remarks", etReceiverRemarks.getText().toString());
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
    private void fetchSavedAddressesAndShowDialog() {
        String url = ApiConfig.getsaveaddressesUrl();
        int userId = SessionManager.getInstance(getApplicationContext()).getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "No saved addresses found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray jsonArray = jsonObject.getJSONArray("addresses");
                        if (jsonArray.length() == 0) {
                            Toast.makeText(this, "No saved addresses found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] addressNames = new String[jsonArray.length()];
                        String[] addressLocations = new String[jsonArray.length()];
                        String[] addressPhones = new String[jsonArray.length()];
                        String[] addressRemarks = new String[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            addressNames[i] = obj.getString("name");
                            addressPhones[i] = obj.getString("phone");
                            addressLocations[i] = obj.getString("location");
                            addressRemarks[i] = obj.getString("remarks");
                        }

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                        builder.setTitle("Select Saved Address");
                        builder.setItems(addressNames, (dialog, which) -> {
                            etReceiverName.setText(addressNames[which]);
                            etReceiverPhone.setText(addressPhones[which]);
                            etDropLocation.setText(addressLocations[which]);
                            etReceiverRemarks.setText(addressRemarks[which]);
                        });
                        builder.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse saved addresses", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load saved addresses", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void resetSelection(LinearLayout... layouts) {
        for (LinearLayout layout : layouts) {
            layout.setBackgroundResource(R.drawable.bg_button_unselected);
        }
    }

    private void updateWeightAndPrice() {
        weightText.setText(weight + " kg");
        double total = weight * pricePerKg;
        totalAmountText.setText("Total: $" + String.format("%.2f", total));
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void bookParcel() {
        if (selectedSize.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select package size and delivery time", Toast.LENGTH_SHORT).show();
            return;
        }
        String pickup = etPickupLocation.getText().toString();
        String drop = etDropLocation.getText().toString();

        if (pickup.isEmpty() || drop.isEmpty()) {
            Toast.makeText(this, "Pickup and drop locations are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Calculate and show distance
        calculateDistanceBetweenLocations(pickup, drop);
        if (switchSaveReceiver.isChecked()) {
            saveReceiverAddress();
        }

        showLoadingDialog();

        String url = ApiConfig.getcreateparcelUrl();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Parcel booked successfully!", Toast.LENGTH_SHORT).show();

                    String totalAmount = String.format("%.2f", weight * pricePerKg);

                    Intent intent = new Intent(Expressbookingactivity.this, PaymentActivity.class);
                    intent.putExtra("parcel_id", parcelId);
                    intent.putExtra("amount", totalAmount);
                    startActivity(intent);
                },
                error -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Failed to book parcel!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pickup_location", etPickupLocation.getText().toString());
                params.put("drop_location", etDropLocation.getText().toString());
                params.put("sender_name", etSenderName.getText().toString());
                params.put("sender_phone", etSenderPhone.getText().toString());
                params.put("sender_remarks", etSenderRemarks.getText().toString());
                params.put("receiver_name", etReceiverName.getText().toString());
                params.put("receiver_phone", etReceiverPhone.getText().toString());
                params.put("receiver_remarks", etReceiverRemarks.getText().toString());
                params.put("package_type", selectedSize);
                params.put("weight", String.valueOf(weight));
                params.put("amount", String.format("%.2f", weight * pricePerKg));
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                params.put("user_id", String.valueOf(userId));
                params.put("delivery_time", selectedTime);
                params.put("delivery_type", "expressdelivery");
                params.put("parcel_id", parcelId);
                params.put("distance_km", String.format(Locale.US, "%.2f", distanceKm));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}