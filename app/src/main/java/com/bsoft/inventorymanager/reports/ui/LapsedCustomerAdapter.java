package com.bsoft.inventorymanager.reports.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Customer;
import java.util.List;

public class LapsedCustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_SHOW_MORE = 1;
    private static final int VIEW_TYPE_SHOW_LESS = 2;
    private static final int COLLAPSED_ITEM_COUNT = 3;

    private final List<Customer> lapsedCustomers;
    private boolean isExpanded = false;

    public LapsedCustomerAdapter(List<Customer> lapsedCustomers) {
        this.lapsedCustomers = lapsedCustomers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SHOW_MORE) {
            View view = inflater.inflate(R.layout.item_show_more, parent, false);
            return new ShowMoreViewHolder(view);
        } else if (viewType == VIEW_TYPE_SHOW_LESS) {
            View view = inflater.inflate(R.layout.item_show_less, parent, false);
            return new ShowLessViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_lapsed_customer, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            Customer customer = lapsedCustomers.get(position);
            ((ItemViewHolder) holder).customerName.setText(customer.getName());
            ((ItemViewHolder) holder).customerPhone.setText(customer.getContactNumber());
        } else if (holder instanceof ShowMoreViewHolder) {
            ((ShowMoreViewHolder) holder).showMoreButton.setOnClickListener(v -> {
                isExpanded = true;
                notifyDataSetChanged();
            });
        } else if (holder instanceof ShowLessViewHolder) {
            ((ShowLessViewHolder) holder).showLessButton.setOnClickListener(v -> {
                isExpanded = false;
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        if (lapsedCustomers.isEmpty()) {
            return 0;
        }
        if (lapsedCustomers.size() > COLLAPSED_ITEM_COUNT) {
            return isExpanded ? lapsedCustomers.size() + 1 : COLLAPSED_ITEM_COUNT + 1;
        } else {
            return lapsedCustomers.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (lapsedCustomers.size() > COLLAPSED_ITEM_COUNT) {
            if (isExpanded) {
                if (position == lapsedCustomers.size()) {
                    return VIEW_TYPE_SHOW_LESS;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            } else {
                if (position == COLLAPSED_ITEM_COUNT) {
                    return VIEW_TYPE_SHOW_MORE;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView customerName;
        TextView customerPhone;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.lapsed_customer_name);
            customerPhone = itemView.findViewById(R.id.lapsed_customer_phone);
        }
    }

    public static class ShowMoreViewHolder extends RecyclerView.ViewHolder {
        ImageButton showMoreButton;

        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            showMoreButton = itemView.findViewById(R.id.show_more_button);
        }
    }

    public static class ShowLessViewHolder extends RecyclerView.ViewHolder {
        ImageButton showLessButton;

        public ShowLessViewHolder(@NonNull View itemView) {
            super(itemView);
            showLessButton = itemView.findViewById(R.id.show_less_button);
        }
    }
}