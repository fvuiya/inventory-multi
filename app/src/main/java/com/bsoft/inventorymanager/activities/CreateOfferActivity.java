package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Offer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateOfferActivity extends AppCompatActivity {

    private EditText editTextOfferTitle;
    private EditText editTextOfferDescription;
    private Button buttonSaveOffer;

    private FirebaseFirestore db;
    private String editOfferId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offer);

        editTextOfferTitle = findViewById(R.id.editTextOfferTitle);
        editTextOfferDescription = findViewById(R.id.editTextOfferDescription);
        buttonSaveOffer = findViewById(R.id.buttonSaveOffer);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("EDIT_OFFER_ID")) {
            editOfferId = getIntent().getStringExtra("EDIT_OFFER_ID");
            loadOfferData();
            buttonSaveOffer.setText("Update");
            setTitle("Edit Offer");
        }

        buttonSaveOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOffer();
            }
        });
    }

    private void loadOfferData() {
        db.collection("offers").document(editOfferId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Offer offer = documentSnapshot.toObject(Offer.class);
                if (offer != null) {
                    editTextOfferTitle.setText(offer.getTitle());
                    editTextOfferDescription.setText(offer.getDescription());
                }
            }
        });
    }

    private void saveOffer() {
        String title = editTextOfferTitle.getText().toString().trim();
        String description = editTextOfferDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Offer offer = new Offer(title, description);

        if (editOfferId != null) {
            db.collection("offers").document(editOfferId).set(offer)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateOfferActivity.this, "Offer updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateOfferActivity.this, "Error updating offer", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("offers").add(offer)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(CreateOfferActivity.this, "Offer saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateOfferActivity.this, "Error saving offer", Toast.LENGTH_SHORT).show());
        }
    }
}
