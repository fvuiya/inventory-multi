package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.CustomerProfileActivity;
import com.bsoft.inventorymanager.models.Customer;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private final List<Customer> customerList;
    private final OnCustomerActionListener actionListener;

    public interface OnCustomerActionListener {
        void onEdit(int position, Customer customer);
        void onDelete(int position, Customer customer);
    }

    public CustomerAdapter(List<Customer> customerList, OnCustomerActionListener actionListener) {
        this.customerList = customerList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.textViewCustomerName.setText(customer.getName());
        holder.textViewCustomerAddress.setText(customer.getAddress());
        holder.textViewCustomerAge.setText(String.format(Locale.getDefault(), "Age: %d", customer.getAge()));
        holder.textViewCustomerPhone.setText(String.format("Phone: %s", customer.getContactNumber() != null ? customer.getContactNumber() : "N/A"));

        if (customer.getPhoto() != null && !customer.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(customer.getPhoto(), Base64.DEFAULT);
            holder.imageViewCustomer.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        } else {
            holder.imageViewCustomer.setImageResource(R.drawable.ic_customer);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CustomerProfileActivity.class);
            intent.putExtra("customer_id", customer.getDocumentId());
            context.startActivity(intent);
        });

        holder.buttonEditCustomer.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(currentPosition, customerList.get(currentPosition));
                }
            }
        });

        holder.buttonDeleteCustomer.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(currentPosition, customerList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList == null ? 0 : customerList.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCustomerName, textViewCustomerAddress, textViewCustomerAge, textViewCustomerPhone;
        ImageButton buttonEditCustomer, buttonDeleteCustomer;
        ImageView imageViewCustomer;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCustomerName = itemView.findViewById(R.id.textViewCustomerName);
            textViewCustomerAddress = itemView.findViewById(R.id.textViewCustomerAddress);
            textViewCustomerAge = itemView.findViewById(R.id.textViewCustomerAge);
            textViewCustomerPhone = itemView.findViewById(R.id.textViewCustomerPhone);
            buttonEditCustomer = itemView.findViewById(R.id.buttonEditCustomer);
            buttonDeleteCustomer = itemView.findViewById(R.id.buttonDeleteCustomer);
            imageViewCustomer = itemView.findViewById(R.id.iv_customer_image_item);
        }
    }
}
