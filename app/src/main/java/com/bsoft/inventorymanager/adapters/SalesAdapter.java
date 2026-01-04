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
// [KMP MIGRATION] Use shared Sale model
import com.bsoft.inventorymanager.model.Sale;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SaleViewHolder> {

    private List<Sale> saleList;
    private OnSaleActionListener actionListener;
    private OnSaleItemClickListener itemClickListener;
    private Context context;

    public interface OnSaleActionListener {
        void onEdit(Sale sale);

        void onDelete(Sale sale);
    }

    public interface OnSaleItemClickListener {
        void onItemClick(Sale sale);
    }

    public SalesAdapter(Context context, List<Sale> saleList, OnSaleActionListener actionListener,
            OnSaleItemClickListener itemClickListener) {
        this.context = context;
        this.saleList = saleList;
        this.actionListener = actionListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Sale sale = saleList.get(position);

        holder.textViewSaleCustomerName.setText(sale.getCustomerName());
        holder.textViewSaleCustomerAddress.setText(sale.getCustomerPhoneNumber());

        // [KMP MIGRATION] saleDate is now Long (millis), not Timestamp
        long saleDateMillis = sale.getSaleDate();
        if (saleDateMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
            holder.textViewSaleDateTime.setText(sdf.format(new Date(saleDateMillis)));
        } else {
            holder.textViewSaleDateTime.setText("N/A");
        }

        Locale bdtLocale = new Locale("bn", "BD");
        NumberFormat bdtFormat = NumberFormat.getCurrencyInstance(bdtLocale);

        // [KMP MIGRATION] Use getTotalAmount() instead of getTotalBill()
        holder.textViewSaleTotalBill.setText(String.format("Total: %s", bdtFormat.format(sale.getTotalAmount())));
        holder.textViewSaleAmountPaid.setText(String.format("Paid: %s", bdtFormat.format(sale.getAmountPaid())));
        holder.textViewSaleAmountDue.setText(String.format("Due: %s", bdtFormat.format(sale.getAmountDue())));

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(saleList.get(currentPosition));
                }
            }
        });

        holder.buttonEditSale.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(saleList.get(currentPosition));
                }
            }
        });

        holder.buttonDeleteSale.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(saleList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return saleList == null ? 0 : saleList.size();
    }

    public void setSales(List<Sale> newSalesList) {
        this.saleList = newSalesList;
        notifyDataSetChanged();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSaleCustomerName, textViewSaleCustomerAddress, textViewSaleDateTime;
        TextView textViewSaleTotalBill, textViewSaleAmountPaid, textViewSaleAmountDue;
        ImageButton buttonEditSale, buttonDeleteSale;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSaleCustomerName = itemView.findViewById(R.id.textViewSaleCustomerName);
            textViewSaleCustomerAddress = itemView.findViewById(R.id.textViewSaleCustomerAddress);
            textViewSaleDateTime = itemView.findViewById(R.id.textViewSaleDateTime);
            textViewSaleTotalBill = itemView.findViewById(R.id.textViewSaleTotalBill);
            textViewSaleAmountPaid = itemView.findViewById(R.id.textViewSaleAmountPaid);
            textViewSaleAmountDue = itemView.findViewById(R.id.textViewSaleAmountDue);
            buttonEditSale = itemView.findViewById(R.id.buttonEditSale);
            buttonDeleteSale = itemView.findViewById(R.id.buttonDeleteSale);
        }
    }
}
