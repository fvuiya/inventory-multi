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
import com.bsoft.inventorymanager.model.Supplier;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.ArrayList;
import java.util.List;

public class SelectableSupplierAdapter extends RecyclerView.Adapter<SelectableSupplierAdapter.SupplierViewHolder> {

    private List<Supplier> suppliers;
    private final OnSupplierSelectedListener listener;

    public interface OnSupplierSelectedListener {
        void onSupplierSelected(Supplier supplier);
    }

    public SelectableSupplierAdapter(List<Supplier> suppliers, OnSupplierSelectedListener listener) {
        this.suppliers = suppliers != null ? new ArrayList<>(suppliers) : new ArrayList<>();
        this.listener = listener;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_supplier, parent, false);
        return new SupplierViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        if (position >= suppliers.size()) {
            return;
        }

        Supplier currentSupplier = suppliers.get(position);

        if (currentSupplier != null) {
            holder.supplierNameTextView
                    .setText(currentSupplier.getName() != null ? currentSupplier.getName() : "No Name");
            holder.supplierPhoneTextView.setText(
                    currentSupplier.getContactNumber() != null ? currentSupplier.getContactNumber() : "No Phone");
            holder.supplierAddressTextView
                    .setText(currentSupplier.getAddress() != null ? currentSupplier.getAddress() : "No Address");

            // Load supplier image if available
            if (currentSupplier.getPhoto() != null && !currentSupplier.getPhoto().isEmpty()) {
                String imageString = currentSupplier.getPhoto();
                if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                    Glide.with(holder.itemView.getContext())
                            .load(imageString)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .placeholder(R.drawable.ic_product_error)
                            .error(R.drawable.ic_product_error)
                            .into(holder.supplierImageView);
                } else {
                    try {
                        byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                        Glide.with(holder.itemView.getContext())
                                .asBitmap()
                                .load(imageBytes)
                                .transform(new CenterCrop(), new RoundedCorners(16))
                                .placeholder(R.drawable.ic_product_error)
                                .error(R.drawable.ic_product_error)
                                .into(holder.supplierImageView);
                    } catch (IllegalArgumentException e) {
                        holder.supplierImageView.setImageResource(R.drawable.ic_product_error);
                    }
                }
            } else {
                holder.supplierImageView.setImageResource(R.drawable.ic_product_error);
            }

            // Set rating
            double rating = currentSupplier.getRating();
            if (rating > 0) {
                holder.supplierRatingBar.setRating((float) rating);
                holder.supplierRatingText.setText(String.format("%.1f", rating));
                holder.supplierRatingBar.setVisibility(View.VISIBLE);
                holder.supplierRatingText.setVisibility(View.VISIBLE);
            } else {
                holder.supplierRatingBar.setVisibility(View.GONE);
                holder.supplierRatingText.setVisibility(View.GONE);
            }
        } else {
            holder.supplierNameTextView.setText("Unknown Supplier");
            holder.supplierPhoneTextView.setText("No Data");
            holder.supplierAddressTextView.setText("No Address");
            holder.supplierImageView.setImageResource(R.drawable.ic_product_error);
            holder.supplierRatingBar.setVisibility(View.GONE);
            holder.supplierRatingText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position < suppliers.size()) {
                listener.onSupplierSelected(suppliers.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return suppliers != null ? suppliers.size() : 0;
    }

    static class SupplierViewHolder extends RecyclerView.ViewHolder {
        TextView supplierNameTextView;
        TextView supplierPhoneTextView;
        TextView supplierAddressTextView;
        ImageView supplierImageView;
        RatingBar supplierRatingBar;
        TextView supplierRatingText;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            supplierNameTextView = itemView.findViewById(R.id.supplier_name_text_view);
            supplierPhoneTextView = itemView.findViewById(R.id.supplier_phone_text_view);
            supplierAddressTextView = itemView.findViewById(R.id.supplier_address_text_view);
            supplierImageView = itemView.findViewById(R.id.supplier_image_view);
            supplierRatingBar = itemView.findViewById(R.id.supplier_rating_bar);
            supplierRatingText = itemView.findViewById(R.id.supplier_rating_text);
        }
    }
}
