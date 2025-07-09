package com.example.parcelease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;

import org.json.JSONObject;
import android.widget.ImageButton;
import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendResetLink;
    private TextView tvSignIn;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        tvSignIn = findViewById(R.id.tvSignIn);

        // Back button functionality (updated to remove deprecation warning)
        btnBack = findViewById(R.id.ivBack);
        btnBack.setOnClickListener(v -> finish());

        // Send Reset Link Button Click Listener
        btnSendResetLink.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.length() == 0) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            sendResetLink(email);
        });

        // Navigate to Sign In screen
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendResetLink(String email) {
        String url = ApiConfig.getforgotpassUrl();
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                this::handleForgotPasswordResponse,
                error -> Toast.makeText(ForgotPasswordActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void handleForgotPasswordResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String status = json.optString("status");
            String message = json.optString("message", "Something went wrong");

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if ("success".equals(status)) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Response Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }
}
