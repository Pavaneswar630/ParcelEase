package com.example.parcelease.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.utils.SessionManager;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class TruckRentalActivity extends AppCompatActivity {

    private EditText tvPickupLocation, tvDropLocation, edtTotalLoad, etPurpose, additionalInfo, etName, etPhone;
    private Button btnSetPickup, btnSetDrop, btnConfirm;
    private LatLng pickupLatLng, dropLatLng;
    private int currentRequestCode;
    private AlertDialog loadingDialog;

    // Activity Result Launcher for Map Selection
    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("latitude", 0);
                    double lng = result.getData().getDoubleExtra("longitude", 0);
                    String address = result.getData().getStringExtra("address");

                    if (currentRequestCode == 1) {
                        pickupLatLng = new LatLng(lat, lng);
                        tvPickupLocation.setText(address);
                    } else if (currentRequestCode == 2) {
                        dropLatLng = new LatLng(lat, lng);
                        tvDropLocation.setText(address);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_truck);

        // Initialize Views
        tvPickupLocation = findViewById(R.id.from);
        tvDropLocation = findViewById(R.id.to);
        edtTotalLoad = findViewById(R.id.edtTotalLoad);
        etPurpose = findViewById(R.id.etpurpose);
        additionalInfo = findViewById(R.id.addtionalinfo);
        etName = findViewById(R.id.name);
        etPhone = findViewById(R.id.phone);
        btnSetPickup = findViewById(R.id.btnSetPickup);
        btnSetDrop = findViewById(R.id.btnSetDrop);
        btnConfirm = findViewById(R.id.btnConfirm);
        ImageButton backBtn = findViewById(R.id.backbut);

        btnSetPickup.setOnClickListener(v -> openMap(1));
        btnSetDrop.setOnClickListener(v -> openMap(2));
        btnConfirm.setOnClickListener(v -> confirmBooking());
        backBtn.setOnClickListener(v -> finish());
    }

    private void openMap(int requestCode) {
        currentRequestCode = requestCode;
        Intent intent = new Intent(this, MapSelectionActivity.class);
        mapLauncher.launch(intent);
    }

    private void confirmBooking() {
        if (etName.getText().toString().trim().isEmpty() || etPhone.getText().toString().trim().isEmpty() ||
                tvPickupLocation.getText().toString().trim().isEmpty() || tvDropLocation.getText().toString().trim().isEmpty() ||
                edtTotalLoad.getText().toString().trim().isEmpty() || etPurpose.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pickupLatLng == null || dropLatLng == null) {
            Toast.makeText(this, "Please select pickup and drop locations", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(); // ✅ Show loading dialog

        String url = ApiConfig.getbooktruckUrl();  // Change to your API URL

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    hideLoading(); // ✅ Hide loading when done
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideLoading();
                    Toast.makeText(this, "Booking failed! Check your connection.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                params.put("user_id", String.valueOf(userId));
                params.put("pickup_location", tvPickupLocation.getText().toString());
                params.put("pickup_lat", String.valueOf(pickupLatLng.latitude));
                params.put("pickup_lng", String.valueOf(pickupLatLng.longitude));
                params.put("drop_location", tvDropLocation.getText().toString());
                params.put("drop_lat", String.valueOf(dropLatLng.latitude));
                params.put("drop_lng", String.valueOf(dropLatLng.longitude));
                params.put("total_load", edtTotalLoad.getText().toString());
                params.put("purpose", etPurpose.getText().toString());
                params.put("name", etName.getText().toString());
                params.put("phone", etPhone.getText().toString());
                return params;
            }
        };

        queue.add(request);
    }

    // Show Loading Dialog
    private void showLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    // Hide Loading Dialog
    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
