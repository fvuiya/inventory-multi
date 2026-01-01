package com.bsoft.inventorymanager.reports.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Product;
import java.util.List;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {

    private final List<Product> lowStockProducts;

    public LowStockAdapter(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_low_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = lowStockProducts.get(position);
        holder.productName.setText(product.getName());
        holder.stockCount.setText(String.valueOf(product.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return lowStockProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView stockCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            stockCount = itemView.findViewById(R.id.stock_count);
        }
    }
}