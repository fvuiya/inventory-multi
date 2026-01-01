package com.bsoft.inventorymanager.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.ReturnableItem;
import com.bsoft.inventorymanager.models.SaleReturnItem;

import java.util.ArrayList;
import java.util.List;

public class ReturnItemsAdapter extends RecyclerView.Adapter<ReturnItemsAdapter.ViewHolder> {

    private final List<ReturnableItem> originalItems;
    private final List<SaleReturnItem> returnedItems = new ArrayList<>();

    public ReturnItemsAdapter(List<ReturnableItem> originalItems) {
        this.originalItems = originalItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReturnableItem originalItem = originalItems.get(position);
        holder.productName.setText(originalItem.getProductName());
        holder.originalQuantity.setText("Available: " + originalItem.getQuantity());

        holder.quantityToReturn.setText("0");

        holder.quantityToReturn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String qtyStr = s.toString().trim();
                if (!qtyStr.isEmpty()) {
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        if (qty < 0) {
                            holder.quantityToReturn.setText("0");
                            qty = 0;
                        } else if (qty > originalItem.getQuantity()) {
                            holder.quantityToReturn.setText(String.valueOf(originalItem.getQuantity()));
                            qty = originalItem.getQuantity();
                            Toast.makeText(holder.itemView.getContext(), 
                                "Cannot return more than available quantity: " + originalItem.getQuantity(), 
                                Toast.LENGTH_SHORT).show();
                        }
                        
                        updateReturnItem(originalItem, qty);
                    } catch (NumberFormatException e) {
                        holder.quantityToReturn.setText("0");
                    }
                } else {
                    updateReturnItem(originalItem, 0);
                }
            }
        });
    }

    private void updateReturnItem(ReturnableItem originalItem, int quantity) {
        returnedItems.removeIf(item -> item.getProductId().equals(originalItem.getProductId()));
        
        if (quantity > 0) {
            SaleReturnItem returnItem = new SaleReturnItem();
            returnItem.setProductId(originalItem.getProductId());
            returnItem.setProductName(originalItem.getProductName());
            returnItem.setQuantity(quantity);
            returnItem.setPricePerItem(originalItem.getPricePerItem());
            returnedItems.add(returnItem);
        }
    }

    @Override
    public int getItemCount() {
        return originalItems.size();
    }

    public List<SaleReturnItem> getReturnedItems() {
        return returnedItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView originalQuantity;
        EditText quantityToReturn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tv_product_name);
            originalQuantity = itemView.findViewById(R.id.tv_original_quantity);
            quantityToReturn = itemView.findViewById(R.id.et_return_quantity);
        }
    }
}
