package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.PassiveIncome;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PassiveIncomeAdapter extends ListAdapter<PassiveIncome, PassiveIncomeAdapter.PassiveIncomeViewHolder> {

    public PassiveIncomeAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<PassiveIncome> DIFF_CALLBACK = new DiffUtil.ItemCallback<PassiveIncome>() {
        @Override
        public boolean areItemsTheSame(@NonNull PassiveIncome oldItem, @NonNull PassiveIncome newItem) {
            return oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PassiveIncome oldItem, @NonNull PassiveIncome newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public PassiveIncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passive_income, parent, false);
        return new PassiveIncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassiveIncomeViewHolder holder, int position) {
        PassiveIncome income = getItem(position);
        holder.source.setText(income.getSource());
        holder.note.setText(income.getNote());
        holder.date.setText(new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(income.getDate().toDate()));
        holder.amount.setText(String.format("%.2f", income.getAmount()));
    }

    static class PassiveIncomeViewHolder extends RecyclerView.ViewHolder {
        TextView source, note, date, amount;

        public PassiveIncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            source = itemView.findViewById(R.id.tv_source);
            note = itemView.findViewById(R.id.tv_note);
            date = itemView.findViewById(R.id.tv_date);
            amount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
