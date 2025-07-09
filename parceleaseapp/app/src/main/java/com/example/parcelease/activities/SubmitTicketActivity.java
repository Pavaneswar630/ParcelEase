package com.example.parcelease.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class SubmitTicketActivity extends AppCompatActivity {

    EditText editTitle, editMessage;
    Button btnSubmit;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submitticket);

        editTitle = findViewById(R.id.editTextSubject);
        editMessage = findViewById(R.id.editTextMessage);
        btnSubmit = findViewById(R.id.btnSubmitTicket);
        btnBack = findViewById(R.id.btnBacksubmit);
        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(view -> submitTicket());
    }

    private void submitTicket() {
        String title = editTitle.getText().toString().trim();
        String message = editMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.getsubmitticketUrl();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Ticket submitted successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Close the screen
                },
                error -> {
                    Toast.makeText(this, "Submission failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("title", title);
                params.put("message", message);
                return params;
            }
        };

        queue.add(request);
    }
}
