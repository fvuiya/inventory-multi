package com.bsoft.inventorymanager.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ActivityEventAdapter;
import com.bsoft.inventorymanager.models.Damage;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.viewmodels.ActivityFeedViewModel;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class ProductProfileActivity extends AppCompatActivity {

    private ActivityFeedViewModel viewModel;
    private ActivityEventAdapter eventAdapter;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_profile);

        viewModel = new ViewModelProvider(this).get(ActivityFeedViewModel.class);

        RecyclerView historyRecyclerView = findViewById(R.id.rv_activity_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new ActivityEventAdapter();
        historyRecyclerView.setAdapter(eventAdapter);

        Button recordDamageButton = findViewById(R.id.btn_record_damage);
        recordDamageButton.setOnClickListener(v -> {
            if (currentProduct != null) {
                showRecordDamageDialog();
            }
        });

        String productId = getIntent().getStringExtra("product_id");

        if (productId != null) {
            FirebaseFirestore.getInstance().collection("products").document(productId).addSnapshotListener((snapshot, e) -> {
                if (snapshot != null && snapshot.exists()) {
                    currentProduct = snapshot.toObject(Product.class);
                    if (currentProduct != null) {
                        currentProduct.setDocumentId(snapshot.getId());
                        updateUi(currentProduct);
                    }
                }
            });

            viewModel.loadProductActivity(productId);
        }

        viewModel.getActivityEvents().observe(this, eventList -> eventAdapter.submitList(eventList));
    }

    private void updateUi(Product product) {
        if (product == null) return;

        ImageView photo = findViewById(R.id.iv_product_photo);
        TextView name = findViewById(R.id.tv_product_name);
        TextView brand = findViewById(R.id.tv_product_brand);
        TextView category = findViewById(R.id.tv_product_category);
        TextView code = findViewById(R.id.tv_product_code);
        TextView stock = findViewById(R.id.tv_product_stock);
        TextView purchasePrice = findViewById(R.id.tv_purchase_price);
        TextView mrp = findViewById(R.id.tv_mrp);
        TextView wholesalePrice = findViewById(R.id.tv_wholesale_price);
        TextView dealerPrice = findViewById(R.id.tv_dealer_price);

        name.setText(product.getName());
        brand.setText(product.getBrand());
        category.setText(product.getCategory());
        code.setText("Code: " + product.getProductCode());
        stock.setText("Stock: " + product.getQuantity());
        purchasePrice.setText(String.format("Purchase Price: %.2f", product.getPurchasePrice()));
        mrp.setText(String.format("MRP: %.2f", product.getMrp()));
        wholesalePrice.setText(String.format("Wholesale Price: %.2f", product.getWholesalePrice()));
        dealerPrice.setText(String.format("Dealer Price: %.2f", product.getDealerPrice()));

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                Glide.with(this).load(imageUrl).into(photo);
            } else {
                byte[] decodedString = Base64.decode(imageUrl, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                photo.setImageBitmap(decodedByte);
            }
        } else {
            photo.setImageResource(R.drawable.ic_product);
        }
    }

    private void showRecordDamageDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_add_damage, null);
        final EditText quantityEditText = view.findViewById(R.id.et_damage_quantity);
        final EditText reasonEditText = view.findViewById(R.id.et_damage_reason);

        new AlertDialog.Builder(this)
                .setTitle("Record Damaged Product")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String quantityStr = quantityEditText.getText().toString();
                    String reason = reasonEditText.getText().toString();
                    if (quantityStr.isEmpty() || reason.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int quantity = Integer.parseInt(quantityStr);

                    if (quantity > currentProduct.getQuantity()){
                        Toast.makeText(this, "Damaged quantity cannot be more than stock", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();

                    // 1. Create a new document in the 'damages' collection
                    DocumentReference damageRef = db.collection("damages").document();
                    Damage damage = new Damage(currentProduct.getDocumentId(), currentProduct.getName(), quantity, reason, userId, Timestamp.now());
                    batch.set(damageRef, damage);

                    // 2. Decrement the stock quantity of the product
                    DocumentReference productRef = db.collection("products").document(currentProduct.getDocumentId());
                    batch.update(productRef, "quantity", FieldValue.increment(-quantity));

                    // Commit the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Damage recorded and stock updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
