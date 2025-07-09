package com.example.parcelease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.parcelease.R;
import com.example.parcelease.adapters.TicketAdapter;
import com.example.parcelease.models.Ticket;
import com.example.parcelease.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HelpCenterActivity extends AppCompatActivity {

    ImageButton btnBack;
    LinearLayout liveChatLayout, submitTicketLayout, myTicketsLayout, faqsLayout;
    EditText searchEditText;
    RecyclerView recentTicketsRecycler;

    TicketAdapter adapter;
    ArrayList<Ticket> ticketList = new ArrayList<>();
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpcenter);

        btnBack = findViewById(R.id.btnBack);
        liveChatLayout = findViewById(R.id.liveChatLayout);
        submitTicketLayout = findViewById(R.id.submitTicketLayout);
        myTicketsLayout = findViewById(R.id.myTicketsLayout);
        faqsLayout = findViewById(R.id.faqsLayout);
        recentTicketsRecycler = findViewById(R.id.recyclerRecentTickets);
        searchEditText = findViewById(R.id.searchEditText);

        userId = SessionManager.getInstance(this).getUserId();

        // RecyclerView setup
        recentTicketsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(this, ticketList, ticket -> {
            Toast.makeText(this, "Clicked: " + ticket.getSubject(), Toast.LENGTH_SHORT).show();
        });
        recentTicketsRecycler.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Quick Action
        liveChatLayout.setOnClickListener(v -> startActivity(new Intent(this, chatbotactivity.class)));
        submitTicketLayout.setOnClickListener(v -> startActivity(new Intent(this, SubmitTicketActivity.class)));
        myTicketsLayout.setOnClickListener(v -> startActivity(new Intent(this, MyTicketsActivity.class)));

        // Load recent tickets
        loadRecentTickets();
    }

    private void loadRecentTickets() {
        String url = ApiConfig.get_ticketsUrl();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONArray ticketsArray = new JSONArray(response);
                        ticketList.clear();

                        for (int i = 0; i < ticketsArray.length() && i < 4; i++) {
                            JSONObject ticket = ticketsArray.getJSONObject(i);
                            ticketList.add(new Ticket(
                                    ticket.getString("id"),
                                    ticket.getString("subject"),
                                    ticket.getString("message"),
                                    ticket.getString("status"),
                                    ticket.getString("created_at"),
                                    ticket.getString("response")
                            ));
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing tickets", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to load tickets: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
