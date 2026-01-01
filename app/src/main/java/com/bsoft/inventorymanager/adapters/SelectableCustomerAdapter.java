package com.bsoft.inventorymanager.adapters;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Customer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.ArrayList;
import java.util.List;

public class SelectableCustomerAdapter extends RecyclerView.Adapter<SelectableCustomerAdapter.CustomerViewHolder> {

    private final List<Customer> customers;
    private final OnCustomerSelectedListener listener;

    public interface OnCustomerSelectedListener {
        void onCustomerSelected(Customer customer);
    }

    public SelectableCustomerAdapter(List<Customer> customers, OnCustomerSelectedListener listener) {
        this.customers = customers != null ? new ArrayList<>(customers) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_customer, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        if (position >= customers.size()) {
            return;
        }

        Customer currentCustomer = customers.get(position);

        if (currentCustomer != null) {
            holder.customerNameTextView
                    .setText(currentCustomer.getName() != null ? currentCustomer.getName() : "No Name");
            holder.customerPhoneTextView.setText(
                    currentCustomer.getContactNumber() != null ? currentCustomer.getContactNumber() : "No Phone");
            holder.customerAddressTextView
                    .setText(currentCustomer.getAddress() != null ? currentCustomer.getAddress() : "No Address");

            // Load customer image if available
            if (currentCustomer.getPhoto() != null && !currentCustomer.getPhoto().isEmpty()) {
                String imageString = currentCustomer.getPhoto();
                if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                    Glide.with(holder.itemView.getContext())
                            .load(imageString)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .placeholder(R.drawable.ic_product_error)
                            .error(R.drawable.ic_product_error)
                            .into(holder.customerImageView);
                } else {
                    try {
                        byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                        Glide.with(holder.itemView.getContext())
                                .asBitmap()
                                .load(imageBytes)
                                .transform(new CenterCrop(), new RoundedCorners(16))
                                .placeholder(R.drawable.ic_product_error)
                                .error(R.drawable.ic_product_error)
                                .into(holder.customerImageView);
                    } catch (IllegalArgumentException e) {
                        holder.customerImageView.setImageResource(R.drawable.ic_product_error);
                    }
                }
            } else {
                holder.customerImageView.setImageResource(R.drawable.ic_product_error);
            }

            // Set rating
            double rating = currentCustomer.getRating();
            if (rating > 0) {
                holder.customerRatingBar.setRating((float) rating);
                holder.customerRatingText.setText(String.format("%.1f", rating));
                holder.customerRatingBar.setVisibility(View.VISIBLE);
                holder.customerRatingText.setVisibility(View.VISIBLE);
            } else {
                holder.customerRatingBar.setVisibility(View.GONE);
                holder.customerRatingText.setVisibility(View.GONE);
            }
        } else {
            holder.customerNameTextView.setText("Unknown Customer");
            holder.customerPhoneTextView.setText("No Data");
            holder.customerAddressTextView.setText("No Address");
            holder.customerImageView.setImageResource(R.drawable.ic_product_error);
            holder.customerRatingBar.setVisibility(View.GONE);
            holder.customerRatingText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position < customers.size()) {
                listener.onCustomerSelected(customers.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return customers != null ? customers.size() : 0;
    }

    public void setCustomers(List<Customer> newCustomers) {
        this.customers.clear();
        if (newCustomers != null) {
            this.customers.addAll(newCustomers);
        }
        notifyDataSetChanged();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView customerNameTextView;
        TextView customerPhoneTextView;
        TextView customerAddressTextView;
        ImageView customerImageView;
        RatingBar customerRatingBar;
        TextView customerRatingText;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            customerNameTextView = itemView.findViewById(R.id.customer_name_text_view);
            customerPhoneTextView = itemView.findViewById(R.id.customer_phone_text_view);
            customerAddressTextView = itemView.findViewById(R.id.customer_address_text_view);
            customerImageView = itemView.findViewById(R.id.customer_image_view);
            customerRatingBar = itemView.findViewById(R.id.customer_rating_bar);
            customerRatingText = itemView.findViewById(R.id.customer_rating_text);
        }
    }
}
