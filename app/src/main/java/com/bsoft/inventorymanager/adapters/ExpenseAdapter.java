package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses = new ArrayList<>();
    private final OnExpenseInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public interface OnExpenseInteractionListener {
        void onDeleteExpense(Expense expense);

        void onEditExpense(Expense expense);
    }

    public ExpenseAdapter(OnExpenseInteractionListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<Expense> newExpenses) {
        this.expenses.clear();
        this.expenses.addAll(newExpenses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription, tvDate, tvCategory, tvAmount;
        private final ImageButton btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_expense_description);
            tvDate = itemView.findViewById(R.id.tv_expense_date);
            tvCategory = itemView.findViewById(R.id.tv_expense_category);
            tvAmount = itemView.findViewById(R.id.tv_expense_amount);
            btnDelete = itemView.findViewById(R.id.button_delete_expense);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditExpense(expenses.get(pos));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteExpense(expenses.get(pos));
                }
            });
        }

        public void bind(Expense expense) {
            tvDescription.setText(expense.getDescription());
            if (expense.getDate() != null) {
                tvDate.setText(dateFormat.format(expense.getDate().toDate()));
            } else {
                tvDate.setText("");
            }
            tvCategory.setText(expense.getCategory());
            tvAmount.setText(String.format(Locale.US, "%.2f", expense.getAmount()));
        }
    }
}
