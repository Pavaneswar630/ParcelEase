package com.example.parcelease.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.Toast;

import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcelease.R;
import com.example.parcelease.activities.ParcelDetailsActivity;
import com.example.parcelease.activities.sampleparceldetails;
import com.example.parcelease.models.ShipmentModel;

import java.util.List;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder> {

    Context context;
    List<ShipmentModel> list;

    public ShipmentAdapter(Context context, List<ShipmentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ShipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parcel, parent, false);
        return new ShipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipmentViewHolder holder, int position) {
        ShipmentModel model = list.get(position);

        holder.tvOrderId.setText("Order ID: #" + model.orderId);
        holder.tvOrderDate.setText("Date: " + model.date);
        holder.tvOrderStatus.setText(model.status);
        holder.tvFrom.setText("From: " + model.fromAddress);
        holder.tvTo.setText("To: " + model.toAddress);
        holder.tvWeightSize.setText("Weight: " + model.weight + " kg | Size: " + model.size);
        holder.tvAmount.setText("$ " + model.amount);

        // Set booking type icon
        if (model.type != null) {
            switch (model.type.toLowerCase()) {
                case "parcel":
                    holder.imgType.setImageResource(R.drawable.img_27);
                    break;
                case "truck":
                    holder.imgType.setImageResource(R.drawable.ic_truck);
                    break;
                case "incity":
                default:
                    holder.imgType.setImageResource(R.drawable.ic_incity);
                    break;
            }
        }

        // Set status background color
        switch (model.status.toLowerCase()) {
            case "active":
            case "confirmed":
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "completed":
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "cancelled":
            case "canceled":
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                holder.tvOrderStatus.setBackgroundColor(Color.GRAY);
        }

        // On item click, open ParcelDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, sampleparceldetails.class);
            Log.d("Adapter", "Clicked parcel ID: " + model.orderId);
            Toast.makeText(context, "Parcel ID: " + model.orderId, Toast.LENGTH_SHORT).show();
            intent.putExtra("parcel_id", model.orderId);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ShipmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvFrom, tvTo, tvWeightSize, tvAmount;
        ImageView imgType;

        public ShipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvFrom = itemView.findViewById(R.id.tvFromAddress);
            tvTo = itemView.findViewById(R.id.tvToAddress);
            tvWeightSize = itemView.findViewById(R.id.tvWeightSize);
            tvAmount = itemView.findViewById(R.id.tvTotalAmount);
            imgType = itemView.findViewById(R.id.imgBookingType);
        }
    }
}
