package com.example.parcelease.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.chip.Chip;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.adapters.TimelineAdapter;
import com.example.parcelease.models.TimelineModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class sampleparceldetails extends AppCompatActivity {

    TextView trackingNumberText, weightText, senderName, senderPhone, senderAddress;
    TextView receiverName, receiverPhone, receiverAddress;
    ImageView qrCodeImage;

    RecyclerView timelineRecycler;
    TimelineAdapter timelineAdapter;
    ArrayList<TimelineModel> timelineList;

    String parcelId;
    Chip statusChip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailssample);

        // Get parcel ID from intent
        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) {
            Toast.makeText(this, "Parcel ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        trackingNumberText = findViewById(R.id.trackingNumberText);
        weightText = findViewById(R.id.weightText);
        senderName = findViewById(R.id.senderName);
        senderPhone = findViewById(R.id.senderPhone);
        senderAddress = findViewById(R.id.senderAddress);
        receiverName = findViewById(R.id.receiverName);
        receiverPhone = findViewById(R.id.receiverPhone);
        receiverAddress = findViewById(R.id.receiverAddress);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        statusChip = findViewById(R.id.statusChip);



        // Setup RecyclerView
        timelineRecycler = findViewById(R.id.timelineRecyclerView);
        timelineRecycler.setLayoutManager(new LinearLayoutManager(this));
        timelineList = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(this, timelineList);
        timelineRecycler.setAdapter(timelineAdapter);

        // Load parcel and timeline data
        fetchParcelDetails();
    }

    private void fetchParcelDetails() {
        String url = ApiConfig.getdetailsUrl(); // Change to your real URL

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        JSONObject parcel = obj.getJSONObject("parcel");

                        trackingNumberText.setText(parcel.getString("parcel_id"));
                        weightText.setText(parcel.getString("weight") + " kg");

                        JSONObject sender = obj.getJSONObject("sender");
                        senderName.setText(sender.getString("name"));
                        senderPhone.setText(sender.getString("phone"));
                        senderAddress.setText(sender.getString("address"));

                        JSONObject receiver = obj.getJSONObject("receiver");
                        receiverName.setText(receiver.getString("name"));
                        receiverPhone.setText(receiver.getString("phone"));
                        receiverAddress.setText(receiver.getString("address"));
                        String status = obj.optString("status", "Unknown");
                        statusChip.setText(status);


                        // Generate QR code with parcel ID
                        Bitmap qrBitmap = generateQRCode(parcelId);
                        if (qrBitmap != null) {
                            qrCodeImage.setImageBitmap(qrBitmap);
                        }

                        // Load timeline
                        JSONArray timelineArray = obj.getJSONArray("timeline");
                        timelineList.clear(); // Clear existing
                        for (int i = 0; i < timelineArray.length(); i++) {
                            JSONObject entry = timelineArray.getJSONObject(i);
                            timelineList.add(new TimelineModel(
                                    entry.getString("status"),
                                    entry.getString("location"),
                                    entry.getString("timestamp")
                            ));
                        }
                        timelineAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing parcel details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load parcel details", Toast.LENGTH_SHORT).show();
                    Log.e("VolleyError", error.toString());
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("parcel_id", parcelId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private Bitmap generateQRCode(String data) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
