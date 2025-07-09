package com.example.parcelease.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import android.widget.LinearLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.adapters.RecentActivityAdapter;
import com.example.parcelease.models.ActivityItem;
import com.example.parcelease.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private TextView tvUserName;
    private ImageView ivProfile;
    private LinearLayout bookt, trackt, supportt, profilecard;
    private EditText etSearch;
    private RecyclerView recyclerRecentActivities;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextToSpeech textToSpeech;
    private ImageButton voiceBtn;

    private RecentActivityAdapter adapter;
    private List<ActivityItem> activityList;

    private View btnbookparcel, btnHelpCenter, btnInCity, btnHistory, btntrack, btnTruck;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        checkAudioPermission();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_track) {
                startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(HomeActivity.this, MyShipmentsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fab);

        // Bind Views
        bookt = findViewById(R.id.bookt);
        trackt = findViewById(R.id.trackt);
        supportt = findViewById(R.id.supportt);
        profilecard = findViewById(R.id.profilecard);
        tvUserName = findViewById(R.id.tvUserName);
        ivProfile = findViewById(R.id.ivProfile);
        recyclerRecentActivities = findViewById(R.id.recyclerRecentActivities);
        voiceBtn = findViewById(R.id.voiceAssistantBtn);

        btnbookparcel = findViewById(R.id.bookparcel);
        btnHelpCenter = findViewById(R.id.support);
        btnInCity = findViewById(R.id.incity);
        btnHistory = findViewById(R.id.expressdelivery);
        btntrack = findViewById(R.id.track);
        btnTruck = findViewById(R.id.truck);

        // Username
        String userName = SessionManager.getInstance(getApplicationContext()).getUserName();
        tvUserName.setText((userName != null && !userName.isEmpty()) ? userName : "Guest");

        // RecyclerView setup
        activityList = new ArrayList<>();
        adapter = new RecentActivityAdapter(this, activityList);
        recyclerRecentActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentActivities.setAdapter(adapter);
        loadRecentActivities();

        // TTS
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Track if wake word was said

        voiceBtn.setOnClickListener(v -> {
            textToSpeech.speak("How can I help you?", TextToSpeech.QUEUE_FLUSH, null, null);
            voiceBtn.setEnabled(false);
            speechRecognizer.startListening(speechRecognizerIntent);

            // Re-enable button after 2 sec
            voiceBtn.postDelayed(() -> voiceBtn.setEnabled(true), 2000);
        });




        voiceBtn.setOnClickListener(v -> {
            textToSpeech.speak("How can I help you?", TextToSpeech.QUEUE_FLUSH, null, null);
            voiceBtn.setEnabled(false);
            speechRecognizer.startListening(speechRecognizerIntent);

            // Debounce: re-enable button after 2 sec
            voiceBtn.postDelayed(() -> voiceBtn.setEnabled(true), 2000);
        });
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase();

                    // Debug: show what was recognized
                    Toast.makeText(HomeActivity.this, "Heard: " + command, Toast.LENGTH_SHORT).show();

                    if (command.contains("book a parcel") || command.contains("book parcel")) {
                        // Speak message before opening ParcelBookingActivity
                        if (textToSpeech != null) {
                            textToSpeech.speak("Please enter sender and receiver details to book a parcel", TextToSpeech.QUEUE_FLUSH, null, null);
                        }

                        // Delay the activity start slightly to allow the speech to play first
                        new android.os.Handler().postDelayed(() -> {
                            startActivity(new Intent(HomeActivity.this, ParcelBookingActivity.class));
                        }, 2000); // 2 seconds delay
                    }
                    else if (command.contains("track") || command.contains("tracking")) {
                        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
                    }   else if (command.contains("profile")) {
                        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    } else if (command.contains("support") || command.contains("help")) {
                        startActivity(new Intent(HomeActivity.this, HelpCenterActivity.class));
                    } else {
                        textToSpeech.speak("Sorry, I didn't understand. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }

            // Required overrides with empty bodies
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                Toast.makeText(HomeActivity.this, "Speech error. Try again.", Toast.LENGTH_SHORT).show();
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });



        // Click listeners
        btnbookparcel.setOnClickListener(v -> startActivity(new Intent(this, ParcelBookingActivity.class)));
        btnHelpCenter.setOnClickListener(v -> startActivity(new Intent(this, HelpCenterActivity.class)));
        btnInCity.setOnClickListener(v -> startActivity(new Intent(this, InCityBooking.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, Expressbookingactivity.class)));
        btntrack.setOnClickListener(v -> startActivity(new Intent(this, TrackingActivity.class)));
        btnTruck.setOnClickListener(v -> startActivity(new Intent(this, TruckRentalActivity.class)));
        bookt.setOnClickListener(v -> startActivity(new Intent(this, ParcelBookingActivity.class)));
        trackt.setOnClickListener(v -> startActivity(new Intent(this, TrackingActivity.class)));
        supportt.setOnClickListener(v -> startActivity(new Intent(this, HelpCenterActivity.class)));
        profilecard.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        fab.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ParcelBookingActivity.class)));
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    private void loadRecentActivities() {
        String url = ApiConfig.getrecentactivitiesUrl();

        JSONObject params = new JSONObject();
        int userId = SessionManager.getInstance(getApplicationContext()).getUserId();
        Log.d("USER_ID", "User ID: " + userId);
        Toast.makeText(this, "User ID: " + userId, Toast.LENGTH_SHORT).show();

        try {
            params.put("user_id", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    Log.d("API_RESPONSE", response.toString());
                    activityList.clear();
                    try {
                        if (!response.has("activities")) {
                            Toast.makeText(HomeActivity.this, "No recent activities", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        JSONArray activities = response.getJSONArray("activities");
                        for (int i = 0; i < activities.length(); i++) {
                            JSONObject obj = activities.getJSONObject(i);
                            String title = obj.getString("title");
                            String shipmentId = obj.getString("shipment_id");
                            String timestamp = obj.getString("timestamp");
                            String type = obj.getString("type");

                            activityList.add(new ActivityItem(title, shipmentId, timestamp, type));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("API_ERROR", "Failed to load activities: " + error.toString());
                    Toast.makeText(HomeActivity.this, "Failed to load activities", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
