package com.example.parcelease.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.example.parcelease.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThankYouActivity extends AppCompatActivity {

    private TextView tvMessage, tvDetails;
    private Button btnInvoice, btnHome;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        tvMessage = findViewById(R.id.tvMessage);
        tvDetails = findViewById(R.id.tvDetails);
        btnInvoice = findViewById(R.id.btnInvoice);
        btnHome = findViewById(R.id.btnHome);

        String parcelId = getIntent().getStringExtra("parcel_id");
        String amount = getIntent().getStringExtra("amount");
        String txnId = getIntent().getStringExtra("transaction_id");

        tvDetails.setText("Parcel ID: #" + parcelId + "\nAmount: ₹" + amount + "\nTxn ID: " + txnId);

        // Success sound
        mediaPlayer = MediaPlayer.create(this, R.raw.sucess_sound);
        mediaPlayer.start();

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ThankYouActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


        btnInvoice.setOnClickListener(v -> {
            Log.d("InvoiceDebug", "Parcel ID sent: " + parcelId);
            Intent intent = new Intent(ThankYouActivity.this, InvoiceActivity.class);
            intent.putExtra("parcel_id", parcelId); // Make sure parcelId is NOT null
            startActivity(intent);
        });
        sendInvoiceEmail(parcelId);
    }
    // When landing on ThankYouActivity (after payment), call:
    private void sendInvoiceEmail(String parcelId) {
        StringRequest request = new StringRequest(Request.Method.POST,
                "http://192.168.222.44/parcelease/booking/get_invoice.php",
                response -> Log.d("Invoice", "Sent: " + response),
                error -> Log.e("Invoice", "Failed: " + error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("parcel_id", parcelId);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }


    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
