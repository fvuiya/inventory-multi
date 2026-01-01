package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {

    public NotificationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK = new DiffUtil.ItemCallback<Notification>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getDocumentId() != null && oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            // Simplified check; in a real app, check all visible fields
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getBody().equals(newItem.getBody()) &&
                    oldItem.isRead() == newItem.isRead();
        }
    };

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = getItem(position);
        holder.bind(notification);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textBody;
        private final TextView textDate;
        private final TextView textAmount;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_notification_title);
            textBody = itemView.findViewById(R.id.text_notification_body);
            textDate = itemView.findViewById(R.id.text_notification_date);
            textAmount = itemView.findViewById(R.id.text_notification_amount);
        }

        public void bind(Notification notification) {
            textTitle.setText(notification.getTitle());
            textBody.setText(notification.getBody());

            if (notification.getTimestamp() != null) {
                textDate.setText(dateFormat.format(notification.getTimestamp()));
            } else {
                textDate.setText("");
            }

            if (notification.getAmount() > 0) {
                textAmount.setVisibility(View.VISIBLE);
                textAmount.setText(String.format(Locale.getDefault(), "$%.2f", notification.getAmount()));
            } else {
                textAmount.setVisibility(View.GONE);
            }
        }
    }
}
