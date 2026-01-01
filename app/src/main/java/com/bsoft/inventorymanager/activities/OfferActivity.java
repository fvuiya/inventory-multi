package com.bsoft.inventorymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.OfferAdapter;
import com.bsoft.inventorymanager.models.Offer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OfferActivity extends AppCompatActivity implements OfferAdapter.OnOfferActionListener {

    private RecyclerView offersRecyclerView;
    private FloatingActionButton fabAddOffer;
    private OfferAdapter offerAdapter;
    private List<Offer> offerList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        Toolbar toolbar = findViewById(R.id.toolbar_offer);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Offers");

        offersRecyclerView = findViewById(R.id.offersRecyclerView);
        fabAddOffer = findViewById(R.id.fabAddOffer);

        db = FirebaseFirestore.getInstance();
        offerList = new ArrayList<>();
        offerAdapter = new OfferAdapter(offerList, this);

        offersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        offersRecyclerView.setAdapter(offerAdapter);

        fabAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OfferActivity.this, CreateOfferActivity.class);
                startActivity(intent);
            }
        });

        loadOffers();
    }

    private void loadOffers() {
        db.collection("offers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                offerList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Offer offer = document.toObject(Offer.class);
                    offer.setDocumentId(document.getId());
                    offerList.add(offer);
                }
                offerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEditOffer(Offer offer) {
        Intent intent = new Intent(this, CreateOfferActivity.class);
        intent.putExtra("EDIT_OFFER_ID", offer.getDocumentId());
        startActivity(intent);
    }

    @Override
    public void onDeleteOffer(Offer offer) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Offer")
                .setMessage("Are you sure you want to delete this offer?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("offers").document(offer.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> loadOffers());
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOffers();
    }
}
