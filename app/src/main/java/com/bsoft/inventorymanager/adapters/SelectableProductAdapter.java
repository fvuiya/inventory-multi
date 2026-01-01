package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Product;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectableProductAdapter extends RecyclerView.Adapter<SelectableProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private final OnProductSelectedListener listener;

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
    }

    private boolean isPurchase = false;

    public SelectableProductAdapter(List<Product> productList, OnProductSelectedListener listener) {
        this(productList, listener, false);
    }

    public SelectableProductAdapter(List<Product> productList, OnProductSelectedListener listener, boolean isPurchase) {
        this.productList = productList;
        this.listener = listener;
        this.isPurchase = isPurchase;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product currentProduct = productList.get(position);

        holder.nameTextView.setText(currentProduct.getName() != null ? currentProduct.getName() : "N/A");
        holder.brandTextView.setText(
                String.format("Brand: %s", currentProduct.getBrand() != null ? currentProduct.getBrand() : "N/A"));
        holder.categoryTextView.setText(String.format("Category: %s",
                currentProduct.getCategory() != null ? currentProduct.getCategory() : "N/A"));
        holder.stockTextView.setText(String.format(Locale.getDefault(), "Stock: %d %s", currentProduct.getQuantity(),
                currentProduct.getUnit() != null ? currentProduct.getUnit() : ""));

        // Display unit price
        double unitPrice = currentProduct.getMrp(); // Use MRP as the default unit price
        holder.priceTextView.setText(String.format(Locale.getDefault(), "₹%.2f", unitPrice));

        // Load product image if available
        if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            String imageString = currentProduct.getImageUrl();
            // Check if the image string is a URL or a Base64 string
            if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                // It's a URL, load it directly
                Glide.with(holder.itemView.getContext())
                        .load(imageString)
                        .transform(new CenterCrop(), new RoundedCorners(16))
                        .placeholder(R.drawable.ic_product_error)
                        .error(R.drawable.ic_product_error)
                        .into(holder.productImageView);
            } else {
                // It's a Base64 string, decode it first
                try {
                    byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                    Glide.with(holder.itemView.getContext())
                            .asBitmap()
                            .load(imageBytes)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .placeholder(R.drawable.ic_product_error)
                            .error(R.drawable.ic_product_error)
                            .into(holder.productImageView);
                } catch (IllegalArgumentException e) {
                    // If decoding fails, show an error image
                    holder.productImageView.setImageResource(R.drawable.ic_product_error);
                }
            }
        } else {
            holder.productImageView.setImageResource(R.drawable.ic_product_error);
        }

        // Display expiry date if applicable
        if (currentProduct.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String expiryDate = sdf.format(new Date(currentProduct.getExpiryDate().toDate().getTime()));
            holder.expiryTextView.setText(String.format("Exp: %s", expiryDate));
            holder.expiryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.expiryTextView.setVisibility(View.GONE);
        }

        // Display batch number if applicable
        if (currentProduct.getBatchNumber() != null && !currentProduct.getBatchNumber().isEmpty()) {
            holder.batchTextView.setText(String.format("Batch: %s", currentProduct.getBatchNumber()));
            holder.batchTextView.setVisibility(View.VISIBLE);
        } else {
            holder.batchTextView.setVisibility(View.GONE);
        }

        // Check stock and disable if necessary, BUT allow if it is a Purchase
        // interaction
        if (currentProduct.getQuantity() <= 0) {
            holder.stockTextView.setTextColor(android.graphics.Color.RED);
            holder.stockTextView.setText(String.format(Locale.getDefault(), "Stock: 0 %s (Out of Stock)",
                    currentProduct.getUnit() != null ? currentProduct.getUnit() : ""));

            if (!isPurchase) {
                holder.itemView.setEnabled(false);
                holder.itemView.setAlpha(0.5f); // Gray out
                holder.itemView.setOnClickListener(null); // Disable click
            } else {
                // For purchase, visually indicate low stock but allow interaction
                holder.itemView.setEnabled(true);
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductSelected(currentProduct);
                    }
                });
            }
        } else {
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
            holder.stockTextView.setTextColor(android.graphics.Color.BLACK); // Safe fallback (or theme color)

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductSelected(currentProduct);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged(); // Consider DiffUtil for better performance
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView brandTextView;
        TextView categoryTextView;
        TextView stockTextView;
        TextView priceTextView;
        TextView expiryTextView;
        TextView batchTextView;
        ImageView productImageView;

        ProductViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.product_name_text_view);
            brandTextView = view.findViewById(R.id.product_brand_text_view);
            categoryTextView = view.findViewById(R.id.product_category_text_view);
            stockTextView = view.findViewById(R.id.product_stock_text_view);
            priceTextView = view.findViewById(R.id.product_price_text_view);
            expiryTextView = view.findViewById(R.id.product_expiry_text_view);
            batchTextView = view.findViewById(R.id.product_batch_text_view);
            productImageView = view.findViewById(R.id.product_image_view);
        }
    }
}
