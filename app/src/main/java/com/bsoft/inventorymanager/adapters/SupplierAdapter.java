package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.SupplierProfileActivity;
import com.bsoft.inventorymanager.model.Supplier;
import java.util.List;
import java.util.Locale;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {

    private final List<Supplier> supplierList;
    private final OnSupplierActionListener actionListener;

    public interface OnSupplierActionListener {
        void onEdit(int position, Supplier supplier);

        void onDelete(int position, Supplier supplier);
    }

    public SupplierAdapter(List<Supplier> supplierList, OnSupplierActionListener actionListener) {
        this.supplierList = supplierList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        Supplier supplier = supplierList.get(position);
        holder.textViewSupplierName.setText(supplier.getName());
        holder.textViewSupplierAddress.setText(supplier.getAddress());
        holder.textViewSupplierAge.setText(String.format(Locale.getDefault(), "Age: %d", supplier.getAge()));
        holder.textViewSupplierPhone.setText(
                String.format("Phone: %s", supplier.getContactNumber() != null ? supplier.getContactNumber() : "N/A"));

        if (supplier.getPhoto() != null && !supplier.getPhoto().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(supplier.getPhoto(), Base64.DEFAULT);
                holder.imageViewSupplier
                        .setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            } catch (Exception e) {
                holder.imageViewSupplier.setImageResource(R.drawable.ic_customer);
            }
        } else {
            holder.imageViewSupplier.setImageResource(R.drawable.ic_customer);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, SupplierProfileActivity.class);
            intent.putExtra("supplier_id", supplier.getDocumentId());
            context.startActivity(intent);
        });

        holder.buttonEditSupplier.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onEdit(currentPosition, supplierList.get(currentPosition));
                }
            }
        });

        holder.buttonDeleteSupplier.setOnClickListener(v -> {
            if (actionListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDelete(currentPosition, supplierList.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return supplierList == null ? 0 : supplierList.size();
    }

    static class SupplierViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSupplierName, textViewSupplierAddress, textViewSupplierAge, textViewSupplierPhone;
        ImageButton buttonEditSupplier, buttonDeleteSupplier;
        ImageView imageViewSupplier;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSupplierName = itemView.findViewById(R.id.textViewSupplierName);
            textViewSupplierAddress = itemView.findViewById(R.id.textViewSupplierAddress);
            textViewSupplierAge = itemView.findViewById(R.id.textViewSupplierAge);
            textViewSupplierPhone = itemView.findViewById(R.id.textViewSupplierPhone);
            buttonEditSupplier = itemView.findViewById(R.id.buttonEditSupplier);
            buttonDeleteSupplier = itemView.findViewById(R.id.buttonDeleteSupplier);
            imageViewSupplier = itemView.findViewById(R.id.iv_supplier_image_item);
        }
    }
}
