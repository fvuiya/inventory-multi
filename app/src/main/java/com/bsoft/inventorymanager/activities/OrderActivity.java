package com.bsoft.inventorymanager.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.OrderAdapter;
import com.bsoft.inventorymanager.models.Order;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private CollectionReference ordersCollection;
    // Ensure dateFormat is initialized and used consistently
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        db = FirebaseFirestore.getInstance();
        ordersCollection = db.collection("orders");

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this);
        ordersRecyclerView.setAdapter(orderAdapter);

        FloatingActionButton addOrderFab = findViewById(R.id.addOrderFab);
        addOrderFab.setOnClickListener(v -> showAddEditOrderDialog(null, -1));

        loadOrders();
    }

    private void loadOrders() {
        ordersCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                orderList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Order order = document.toObject(Order.class);
                    order.setDocumentId(document.getId());
                    orderList.add(order);
                }
                orderAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(OrderActivity.this, "Error loading orders: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditOrderDialog(final Order orderToEdit, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_order, null);
        builder.setView(dialogView);

        final EditText editTextOrderCustomerId = dialogView.findViewById(R.id.editTextOrderCustomerId);
        final EditText editTextOrderDate = dialogView.findViewById(R.id.editTextOrderDate);
        final EditText editTextOrderStatus = dialogView.findViewById(R.id.editTextOrderStatus);

        if (orderToEdit != null) {
            builder.setTitle("Edit Order");
            editTextOrderCustomerId.setText(orderToEdit.getCustomerId());
            // This is the critical part. If orderToEdit.getOrderDate() is null, format will throw an error.
            // If getOrderDate() is returning Date, this check is correct.
            if (orderToEdit.getOrderDate() != null) {
                editTextOrderDate.setText(dateFormat.format(orderToEdit.getOrderDate()));
            } else {
                editTextOrderDate.setText(""); // Or set a default if appropriate
            }
            editTextOrderStatus.setText(orderToEdit.getStatus());
        } else {
            builder.setTitle("Add New Order");
            editTextOrderDate.setText(dateFormat.format(new Date())); // Default to current date for new orders
        }

        builder.setPositiveButton(orderToEdit == null ? "Add" : "Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String customerId = editTextOrderCustomerId.getText().toString().trim();
                String dateStr = editTextOrderDate.getText().toString().trim();
                String status = editTextOrderStatus.getText().toString().trim();

                if (TextUtils.isEmpty(customerId) || TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(status)) {
                    Toast.makeText(OrderActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date parsedOrderDate; // Use Date object
                try {
                    parsedOrderDate = dateFormat.parse(dateStr);
                } catch (ParseException e) {
                    Toast.makeText(OrderActivity.this, "Invalid date format. Use YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double totalAmount = orderToEdit != null ? orderToEdit.getTotalAmount() : 0.0;

                // Construct Order object using java.util.Date
                Order order = new Order(customerId, parsedOrderDate, status, totalAmount);

                if (orderToEdit == null) { // Add new order
                    ordersCollection.add(order)
                            .addOnSuccessListener(documentReference -> {
                                order.setDocumentId(documentReference.getId());
                                orderList.add(order);
                                // Make sure adapter is notified on the main thread if there are any issues
                                // For basic cases, this is fine.
                                orderAdapter.notifyItemInserted(orderList.size() - 1);
                                ordersRecyclerView.scrollToPosition(orderList.size() - 1); // Scroll to new item
                                Toast.makeText(OrderActivity.this, "Order added.", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(OrderActivity.this, "Error adding order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else { // Update existing order
                    order.setDocumentId(orderToEdit.getDocumentId()); // Keep the original document ID
                    ordersCollection.document(orderToEdit.getDocumentId()).set(order)
                            .addOnSuccessListener(aVoid -> {
                                orderList.set(position, order);
                                orderAdapter.notifyItemChanged(position);
                                Toast.makeText(OrderActivity.this, "Order updated.", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(OrderActivity.this, "Error updating order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
        alertDialog.show();
    }

    @Override
    public void onViewDetails(int position, Order order) {
        Toast.makeText(this, "Viewing details for Order ID: " + order.getDocumentId(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, OrderDetailActivity.class);
        // intent.putExtra("ORDER_ID", order.getDocumentId());
        // startActivity(intent);
    }

    @Override
    public void onEdit(int position, Order order) {
        showAddEditOrderDialog(order, position);
    }

    @Override
    public void onDelete(int position, Order order) {
        if (order.getDocumentId() == null || order.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Cannot delete order without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete order " + order.getDocumentId() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ordersCollection.document(order.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                orderList.remove(position);
                                orderAdapter.notifyItemRemoved(position);
                                // Optional: notifyItemRangeChanged if positions below the removed item need re-binding.
                                // For simple removal, notifyItemRemoved might be enough, but this is safer.
                                orderAdapter.notifyItemRangeChanged(position, orderList.size());
                                Toast.makeText(OrderActivity.this, "Order deleted.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(OrderActivity.this, "Error deleting order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
