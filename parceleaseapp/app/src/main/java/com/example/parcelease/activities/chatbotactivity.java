package com.example.parcelease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.example.parcelease.R;

public class chatbotactivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        webView = findViewById(R.id.webViewChatbot);
        ImageButton backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());

        // Enable JavaScript for the WebView to load the chatbot properly
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Ensure links open inside the WebView
        webView.setWebViewClient(new WebViewClient());

        // Load the chatbot URL
        webView.loadUrl("https://pavaneswar630.github.io/parcelbot/applications.html");
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(chatbotactivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }
}

