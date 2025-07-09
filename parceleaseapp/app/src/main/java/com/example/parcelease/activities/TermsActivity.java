package com.example.parcelease.activities;
 // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import com.example.parcelease.R;
import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.termsandconditions);

        ImageButton backBtn = findViewById(R.id.btnBackprofile);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TermsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}

