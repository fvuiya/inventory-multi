package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.CreatePurchaseReturnActivity;
import com.bsoft.inventorymanager.models.Purchase;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PurchaseToReturnAdapter extends RecyclerView.Adapter<PurchaseToReturnAdapter.ViewHolder> {

    private final List<Purchase> purchases;

    public PurchaseToReturnAdapter(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    public void updateList(List<Purchase> newPurchases) {
        this.purchases.clear();
        this.purchases.addAll(newPurchases);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_to_return, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Purchase purchase = purchases.get(position);

        holder.supplierName
                .setText(purchase.getSupplierName() != null ? purchase.getSupplierName() : "Unknown Supplier");

        // Show Invoice # if available, else Purchase Order #
        String ref = purchase.getInvoiceNumber();
        if (ref == null || ref.isEmpty()) {
            ref = purchase.getPurchaseOrderNumber();
        }
        if (ref == null)
            ref = "N/A";
        holder.invoiceNumber.setText(ref);

        if (purchase.getPurchaseDate() != null) {
            holder.purchaseDate.setText(
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(purchase.getPurchaseDate().toDate()));
        } else {
            holder.purchaseDate.setText("--");
        }

        holder.purchaseTotal.setText(String.format("৳%.2f", purchase.getTotalAmount()));

        holder.documentId.setText("ID: " + purchase.getDocumentId());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CreatePurchaseReturnActivity.class);
            intent.putExtra("PURCHASE_ID", purchase.getDocumentId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return purchases.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView supplierName, purchaseDate, purchaseTotal, invoiceNumber, documentId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            supplierName = itemView.findViewById(R.id.tv_supplier_name);
            purchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            purchaseTotal = itemView.findViewById(R.id.tv_purchase_total);
            invoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            documentId = itemView.findViewById(R.id.tv_document_id);
        }
    }
}
