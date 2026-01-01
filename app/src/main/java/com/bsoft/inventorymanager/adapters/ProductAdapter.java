package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Changed from ImageView
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.ProductProfileActivity;
import com.bsoft.inventorymanager.models.Product;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final List<Product> productList;
    private final OnProductActionListener actionListener;

    public interface OnProductActionListener {
        void onEdit(int position, Product product);
        void onDelete(int position, Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productNameTextView.setText(product.getName() != null && !product.getName().isEmpty() ? product.getName() : "N/A");
        holder.availableCountTextView.setText(String.format(Locale.getDefault(), "Stocks: %d", product.getQuantity()));

        // New fields
        holder.textViewProductCode.setText(product.getProductCode() != null && !product.getProductCode().isEmpty() ? "Code: " + product.getProductCode() : "Code: N/A");
        holder.textViewProductBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty() ? "Brand: " + product.getBrand() : "Brand: N/A");
        holder.textViewProductCategory.setText(product.getCategory() != null && !product.getCategory().isEmpty() ? "Category: " + product.getCategory() : "Category: N/A");
        holder.textViewProductMrp.setText(String.format(Locale.getDefault(), "MRP: %.2f", product.getMrp()));
        holder.textViewProductCostPrice.setText(String.format(Locale.getDefault(), "Cost: %.2f", product.getPurchasePrice())); // Changed to getPurchasePrice

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_product)
                        .error(R.drawable.ic_product_error)
                        .into(holder.ivProductImageItem);
            } else {
                try {
                    byte[] decodedString = Base64.decode(imageUrl, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivProductImageItem.setImageBitmap(decodedByte);
                } catch (IllegalArgumentException e) {
                    holder.ivProductImageItem.setImageResource(R.drawable.ic_product_error);
                }
            }
        } else {
            holder.ivProductImageItem.setImageResource(R.drawable.ic_product);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ProductProfileActivity.class);
            intent.putExtra("product_id", product.getDocumentId());
            context.startActivity(intent);
        });

        holder.editProductImageView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(holder.getAdapterPosition(), product);
            }
        });

        holder.deleteProductImageView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(holder.getAdapterPosition(), product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProduct(int position, Product product) {
        productList.set(position, product);
        notifyItemChanged(position);
    }

    public void removeProduct(int position) {
        productList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, availableCountTextView;
        TextView textViewProductCode, textViewProductBrand, textViewProductCategory, textViewProductMrp, textViewProductCostPrice; // New TextViews
        ImageButton editProductImageView, deleteProductImageView; // Changed from ImageView
        ImageView ivProductImageItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            availableCountTextView = itemView.findViewById(R.id.availableCountTextView);
            editProductImageView = itemView.findViewById(R.id.editProductImageView);
            deleteProductImageView = itemView.findViewById(R.id.deleteProductImageView);
            ivProductImageItem = itemView.findViewById(R.id.iv_product_image_item);

            // Initialize new TextViews
            textViewProductCode = itemView.findViewById(R.id.text_view_product_code);
            textViewProductBrand = itemView.findViewById(R.id.text_view_product_brand);
            textViewProductCategory = itemView.findViewById(R.id.text_view_product_category);
            textViewProductMrp = itemView.findViewById(R.id.text_view_product_mrp);
            textViewProductCostPrice = itemView.findViewById(R.id.text_view_product_cost_price);
        }
    }
}
