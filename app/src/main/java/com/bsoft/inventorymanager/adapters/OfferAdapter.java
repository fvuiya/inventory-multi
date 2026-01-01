package com.bsoft.inventorymanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.SendMessageActivity;
import com.bsoft.inventorymanager.models.Offer;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final List<Offer> offerList;
    private final OnOfferActionListener actionListener;

    public interface OnOfferActionListener {
        void onEditOffer(Offer offer);
        void onDeleteOffer(Offer offer);
    }

    public OfferAdapter(List<Offer> offerList, OnOfferActionListener listener) {
        this.offerList = offerList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offerList.get(position);
        holder.textViewOfferTitle.setText(offer.getTitle());
        holder.textViewOfferDescription.setText(offer.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, SendMessageActivity.class);
            intent.putExtra("offer_description", offer.getDescription());
            context.startActivity(intent);
        });

        holder.buttonEditOffer.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditOffer(offer);
            }
        });

        holder.buttonDeleteOffer.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteOffer(offer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {

        TextView textViewOfferTitle;
        TextView textViewOfferDescription;
        ImageButton buttonEditOffer;
        ImageButton buttonDeleteOffer;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOfferTitle = itemView.findViewById(R.id.textViewOfferTitle);
            textViewOfferDescription = itemView.findViewById(R.id.textViewOfferDescription);
            buttonEditOffer = itemView.findViewById(R.id.buttonEditOffer);
            buttonDeleteOffer = itemView.findViewById(R.id.buttonDeleteOffer);
        }
    }
}
