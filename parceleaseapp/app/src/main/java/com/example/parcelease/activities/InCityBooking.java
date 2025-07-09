package com.example.parcelease.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.parcelease.utils.SessionManager;
import androidx.appcompat.app.AlertDialog;


public class InCityBooking extends AppCompatActivity {

    private EditText etPickupLocation, etDropLocation, etSenderName, etSenderPhone, etReceiverName, etReceiverPhone;
    private TextView weightText, totalAmountText;
    private Button bookParcel;
    private ImageButton increaseWeight, decreaseWeight;
    ImageButton btnBack;
    private AlertDialog loadingDialog;
    String parcelId = "INCP" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);

    private int weight = 1;
    private double pricePerKg = 10.0;
    private String selectedSize = "",selectedVehicleType = "";
    private String selectedParcelType = "";
    // Package Size Layouts
    private LinearLayout sizeSmall, sizeMedium, sizeLarge;

    // Parcel Type Layouts

    //Vehicle Type Layouts
    private LinearLayout vehicleBike, vehicleVan, vehicleTruck;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incitybooking);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        // Initialize UI Elements
        etPickupLocation = findViewById(R.id.tvPickupLocation);
        etDropLocation = findViewById(R.id.tvdropLocation);
        etSenderName = findViewById(R.id.senderperu);
        etSenderPhone = findViewById(R.id.senderphno);
        etReceiverName = findViewById(R.id.recieverperu);
        etReceiverPhone = findViewById(R.id.recieverphno);
        weightText = findViewById(R.id.Weight);
        totalAmountText = findViewById(R.id.tvTotalAmount);
        ImageButton increaseWeight = findViewById(R.id.IncreaseWeight);
        ImageButton decreaseWeight = findViewById(R.id.DecreaseWeight);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        bookParcel = findViewById(R.id.btnBookNow);

        // Package Size Layouts
        sizeSmall = findViewById(R.id.btnS);
        sizeMedium = findViewById(R.id.btnM);
        sizeLarge = findViewById(R.id.btnL);

        // Vehicle Type Layouts
        vehicleBike = findViewById(R.id.btnBike);
        vehicleVan = findViewById(R.id.btnVan);
        vehicleTruck = findViewById(R.id.btnTruck);


        // Set Click Listeners for Selection
        setSelectable(sizeSmall, "Small", "package");
        setSelectable(sizeMedium, "Medium", "package");
        setSelectable(sizeLarge, "Large", "package");

        setSelectable(vehicleBike, "Bike", "vehicle");
        setSelectable(vehicleVan, "Van", "vehicle");
        setSelectable(vehicleTruck, "Truck", "vehicle");

        // Default Weight
        updateWeightAndPrice();

        // Weight Increase Button
        increaseWeight.setOnClickListener(view -> {
            weight++;
            updateWeightAndPrice();
        });

        // Weight Decrease Button
        decreaseWeight.setOnClickListener(view -> {
            if (weight > 1) {
                weight--;
                updateWeightAndPrice();
            }
        });

        // Book Parcel Button
        bookParcel.setOnClickListener(view -> {
            bookParcel(); // Call API to book parcel
            startActivity(new Intent(InCityBooking.this, PaymentActivity.class)); // Redirect to PaymentActivity
        });
    }

    // Function to Make Layouts Selectable
    private void setSelectable(LinearLayout layout, String value, String type) {
        layout.setOnClickListener(view -> {
            if (type.equals("package")) {
                resetSelection(sizeSmall, sizeMedium, sizeLarge);
                selectedSize = value;
            }
            else if (type.equals("vehicle")) {
             resetSelection(vehicleBike, vehicleVan, vehicleTruck);
             selectedVehicleType = value;
            }
            layout.setBackgroundResource(R.drawable.bg_button_selected);
        });
    }

    // Reset Unselected Layouts
    private void resetSelection(LinearLayout... layouts) {
        for (LinearLayout layout : layouts) {
            layout.setBackgroundResource(R.drawable.bg_button_unselected);
        }
    }

    // Update Weight & Price
    private void updateWeightAndPrice() {
        weightText.setText(weight + " kg");
        double total = weight * pricePerKg;
        totalAmountText.setText("Total: $" + String.format("%.2f", total));
    }

    // Function to Book Parcel via API
    private void bookParcel() {
        if (selectedSize.isEmpty() || selectedVehicleType.isEmpty() ) {
            Toast.makeText(this, "Please select package size, type", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();

        String url = ApiConfig.getcreatebookingUrl();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Parcel booked successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Extract total amount from calculation
                    String totalAmount = String.format("%.2f", weight * pricePerKg);

                    // ✅ Navigate to PaymentActivity with Parcel ID and Amount
                    Intent intent = new Intent(InCityBooking.this, PaymentActivity.class);
                    intent.putExtra("parcel_id", parcelId);
                    intent.putExtra("amount", totalAmount);
                    startActivity(intent);
                },
                error -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Failed to book parcel!", Toast.LENGTH_SHORT).show();
                })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pickup_location", etPickupLocation.getText().toString());
                params.put("drop_location", etDropLocation.getText().toString());
                params.put("sender_name", etSenderName.getText().toString());
                params.put("sender_phone", etSenderPhone.getText().toString());
                params.put("receiver_name", etReceiverName.getText().toString());
                params.put("receiver_phone", etReceiverPhone.getText().toString());
                params.put("package_type", selectedSize);
                params.put("vehicle_type", selectedVehicleType);
                params.put("weight", String.valueOf(weight));
                params.put("amount", String.format("%.2f", weight * pricePerKg));
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                params.put("user_id", String.valueOf(userId));// example
                params.put("delivery_time", "ASAP");
                params.put("parcel_id", parcelId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
