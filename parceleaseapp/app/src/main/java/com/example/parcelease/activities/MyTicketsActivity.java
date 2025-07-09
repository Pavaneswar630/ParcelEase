package com.example.parcelease.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.ImageButton;

public class MyTicketsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Ticket> ticketList;
    ImageButton btnBack;
    TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_tickets);
        btnBack = findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();

        adapter = new TicketAdapter(this, ticketList, ticket -> {
            // Show response in dialog
            new AlertDialog.Builder(this)
                    .setTitle(ticket.getSubject())
                    .setMessage("Message: " + ticket.getMessage() + "\n\nResponse: " + ticket.getResponse())
                    .setPositiveButton("OK", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        fetchTickets();
    }

    private void fetchTickets() {
        String url = ApiConfig.getticketsUrl();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            JSONArray tickets = obj.getJSONArray("tickets");
                            for (int i = 0; i < tickets.length(); i++) {
                                JSONObject t = tickets.getJSONObject(i);
                                ticketList.add(new Ticket(
                                        t.getString("id"),
                                        t.getString("title"),
                                        t.getString("message"),
                                        t.getString("status"),
                                        t.getString("created_at"),
                                        t.getString("response")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No tickets found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>(); // Using session-based login
            }
        };

        queue.add(request);
    }
}
