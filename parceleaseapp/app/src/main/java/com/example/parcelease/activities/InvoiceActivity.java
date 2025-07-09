package com.example.parcelease.activities;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.view.View;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parcelease.R;


import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parcelease.R;

public class InvoiceActivity extends AppCompatActivity {
    private WebView webView;
    private static final String TAG = "InvoiceDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        webView = findViewById(R.id.webView);
        ProgressBar progressBar;

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Handle WebView errors
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loaded: " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(InvoiceActivity.this, "WebView Error: " + description, Toast.LENGTH_LONG).show();
                Log.e(TAG, "WebView error: " + description);
            }
        });

        // Get parcel ID and construct the URL
        String parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null || parcelId.isEmpty()) {
            Toast.makeText(this, "Parcel ID not found!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Parcel ID is null or empty");
            return;
        }

        String url = "http://192.168.1.7/parcelease/booking/get_invoice.php?parcel_id=" + parcelId;
        Log.d(TAG, "Loading URL: " + url);
        webView.loadUrl(url);
    }
}
