package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Purchase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.PurchaseViewHolder> {

    private List<Purchase> purchaseList;
    private Context context;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormat;
    private OnPurchaseClickListener listener;

    public interface OnPurchaseClickListener {
        void onPurchaseClick(Purchase purchase);
    }

    public PurchaseHistoryAdapter(Context context, OnPurchaseClickListener listener) {
        this.context = context;
        this.purchaseList = new ArrayList<>();
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("bn", "BD"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase_history, parent, false);
        return new PurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Purchase purchase = purchaseList.get(position);
        
        holder.tvSupplierName.setText(purchase.getSupplierName());
        holder.tvPurchaseDate.setText(dateFormat.format(purchase.getPurchaseDate().toDate()));
        holder.tvTotalAmount.setText(currencyFormatter.format(purchase.getTotalAmount()));
        holder.tvAmountPaid.setText(currencyFormatter.format(purchase.getAmountPaid()));
        holder.tvAmountDue.setText(currencyFormatter.format(purchase.getAmountDue()));
        holder.tvStatus.setText(purchase.getStatus() != null ? purchase.getStatus() : "COMPLETED");
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPurchaseClick(purchase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchaseList.size();
    }

    public void updatePurchases(List<Purchase> newPurchases) {
        this.purchaseList.clear();
        this.purchaseList.addAll(newPurchases);
        notifyDataSetChanged();
    }

    public static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvSupplierName, tvPurchaseDate, tvTotalAmount, tvAmountPaid, tvAmountDue, tvStatus;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvSupplierName = itemView.findViewById(R.id.tv_supplier_name);
            tvPurchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvAmountPaid = itemView.findViewById(R.id.tv_amount_paid);
            tvAmountDue = itemView.findViewById(R.id.tv_amount_due);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}