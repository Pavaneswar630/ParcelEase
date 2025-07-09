package com.example.parcelease.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.utils.SessionManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView amountText, parcelIdText;
    private EditText utrNumberEditText;
    private ImageView qrCodeImage;
    private Button btnPayNow;

    private String parcelId;
    private String amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        // UI references
        amountText = findViewById(R.id.amountText);
        parcelIdText = findViewById(R.id.parcelidtext);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        utrNumberEditText = findViewById(R.id.utrNumberEditText);
        btnPayNow = findViewById(R.id.btn_pay_now);
        ImageButton backBtn = findViewById(R.id.btnBack);

        // Get data from intent
        parcelId = getIntent().getStringExtra("parcel_id");
        amount = getIntent().getStringExtra("amount");

        if (parcelId != null && amount != null) {
            parcelIdText.setText("Parcel ID: " + parcelId);
            amountText.setText("Amount: ₹" + amount);
            generateQRCode(amount);
        } else {
            Toast.makeText(this, "Invalid payment details!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnPayNow.setOnClickListener(v -> {
            String transactionId = utrNumberEditText.getText().toString().trim();
            if (transactionId.isEmpty()) {
                Toast.makeText(this, "Please enter the transaction ID", Toast.LENGTH_SHORT).show();
            } else {
                sendPaymentToServer(transactionId);
            }
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void generateQRCode(String amount) {
        try {
            String upiUrl = "upi://pay?pa=6305240281-2@ybl&pn=ParcelEase&am=" + amount + "&cu=INR";
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(upiUrl, BarcodeFormat.QR_CODE, 250, 250);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPaymentToServer(String transactionId) {
        String url = ApiConfig.getmakepaymentUrl();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ThankYouActivity.class);
                    intent.putExtra("parcel_id", parcelId);
                    intent.putExtra("amount", amount);
                    intent.putExtra("transaction_id", transactionId);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
                params.put("user_id", String.valueOf(userId));
                params.put("parcel_id", parcelId);
                params.put("transaction_id", transactionId);
                params.put("payment_method", "online");
                params.put("payment_status", "completed");
                params.put("amount", amount);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
