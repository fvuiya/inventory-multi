package com.bsoft.inventorymanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.ActivityEvent;
import com.bsoft.inventorymanager.models.Damage;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.roles.CurrentUser;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ActivityEventAdapter extends ListAdapter<ActivityEvent, ActivityEventAdapter.ActivityEventViewHolder> {

    public ActivityEventAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ActivityEvent> DIFF_CALLBACK = new DiffUtil.ItemCallback<ActivityEvent>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityEvent oldItem, @NonNull ActivityEvent newItem) {
            return oldItem.getEventDate().equals(newItem.getEventDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ActivityEvent oldItem, @NonNull ActivityEvent newItem) {
            return oldItem.getEventData().equals(newItem.getEventData());
        }
    };

    @NonNull
    @Override
    public ActivityEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Universal Card Design Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_event, parent, false);
        return new ActivityEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityEventViewHolder holder, int position) {
        ActivityEvent event = getItem(position);
        holder.bind(event);
    }

    static class ActivityEventViewHolder extends RecyclerView.ViewHolder {
        // Universal fields: Type/Name (TopLeft), Date (TopRight), Desc (Middle), Amount
        // (BottomRight)
        TextView tvEventType, tvEventDate, tvEventDescription, tvEventAmount;

        public ActivityEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventType = itemView.findViewById(R.id.tv_event_type);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventDescription = itemView.findViewById(R.id.tv_event_description);
            tvEventAmount = itemView.findViewById(R.id.tv_event_amount);
        }

        void bind(ActivityEvent event) {
            tvEventDate.setText(
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(event.getEventDate().toDate()));

            switch (event.getEventType()) {
                case SALE:
                    bindSale(event);
                    break;
                case PURCHASE:
                    bindPurchase(event);
                    break;
                case DAMAGE:
                    bindDamage(event);
                    break;
                case RETURN_SALE:
                    bindReturnSale(event);
                    break;
                case RETURN_PURCHASE:
                    bindReturnPurchase(event);
                    break;
            }
        }

        private void bindSale(ActivityEvent event) {
            try {
                Sale sale = (Sale) event.getEventData();
                tvEventType.setText("SALE");
                tvEventAmount.setText(String.format("৳%.2f", sale.getTotalAmount()));
                tvEventAmount.setTextColor(Color.parseColor("#4CAF50")); // Green for income

                String description = "Sold " + (sale.getItems() != null ? sale.getItems().size() : 0) + " items";
                if (sale.getCustomerName() != null) {
                    description += " to " + sale.getCustomerName();
                }
                tvEventDescription.setText(description);
            } catch (Exception e) {
                tvEventDescription.setText("Sale Details Unavailable");
            }
        }

        private void bindPurchase(ActivityEvent event) {
            try {
                Purchase purchase = (Purchase) event.getEventData();
                tvEventType.setText("PURCHASE");
                tvEventAmount.setText(String.format("৳%.2f", purchase.getTotalAmount()));
                tvEventAmount.setTextColor(Color.parseColor("#2196F3")); // Blue for expense

                String description = "Purchased " + (purchase.getItems() != null ? purchase.getItems().size() : 0)
                        + " items";
                if (purchase.getSupplierName() != null) {
                    description += " from " + purchase.getSupplierName();
                }
                tvEventDescription.setText(description);
            } catch (Exception e) {
                tvEventDescription.setText("Purchase Details Unavailable");
            }
        }

        private void bindDamage(ActivityEvent event) {
            try {
                Damage damage = (Damage) event.getEventData();
                tvEventType.setText("DAMAGE");
                // Damage usually has no 'Amount' in the simple sense, or cost.
                // We leave it empty or clear it to avoid recycling issues.
                tvEventAmount.setText("");

                String description = damage.getQuantity() + " units of " + damage.getProductName() + " damaged";
                tvEventDescription.setText(description);
            } catch (Exception e) {
                tvEventDescription.setText("Damage Details Unavailable");
            }
        }

        private void bindReturnSale(ActivityEvent event) {
            tvEventType.setText("RETURN (SALE)");
            tvEventAmount.setTextColor(Color.RED);
            try {
                // Assuming eventData is Sale (the returned sale) or a Return object.
                // Given no Return class was found, it might be a Sale object representing the
                // return?
                // Or a Map?
                // For safety against crash:
                if (event.getEventData() instanceof Sale) {
                    Sale sale = (Sale) event.getEventData();
                    tvEventAmount.setText(String.format("৳%.2f", sale.getTotalAmount()));
                    tvEventDescription.setText("Return from " + sale.getCustomerName());
                } else {
                    tvEventAmount.setText("-");
                    tvEventDescription.setText("Sale Return Processed");
                }
            } catch (Exception e) {
                tvEventAmount.setText("-");
                tvEventDescription.setText("Sale Return Processed");
            }
        }

        private void bindReturnPurchase(ActivityEvent event) {
            tvEventType.setText("RETURN (PURCHASE)");
            tvEventAmount.setTextColor(Color.parseColor("#FFA000")); // Orange
            try {
                if (event.getEventData() instanceof Purchase) {
                    Purchase purchase = (Purchase) event.getEventData();
                    tvEventAmount.setText(String.format("৳%.2f", purchase.getTotalAmount()));
                    tvEventDescription.setText("Return to " + purchase.getSupplierName());
                } else {
                    tvEventAmount.setText("-");
                    tvEventDescription.setText("Purchase Return Processed");
                }
            } catch (Exception e) {
                tvEventAmount.setText("-");
                tvEventDescription.setText("Purchase Return Processed");
            }
        }
    }
}
