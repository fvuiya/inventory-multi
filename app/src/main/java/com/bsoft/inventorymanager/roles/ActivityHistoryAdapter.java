package com.bsoft.inventorymanager.roles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    private final List<ActivityHistory> historyList;

    public ActivityHistoryAdapter(List<ActivityHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityHistory history = historyList.get(position);
        holder.eventDescription.setText(history.getEventDescription());
        holder.performedBy.setText("By: " + history.getPerformedBy());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.eventTimestamp.setText(sdf.format(history.getEventTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventDescription;
        TextView eventTimestamp;
        TextView performedBy;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDescription = itemView.findViewById(R.id.tv_event_description);
            eventTimestamp = itemView.findViewById(R.id.tv_event_timestamp);
            performedBy = itemView.findViewById(R.id.tv_performed_by);
        }
    }
}
