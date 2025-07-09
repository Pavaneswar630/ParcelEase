package com.example.parcelease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import com.example.parcelease.R;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class verifyaccountactivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private String email, receivedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verifyaccount);

        // Get email and OTP from Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        receivedOtp = intent.getStringExtra("otp");

        // Initialize Views
        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);


        // Set up the button click listener
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    // Function to verify OTP
    private void verifyOtp() {
        String inputOtp = etOtp.getText().toString().trim();

        if (inputOtp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputOtp.equals(receivedOtp)) {
            // OTP matches, now call activateAccount() method
            activateAccount(email);
        } else {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to activate the account
    private void activateAccount(String email) {
        new Thread(() -> {
            try {
                // Set the URL for activateaccount.php
                URL url = new URL(ApiConfig.getactivateaccountUrl()); // Update this URL with your actual server URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Create a map for parameters
                Map<String, String> params = new HashMap<>();
                params.put("email", email);

                // Convert parameters to URL-encoded string
                String postData = getPostDataString(params);

                // Write data to the request body
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // Get response from the server
                int responseCode = conn.getResponseCode();
                Log.d("VerifyAccount", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Handle server response
                    String responseString = response.toString();
                    Log.d("VerifyAccount", "Response: " + responseString);

                    // Handle successful response
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseString);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // Account activated successfully
                                Toast.makeText(verifyaccountactivity.this, "Account Activated Successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(verifyaccountactivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(verifyaccountactivity.this, "Failed to activate account", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            // Error in JSON parsing
                            Log.e("VerifyAccount", "Error parsing JSON: " + e.getMessage());
                            Toast.makeText(verifyaccountactivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(verifyaccountactivity.this, "Failed to activate account", Toast.LENGTH_SHORT).show());
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(verifyaccountactivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // Utility method to convert Map to URL-encoded string
    private String getPostDataString(Map<String, String> params) throws Exception {
        StringBuilder encodedParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (encodedParams.length() > 0) {
                encodedParams.append("&");
            }
            encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            encodedParams.append("=");
            encodedParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return encodedParams.toString();
    }
}
