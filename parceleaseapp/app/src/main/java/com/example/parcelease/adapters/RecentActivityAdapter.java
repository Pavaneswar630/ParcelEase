package com.example.parcelease.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcelease.R;
import com.example.parcelease.models.ActivityItem;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {

    private final Context context;
    private final List<ActivityItem> activityList;

    public RecentActivityAdapter(Context context, List<ActivityItem> activityList) {
        this.context = context;
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = activityList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvId.setText(item.getShipmentId());
        holder.tvTimestamp.setText(item.getTimestamp());

        int iconRes;
        switch (item.getType()) {
            case "truck":
                iconRes = R.drawable.ic_truck;
                break;
            case "incity":
                iconRes = R.drawable.ic_incity;
                break;
            default:
                iconRes = R.drawable.img_27;
                break;
        }
        holder.ivIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvId, tvTimestamp;
        ImageView ivIcon;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvId = itemView.findViewById(R.id.tvId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
