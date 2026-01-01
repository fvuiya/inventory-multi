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

public class SlowMovingAdapter extends RecyclerView.Adapter<SlowMovingAdapter.ViewHolder> {

    private final List<Product> slowMovingProducts;

    public SlowMovingAdapter(List<Product> slowMovingProducts) {
        this.slowMovingProducts = slowMovingProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slow_moving, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = slowMovingProducts.get(position);
        holder.productName.setText(product.getName());
    }

    @Override
    public int getItemCount() {
        return slowMovingProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
        }
    }
}