package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ReturnItemsAdapter;
import com.bsoft.inventorymanager.models.ReturnableItem;
import com.bsoft.inventorymanager.models.SaleReturnItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

public abstract class BaseCreateReturnActivity<T> extends AppCompatActivity {

    protected T originalItem;
    protected ReturnItemsAdapter adapter;

    protected abstract String getLayoutTitle();
    protected abstract String getOriginalItemId();
    protected abstract void loadOriginalItem(String itemId);
    protected abstract List<ReturnableItem> getReturnableItems();
    protected abstract void saveReturn(List<SaleReturnItem> returnedItems);
    protected abstract FieldValue getStockUpdate(int quantity);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_return);

        setTitle(getLayoutTitle());

        String itemId = getIntent().getStringExtra(getOriginalItemId());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_return_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button confirmButton = findViewById(R.id.button_confirm_return);
        confirmButton.setOnClickListener(v -> {
            if (adapter != null) {
                saveReturn(adapter.getReturnedItems());
            }
        });

        loadOriginalItem(itemId);
    }

    protected void onOriginalItemLoaded() {
        if (originalItem != null) {
            adapter = new ReturnItemsAdapter(getReturnableItems());
            RecyclerView recyclerView = findViewById(R.id.recycler_view_return_items);
            recyclerView.setAdapter(adapter);
        }
    }

    protected void processReturn(String collectionName, Object returnObject, List<SaleReturnItem> returnedItems) {
        if (returnedItems.isEmpty()) {
            Toast.makeText(this, "No items selected for return", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        double totalAmount = 0;
        for (SaleReturnItem item : returnedItems) {
            totalAmount += item.getPricePerItem() * item.getQuantity();
            DocumentReference productRef = db.collection("products").document(item.getProductId());
            batch.update(productRef, "quantity", getStockUpdate(item.getQuantity()));
        }

        // You might need to set the total amount on the return object here if it's a field

        batch.set(db.collection(collectionName).document(), returnObject);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Return processed successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
