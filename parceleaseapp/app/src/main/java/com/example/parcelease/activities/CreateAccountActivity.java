package com.example.parcelease.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private ImageView imgProfile, ivAddProfile;
    private Button btnCreateAccount;
    private Bitmap selectedBitmap = null;
    private AlertDialog progressDialog;
    ImageButton btnBack;

    private static final String REGISTER_URL = ApiConfig.getRegisterUrl();
    private static final String SEND_OTP_URL = ApiConfig.getSendOtpUrl();

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account); // Make sure your layout has ivAddProfile

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        imgProfile = findViewById(R.id.imgProfile);
        ivAddProfile = findViewById(R.id.ivAddProfile);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBack = findViewById(R.id.ivBack);
        btnBack.setOnClickListener(v -> finish());

        // Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imgProfile.setImageURI(imageUri);
                        selectedBitmap = ((BitmapDrawable) imgProfile.getDrawable()).getBitmap();
                    }
                }
        );

        // Set Click on Overlay Icon
        ivAddProfile.setOnClickListener(v -> pickImage());

        // Register Button Click
        btnCreateAccount.setOnClickListener(view -> registerUser());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String profileImage = selectedBitmap != null ? encodeImageToBase64(selectedBitmap) : "";

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog();

        StringRequest registerRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                response -> {
                    hideProgressDialog();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            String otp = generateOtp();
                            sendOtp(email, otp);
                        } else {
                            Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        hideProgressDialog();
                        e.printStackTrace();
                        Toast.makeText(CreateAccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    hideProgressDialog();
                    Toast.makeText(CreateAccountActivity.this, "Registration Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                params.put("profile_image", profileImage); // Optional, only if you use it in backend
                params.put("status", "pending");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(registerRequest);
    }

    private void sendOtp(String email, String otp) {
        showProgressDialog();

        StringRequest otpRequest = new StringRequest(Request.Method.POST, SEND_OTP_URL,
                response -> {
                    hideProgressDialog();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Intent intent = new Intent(CreateAccountActivity.this, verifyaccountactivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("otp", otp);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Failed to send OTP: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        hideProgressDialog();
                        e.printStackTrace();
                        Toast.makeText(CreateAccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    hideProgressDialog();
                    Toast.makeText(CreateAccountActivity.this, "OTP Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(otpRequest);
    }
}
