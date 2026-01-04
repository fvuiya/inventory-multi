package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
// [KMP MIGRATION] Use shared Purchase model
import com.bsoft.inventorymanager.model.Purchase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    private List<Purchase> purchaseList;
    private final OnPurchaseActionListener actionListener;
    private final OnPurchaseItemClickListener itemClickListener;
    private final Context context;

    public interface OnPurchaseActionListener {
        void onEdit(Purchase purchase);

        void onDelete(Purchase purchase);
    }

    public interface OnPurchaseItemClickListener {
        void onItemClick(Purchase purchase);
    }

    public PurchaseAdapter(Context context, List<Purchase> purchaseList, OnPurchaseActionListener actionListener,
            OnPurchaseItemClickListener itemClickListener) {
        this.context = context;
        this.purchaseList = purchaseList;
        this.actionListener = actionListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase, parent, false);
        return new PurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Purchase purchase = purchaseList.get(position);

        holder.textViewPurchaseSupplierName.setText(purchase.getSupplierName());
        holder.textViewPurchaseSupplierAddress.setText(purchase.getSupplierContactNumber());

        // [KMP MIGRATION] purchaseDate is now Long (millis), not Timestamp
        long purchaseDateMillis = purchase.getPurchaseDate();
        if (purchaseDateMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
            holder.textViewPurchaseDateTime.setText(sdf.format(new Date(purchaseDateMillis)));
        } else {
            holder.textViewPurchaseDateTime.setText("N/A");
        }

        Locale bdtLocale = new Locale("bn", "BD");
        NumberFormat bdtFormat = NumberFormat.getCurrencyInstance(bdtLocale);

        holder.textViewPurchaseTotalBill
                .setText(String.format("Total: %s", bdtFormat.format(purchase.getTotalAmount())));
        holder.textViewPurchaseAmountPaid
                .setText(String.format("Paid: %s", bdtFormat.format(purchase.getAmountPaid())));
        holder.textViewPurchaseAmountDue.setText(String.format("Due: %s", bdtFormat.format(purchase.getAmountDue())));

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(purchaseList.get(currentPosition));
                }
            }
        });

        holder.buttonEditPurchase.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(purchaseList.get(currentPosition));
                }
            }
        });

        holder.buttonDeletePurchase.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(purchaseList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchaseList == null ? 0 : purchaseList.size();
    }

    public void setPurchases(List<Purchase> newPurchasesList) {
        this.purchaseList = newPurchasesList;
        notifyDataSetChanged();
    }

    static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPurchaseSupplierName, textViewPurchaseSupplierAddress, textViewPurchaseDateTime;
        TextView textViewPurchaseTotalBill, textViewPurchaseAmountPaid, textViewPurchaseAmountDue;
        ImageButton buttonEditPurchase, buttonDeletePurchase;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPurchaseSupplierName = itemView.findViewById(R.id.textViewPurchaseSupplierName);
            textViewPurchaseSupplierAddress = itemView.findViewById(R.id.textViewPurchaseSupplierAddress);
            textViewPurchaseDateTime = itemView.findViewById(R.id.textViewPurchaseDateTime);
            textViewPurchaseTotalBill = itemView.findViewById(R.id.textViewPurchaseTotalBill);
            textViewPurchaseAmountPaid = itemView.findViewById(R.id.textViewPurchaseAmountPaid);
            textViewPurchaseAmountDue = itemView.findViewById(R.id.textViewPurchaseAmountDue);
            buttonEditPurchase = itemView.findViewById(R.id.buttonEditPurchase);
            buttonDeletePurchase = itemView.findViewById(R.id.buttonDeletePurchase);
        }
    }
}
