package com.bsoft.inventorymanager.activities;

import android.content.DialogInterface;
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
import com.bsoft.inventorymanager.adapters.SalesAdapter;
// [KMP MIGRATION] Use shared Sale model
import com.bsoft.inventorymanager.model.Sale;
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
public class SalesActivity extends AppCompatActivity
        implements SalesAdapter.OnSaleActionListener, SalesAdapter.OnSaleItemClickListener {

    private static final String TAG = "SalesActivity";
    private RecyclerView recyclerViewSales;
    private SalesAdapter salesAdapter;
    private List<Sale> saleList;
    private FirebaseFirestore db;

    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private com.bsoft.inventorymanager.viewmodels.MainViewModel mainViewModel;
    private CollectionReference salesCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        Toolbar toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Manage Sales");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = FirebaseFirestore.getInstance();
        salesCollection = db.collection("sales");

        // Initialize ViewModel (Activity-scoped, using MainViewModel as it holds the
        // sales list)
        mainViewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.bsoft.inventorymanager.viewmodels.MainViewModel.class);

        db = FirebaseFirestore.getInstance();
        salesCollection = db.collection("sales");

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_sales);
        recyclerViewSales = findViewById(R.id.recycler_view_sales);
        recyclerViewSales.setLayoutManager(new LinearLayoutManager(this));

        saleList = new ArrayList<>();
        salesAdapter = new SalesAdapter(this, saleList, this, this);
        recyclerViewSales.setAdapter(salesAdapter);

        // Setup Refresh & Scroll
        swipeRefreshLayout.setOnRefreshListener(() -> mainViewModel.refreshData());

        recyclerViewSales.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
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
                        mainViewModel.loadNextPageSales();
                    }
                }
            }
        });

        observeViewModel();

        FloatingActionButton fabAddSale = findViewById(R.id.fab_add_sale);
        fabAddSale.setOnClickListener(view -> {
            Intent intent = new Intent(SalesActivity.this, CreateSaleActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loadSalesData(); // Removed in favor of ViewModel
        // Trigger initial load if empty (handled by ViewModel preload usually, but good
        // to check)
        if (mainViewModel.getSales().getValue() == null || mainViewModel.getSales().getValue().isEmpty()) {
            mainViewModel.loadNextPageSales();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // if (salesListenerRegistration != null) {
        // salesListenerRegistration.remove();
        // }
    }

    private void observeViewModel() {
        mainViewModel.getSales().observe(this, sales -> {
            if (sales != null) {
                salesAdapter.setSales(sales);
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
    public void onItemClick(Sale sale) {
        // If you want to navigate to a Sale Detail screen:
        // Intent intent = new Intent(this, SaleDetailActivity.class);
        // intent.putExtra("SALE_ID", sale.getDocumentId()); // Make sure
        // SaleDetailActivity handles this
        // startActivity(intent);
        Toast.makeText(this, "Clicked on sale: " + (sale.getDocumentId() != null ? sale.getDocumentId() : "Unknown ID"),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(Sale sale) {
        if (sale.getDocumentId() == null || sale.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Cannot edit sale without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CreateSaleActivity.class);
        // Use CreateSaleActivity.EXTRA_EDIT_SALE_ID if defined there
        intent.putExtra("EDIT_SALE_ID", sale.getDocumentId());
        startActivity(intent);
        Toast.makeText(this, "Editing sale: " + sale.getDocumentId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(Sale sale) {
        if (sale.getDocumentId() == null || sale.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Cannot delete sale without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Sale")
                .setMessage("Are you sure you want to delete this sale (" + sale.getDocumentId() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSaleFromFirestore(sale.getDocumentId()))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteSaleFromFirestore(String saleId) {
        if (saleId == null || saleId.isEmpty()) {
            Log.e(TAG, "Sale ID is null or empty, cannot delete.");
            Toast.makeText(this, "Error: Sale ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Attempting to delete sale with ID: " + saleId);
        salesCollection.document(saleId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sale successfully deleted from Firestore!");
                    Toast.makeText(SalesActivity.this, "Sale deleted.", Toast.LENGTH_SHORT).show();
                    // The SnapshotListener in loadSalesData should automatically update the UI
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting sale from Firestore", e);
                    Toast.makeText(SalesActivity.this, "Error deleting sale: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
