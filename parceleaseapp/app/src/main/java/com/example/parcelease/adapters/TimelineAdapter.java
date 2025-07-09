package com.example.parcelease.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcelease.R;
import com.example.parcelease.models.TimelineModel;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private Context context;
    private List<TimelineModel> list;

    public TimelineAdapter(Context context, List<TimelineModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timeline_activity, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineModel item = list.get(position);
        holder.status.setText(item.getStatus());
        holder.location.setText(item.getLocation());
        holder.timestamp.setText(item.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView status, location, timestamp;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.statusText);
            location = itemView.findViewById(R.id.locationText);
            timestamp = itemView.findViewById(R.id.timestampText);
        }
    }
}
