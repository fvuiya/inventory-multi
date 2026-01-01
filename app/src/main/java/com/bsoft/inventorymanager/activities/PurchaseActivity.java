package com.bsoft.inventorymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.PurchaseAdapter;
import com.bsoft.inventorymanager.models.Purchase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@dagger.hilt.android.AndroidEntryPoint
public class PurchaseActivity extends AppCompatActivity
        implements PurchaseAdapter.OnPurchaseActionListener, PurchaseAdapter.OnPurchaseItemClickListener {

    private static final String TAG = "PurchaseActivity";
    private RecyclerView recyclerViewPurchases;
    private PurchaseAdapter purchaseAdapter;
    private List<Purchase> purchaseList;
    private FirebaseFirestore db;

    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private com.bsoft.inventorymanager.viewmodels.MainViewModel mainViewModel;
    private CollectionReference purchasesCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Toolbar toolbar = findViewById(R.id.toolbar_purchases);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Manage Purchases");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = FirebaseFirestore.getInstance();
        purchasesCollection = db.collection("purchases");

        // Initialize ViewModel (Activity-scoped, using MainViewModel as it holds the
        // purchase list)
        mainViewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.bsoft.inventorymanager.viewmodels.MainViewModel.class);

        db = FirebaseFirestore.getInstance();
        purchasesCollection = db.collection("purchases");

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_purchases);
        recyclerViewPurchases = findViewById(R.id.recycler_view_purchases);
        recyclerViewPurchases.setLayoutManager(new LinearLayoutManager(this));

        purchaseList = new ArrayList<>();
        purchaseAdapter = new PurchaseAdapter(this, purchaseList, this, this);
        recyclerViewPurchases.setAdapter(purchaseAdapter);

        // Setup Refresh & Scroll
        swipeRefreshLayout.setOnRefreshListener(() -> mainViewModel.refreshData());

        recyclerViewPurchases.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        mainViewModel.loadNextPagePurchases();
                    }
                }
            }
        });

        observeViewModel();

        FloatingActionButton fabAddPurchase = findViewById(R.id.fab_add_purchase);
        fabAddPurchase.setOnClickListener(view -> {
            Intent intent = new Intent(PurchaseActivity.this, CreatePurchaseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loadPurchasesData(); // Removed in favor of ViewModel
        // Trigger initial load if empty
        if (mainViewModel.getPurchases().getValue() == null || mainViewModel.getPurchases().getValue().isEmpty()) {
            mainViewModel.loadNextPagePurchases();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // if (purchasesListenerRegistration != null) {
        // purchasesListenerRegistration.remove();
        // }
    }

    private void observeViewModel() {
        mainViewModel.getPurchases().observe(this, purchases -> {
            if (purchases != null) {
                purchaseAdapter.setPurchases(purchases);
            }
        });

        mainViewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Purchase purchase) {
        Toast.makeText(this, "Clicked on purchase: " + purchase.getDocumentId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(Purchase purchase) {
        if (purchase.getDocumentId() == null || purchase.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Cannot edit purchase without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CreatePurchaseActivity.class);
        intent.putExtra("EDIT_PURCHASE_ID", purchase.getDocumentId());
        startActivity(intent);
        Toast.makeText(this, "Editing purchase: " + purchase.getDocumentId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(Purchase purchase) {
        if (purchase.getDocumentId() == null || purchase.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Cannot delete purchase without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Purchase")
                .setMessage("Are you sure you want to delete this purchase (" + purchase.getDocumentId() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> deletePurchaseFromFirestore(purchase.getDocumentId()))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePurchaseFromFirestore(String purchaseId) {
        if (purchaseId == null || purchaseId.isEmpty()) {
            Log.e(TAG, "Purchase ID is null or empty, cannot delete.");
            Toast.makeText(this, "Error: Purchase ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Attempting to delete purchase with ID: " + purchaseId);
        purchasesCollection.document(purchaseId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Purchase successfully deleted from Firestore!");
                    Toast.makeText(PurchaseActivity.this, "Purchase deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting purchase from Firestore", e);
                    Toast.makeText(PurchaseActivity.this, "Error deleting purchase: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
