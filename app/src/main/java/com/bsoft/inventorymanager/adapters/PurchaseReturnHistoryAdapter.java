package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.PurchaseReturn;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PurchaseReturnHistoryAdapter extends RecyclerView.Adapter<PurchaseReturnHistoryAdapter.ViewHolder> {

    private final List<PurchaseReturn> purchaseReturns;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public PurchaseReturnHistoryAdapter(List<PurchaseReturn> purchaseReturns) {
        this.purchaseReturns = purchaseReturns;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseReturn purchaseReturn = purchaseReturns.get(position);

        // Use document ID substring for visual ID if actual display ID is not available
        String displayId = purchaseReturn.getDocumentId();
        if (displayId != null && displayId.length() > 8) {
            displayId = displayId.substring(0, 8).toUpperCase();
        }
        holder.tvReturnId.setText("Return #" + displayId);

        if (purchaseReturn.getReturnDate() != null) {
            holder.tvReturnDate.setText(dateFormat.format(purchaseReturn.getReturnDate().toDate()));
        } else {
            holder.tvReturnDate.setText("N/A");
        }

        holder.tvPartyName.setText(
                purchaseReturn.getSupplierName() != null ? purchaseReturn.getSupplierName() : "Unknown Supplier");
        holder.tvReturnAmount
                .setText(String.format(Locale.getDefault(), "%.2f", purchaseReturn.getTotalCreditAmount()));
    }

    @Override
    public int getItemCount() {
        return purchaseReturns.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReturnId;
        TextView tvReturnDate;
        TextView tvPartyName;
        TextView tvReturnAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReturnId = itemView.findViewById(R.id.tv_return_id);
            tvReturnDate = itemView.findViewById(R.id.tv_return_date);
            tvPartyName = itemView.findViewById(R.id.tv_party_name);
            tvReturnAmount = itemView.findViewById(R.id.tv_return_amount);
        }
    }
}
