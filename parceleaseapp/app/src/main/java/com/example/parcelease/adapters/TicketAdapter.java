package com.example.parcelease.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcelease.R;
import com.example.parcelease.models.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Ticket> ticketList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Ticket ticket);
    }

    public TicketAdapter(Context context, List<Ticket> ticketList, OnItemClickListener listener) {
        this.context = context;
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.title.setText(ticket.getSubject());
        holder.time.setText(ticket.getCreatedAt());

        // Set status dot color
        if (ticket.getStatus().equalsIgnoreCase("open")) {
            holder.statusDot.setBackgroundResource(R.drawable.status_green); // green dot
        } else if (ticket.getStatus().equalsIgnoreCase("pending")) {
            holder.statusDot.setBackgroundResource(R.drawable.status_yellow); // yellow dot
        } else {
            holder.statusDot.setBackgroundResource(R.drawable.status_red); // red dot
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        View statusDot;
        TextView title, time;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            statusDot = itemView.findViewById(R.id.viewStatusIndicator);
            title = itemView.findViewById(R.id.textSubject);
            time = itemView.findViewById(R.id.textDate);
        }
    }
}
