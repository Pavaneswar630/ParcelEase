package com.example.parcelease.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class ParcelBookingActivity extends AppCompatActivity {

    private EditText etPickupLocation, etDropLocation, etSenderName, etSenderPhone, etSenderRemarks, etReceiverName, etReceiverPhone, etReceiverRemarks;
    private TextView weightText, totalAmountText;
    private TextView tvSelectSavedAddress;
    private Button bookParcel;
    private int weight = 1;
    private double pricePerKg = 10.0;
    private String selectedSize = "";
    private String selectedParcelType = "";
    private float distanceKm = 0f;

    private LinearLayout sizeSmall, sizeMedium, sizeLarge;
    private LinearLayout typeBook, typeGoods, typeCosmetics, typeElectronics, typeMedicine, typeComputer;
    private AlertDialog loadingDialog;
    private String parcelId = "P" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);

    private ActivityResultLauncher<Intent> pickupLocationLauncher;
    private Switch switchSaveReceiver;
    private ActivityResultLauncher<Intent> dropLocationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcel_booking);

        etPickupLocation = findViewById(R.id.etSenderLocation);
        etDropLocation = findViewById(R.id.etRecieverLocation);
        etSenderName = findViewById(R.id.etSenderName);
        etSenderPhone = findViewById(R.id.etSenderPhone);
        etSenderRemarks = findViewById(R.id.etSenderRemarks);
        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etReceiverRemarks = findViewById(R.id.etReceiverRemarks);
        weightText = findViewById(R.id.tvWeight);
        totalAmountText = findViewById(R.id.tvTotalAmount);
        tvSelectSavedAddress = findViewById(R.id.tvSelectSavedAddress);
        bookParcel = findViewById(R.id.btnProcessNext);
        ImageView imgPickup = findViewById(R.id.senderlocation);
        ImageView imgDrop = findViewById(R.id.recieverlocation);
        switchSaveReceiver = findViewById(R.id.switchSaveReceiver);

        sizeSmall = findViewById(R.id.btnSizeS);
        sizeMedium = findViewById(R.id.btnSizeM);
        sizeLarge = findViewById(R.id.btnSizeL);
        typeBook = findViewById(R.id.typeBook);
        typeGoods = findViewById(R.id.typeGoods);
        typeCosmetics = findViewById(R.id.typeCosmetics);
        typeElectronics = findViewById(R.id.typeElectronics);
        typeMedicine = findViewById(R.id.typeMedicine);
        typeComputer = findViewById(R.id.typeComputer);

        ImageButton increaseWeight = findViewById(R.id.btnIncreaseWeight);
        ImageButton decreaseWeight = findViewById(R.id.btnDecreaseWeight);
        ImageButton backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());

        increaseWeight.setOnClickListener(view -> {
            weight++;
            updateWeightAndPrice();
        });
        decreaseWeight.setOnClickListener(view -> {
            if (weight > 1) {
                weight--;
                updateWeightAndPrice();
            }
        });

        setSelectable(sizeSmall, "Small", "package");
        setSelectable(sizeMedium, "Medium", "package");
        setSelectable(sizeLarge, "Large", "package");
        setSelectable(typeBook, "Book", "parcel");
        setSelectable(typeGoods, "Goods", "parcel");
        setSelectable(typeCosmetics, "Cosmetics", "parcel");
        setSelectable(typeElectronics, "Electronics", "parcel");
        setSelectable(typeMedicine, "Medicine", "parcel");
        setSelectable(typeComputer, "Computer", "parcel");

        updateWeightAndPrice();

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

        imgPickup.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapSelectionActivity.class);
            intent.putExtra("selection_type", "pickup");
            pickupLocationLauncher.launch(intent);
        });

        imgDrop.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapSelectionActivity.class);
            intent.putExtra("selection_type", "drop");
            dropLocationLauncher.launch(intent);
        });

        tvSelectSavedAddress.setOnClickListener(v -> fetchSavedAddressesAndShowDialog());

        bookParcel.setOnClickListener(view -> bookParcel());
    }

    private void setSelectable(LinearLayout layout, String value, String type) {
        layout.setOnClickListener(view -> {
            if ("package".equals(type)) {
                resetSelection(sizeSmall, sizeMedium, sizeLarge);
                selectedSize = value;
            } else if ("parcel".equals(type)) {
                resetSelection(typeBook, typeGoods, typeCosmetics, typeElectronics, typeMedicine, typeComputer);
                selectedParcelType = value;
            }
            layout.setBackgroundResource(R.drawable.bg_button_selected);
        });
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

    private void bookParcel() {
        if (selectedSize.isEmpty() || selectedParcelType.isEmpty()) {
            Toast.makeText(this, "Please select package size and type", Toast.LENGTH_SHORT).show();
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
                    Intent intent = new Intent(this, PaymentActivity.class);
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
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();

                params.put("pickup_location", pickup);
                params.put("drop_location", drop);
                params.put("sender_name", etSenderName.getText().toString());
                params.put("sender_phone", etSenderPhone.getText().toString());
                params.put("sender_remarks", etSenderRemarks.getText().toString());
                params.put("receiver_name", etReceiverName.getText().toString());
                params.put("receiver_phone", etReceiverPhone.getText().toString());
                params.put("receiver_remarks", etReceiverRemarks.getText().toString());
                params.put("package_type", selectedSize);
                params.put("weight", String.valueOf(weight));
                params.put("amount", String.format("%.2f", weight * pricePerKg));
                params.put("user_id", String.valueOf(userId));
                params.put("delivery_time", "ASAP");
                params.put("delivery_type", "normal");
                params.put("distance_km", String.format(Locale.US, "%.2f", distanceKm));
                params.put("parcel_id", parcelId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setView(R.layout.dialog_loading);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
