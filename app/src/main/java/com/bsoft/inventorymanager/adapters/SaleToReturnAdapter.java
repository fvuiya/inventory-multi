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
import com.bsoft.inventorymanager.activities.CreateSaleReturnActivity;
import com.bsoft.inventorymanager.models.Sale;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SaleToReturnAdapter extends RecyclerView.Adapter<SaleToReturnAdapter.ViewHolder> {

    private final List<Sale> sales;

    public SaleToReturnAdapter(List<Sale> sales) {
        this.sales = sales;
    }

    public void updateList(List<Sale> newSales) {
        this.sales.clear();
        this.sales.addAll(newSales);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_to_return, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sale sale = sales.get(position);

        holder.customerName.setText(sale.getCustomerName() != null ? sale.getCustomerName() : "Unknown Customer");

        String invoice = sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : "N/A";
        holder.invoiceNumber.setText(invoice);

        if (sale.getSaleDate() != null) {
            holder.saleDate.setText(
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(sale.getSaleDate().toDate()));
        } else {
            holder.saleDate.setText("--");
        }

        holder.saleTotal.setText(String.format("৳%.2f", sale.getTotalAmount()));

        holder.documentId.setText("ID: " + sale.getDocumentId());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CreateSaleReturnActivity.class);
            intent.putExtra("SALE_ID", sale.getDocumentId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sales.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, saleDate, saleTotal, invoiceNumber, documentId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.tv_customer_name);
            saleDate = itemView.findViewById(R.id.tv_sale_date);
            saleTotal = itemView.findViewById(R.id.tv_sale_total);
            invoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            documentId = itemView.findViewById(R.id.tv_document_id);
        }
    }
}
