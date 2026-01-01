package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Order;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onViewDetails(int position, Order order);
        void onEdit(int position, Order order);
        void onDelete(int position, Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderActionListener actionListener) {
        this.orderList = orderList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.textViewOrderId.setText(order.getDocumentId() != null ? order.getDocumentId() : "N/A");
        // For customer info, you might want to fetch customer details based on customerId
        // For now, just displaying the ID.
        holder.textViewCustomerInfo.setText(String.format("Customer ID: %s", order.getCustomerId()));

        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            holder.textViewOrderDate.setText(String.format("Date: %s", sdf.format(order.getOrderDate())));
        } else {
            holder.textViewOrderDate.setText("Date: N/A");
        }

        holder.textViewOrderStatus.setText(String.format("Status: %s", order.getStatus()));
        holder.textViewTotalAmount.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotalAmount()));

        holder.buttonViewOrderDetails.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onViewDetails(currentPosition, orderList.get(currentPosition));
                }
            }
        });

        holder.buttonEditOrder.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(currentPosition, orderList.get(currentPosition));
                }
            }
        });

        holder.buttonDeleteOrder.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(currentPosition, orderList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOrderId, textViewCustomerInfo, textViewOrderDate, textViewOrderStatus, textViewTotalAmount;
        ImageButton buttonViewOrderDetails, buttonEditOrder, buttonDeleteOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewCustomerInfo = itemView.findViewById(R.id.textViewCustomerInfo);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            buttonViewOrderDetails = itemView.findViewById(R.id.buttonViewOrderDetails);
            buttonEditOrder = itemView.findViewById(R.id.buttonEditOrder);
            buttonDeleteOrder = itemView.findViewById(R.id.buttonDeleteOrder);
        }
    }
}
