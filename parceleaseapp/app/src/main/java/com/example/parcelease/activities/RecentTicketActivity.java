package com.example.parcelease.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parcelease.R;
import com.example.parcelease.adapters.TicketAdapter;
import com.example.parcelease.models.Ticket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RecentTicketActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Ticket> recentTickets;
    TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpcenter); // Make sure this layout exists

        recyclerView = findViewById(R.id.recyclerRecentTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recentTickets = new ArrayList<>();
        adapter = new TicketAdapter(this, recentTickets, ticket -> {
            // Handle ticket click if needed
        });
        recyclerView.setAdapter(adapter);

        fetchRecentTickets();
    }

    private void fetchRecentTickets() {
        String url = ApiConfig.getticketsUrl();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            JSONArray tickets = obj.getJSONArray("tickets");
                            List<Ticket> allTickets = new ArrayList<>();

                            for (int i = 0; i < tickets.length(); i++) {
                                JSONObject t = tickets.getJSONObject(i);

                                String id = t.getString("id");
                                String subject = t.getString("subject");
                                String message = t.getString("message");
                                String status = t.getString("status");
                                String createdAt = t.getString("created_at");
                                String responseText = t.getString("response");

                                allTickets.add(new Ticket(id, subject, message, status, createdAt, responseText));
                            }

                            // Sort by createdAt (descending)
                            Collections.sort(allTickets, (t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));

                            // Get the latest 4
                            recentTickets.clear();
                            for (int i = 0; i < Math.min(4, allTickets.size()); i++) {
                                recentTickets.add(allTickets.get(i));
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(RecentTicketActivity.this, "No tickets found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RecentTicketActivity.this, "Parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RecentTicketActivity.this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>(); // Using session for user ID
            }
        };

        queue.add(request);
    }
}
