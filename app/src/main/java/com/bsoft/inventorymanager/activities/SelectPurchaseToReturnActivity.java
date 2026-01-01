package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.PurchaseToReturnAdapter;
import com.bsoft.inventorymanager.models.Purchase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SelectPurchaseToReturnActivity extends BaseActivity {

    public static final String EXTRA_SUPPLIER_ID = "SUPPLIER_ID";
    private PurchaseToReturnAdapter adapter;
    private List<Purchase> allPurchases = new ArrayList<>();
    private String currentSupplierId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_purchase_to_return);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        currentSupplierId = getIntent().getStringExtra(EXTRA_SUPPLIER_ID);

        RecyclerView recyclerView = findViewById(R.id.rv_purchases_to_return);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PurchaseToReturnAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        com.bsoft.inventorymanager.views.SearchAndFilterView searchView = findViewById(
                R.id.search_filter_view_purchases);
        searchView.setHint("Search by Supplier, ID or PO");
        searchView.setOnSearchListener(new com.bsoft.inventorymanager.views.SearchAndFilterView.OnSearchListener() {
            @Override
            public void onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    searchPurchasesRemotely(query.trim());
                }
            }

            @Override
            public void onQueryTextChange(String newText) {
                filterPurchasesLocally(newText);
            }
        });

        loadInitialPurchases();
    }

    private void loadInitialPurchases() {
        showLoadingIndicator("Loading recent purchases...");
        Query query = FirebaseFirestore.getInstance().collection("purchases")
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .limit(100);

        if (currentSupplierId != null && !currentSupplierId.isEmpty()) {
            query = query.whereEqualTo("supplierId", currentSupplierId);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            hideLoadingIndicator();
            allPurchases.clear();
            queryDocumentSnapshots.forEach(doc -> {
                Purchase purchase = doc.toObject(Purchase.class);
                purchase.setDocumentId(doc.getId());
                allPurchases.add(purchase);
            });
            adapter.updateList(allPurchases);
        }).addOnFailureListener(e -> {
            hideLoadingIndicator();
            showErrorToast("Failed to load purchases: " + e.getMessage());
        });
    }

    private void filterPurchasesLocally(String query) {
        if (allPurchases == null)
            return;
        List<Purchase> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Purchase purchase : allPurchases) {
            boolean matchesId = purchase.getDocumentId() != null
                    && purchase.getDocumentId().toLowerCase().contains(lowerCaseQuery);
            boolean matchesSupplier = purchase.getSupplierName() != null
                    && purchase.getSupplierName().toLowerCase().contains(lowerCaseQuery);
            boolean matchesInvoice = (purchase.getInvoiceNumber() != null
                    && purchase.getInvoiceNumber().toLowerCase().contains(lowerCaseQuery))
                    || (purchase.getPurchaseOrderNumber() != null
                            && purchase.getPurchaseOrderNumber().toLowerCase().contains(lowerCaseQuery));

            boolean matchesAmount = String.valueOf((int) purchase.getTotalAmount()).equals(lowerCaseQuery) ||
                    String.valueOf(purchase.getTotalAmount()).contains(lowerCaseQuery);

            if (matchesId || matchesSupplier || matchesInvoice || matchesAmount) {
                filteredList.add(purchase);
            }
        }
        adapter.updateList(filteredList);
    }

    // Kept for signature compatibility if referenced elsewhere, but mostly internal
    // use.
    private void setupWithSearch(List<Purchase> allPurchases, RecyclerView recyclerView) {
        // Deprecated by new structure but keeping if needed or refactoring.
        // Actually I'm replacing the whole class logic so I can remove this old helper.
    }

    private void filterPurchases(String query, List<Purchase> allPurchases, PurchaseToReturnAdapter adapter) {
        // Redundant helper
        filterPurchasesLocally(query);
    }

    private void searchPurchasesRemotely(String queryStr) {
        showLoadingIndicator("Searching...");

        // 1. Search by Document ID
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> idTask = FirebaseFirestore
                .getInstance().collection("purchases")
                .whereEqualTo(com.google.firebase.firestore.FieldPath.documentId(), queryStr)
                .get();

        // 2. Search by Supplier Name (Prefix)
        Query nameQuery = FirebaseFirestore.getInstance().collection("purchases")
                .orderBy("supplierName")
                .startAt(queryStr)
                .endAt(queryStr + "\uf8ff");
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> nameTask = nameQuery.get();

        // 3. Search by Invoice Number (Exact)
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> invoiceTask = FirebaseFirestore
                .getInstance().collection("purchases")
                .whereEqualTo("invoiceNumber", queryStr)
                .get();

        com.google.android.gms.tasks.Tasks.whenAllSuccess(idTask, nameTask, invoiceTask)
                .addOnSuccessListener(results -> {
                    hideLoadingIndicator();
                    List<Purchase> searchResults = new ArrayList<>();
                    java.util.Set<String> addedIds = new java.util.HashSet<>();

                    for (Object result : results) {
                        com.google.firebase.firestore.QuerySnapshot snap = (com.google.firebase.firestore.QuerySnapshot) result;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snap) {
                            Purchase p = doc.toObject(Purchase.class);
                            if (p != null) {
                                p.setDocumentId(doc.getId());
                                if (addedIds.contains(p.getDocumentId()))
                                    continue;

                                if (currentSupplierId == null || currentSupplierId.isEmpty()
                                        || currentSupplierId.equals(p.getSupplierId())) {
                                    searchResults.add(p);
                                    addedIds.add(p.getDocumentId());
                                }
                            }
                        }
                    }

                    if (searchResults.isEmpty()) {
                        showSuccessToast("No purchases found for '" + queryStr + "'");
                        adapter.updateList(searchResults);
                    } else {
                        allPurchases = new ArrayList<>(searchResults);
                        adapter.updateList(searchResults);
                    }

                }).addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    showErrorToast("Search failed: " + e.getMessage());
                });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
