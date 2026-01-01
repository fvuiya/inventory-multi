package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SaleToReturnAdapter;
import com.bsoft.inventorymanager.models.Sale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SelectSaleToReturnActivity extends BaseActivity {

    public static final String EXTRA_CUSTOMER_ID = "CUSTOMER_ID";
    private SaleToReturnAdapter adapter;
    private List<Sale> allSales = new ArrayList<>();
    private String currentCustomerId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sale_to_return);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        currentCustomerId = getIntent().getStringExtra(EXTRA_CUSTOMER_ID);

        RecyclerView recyclerView = findViewById(R.id.rv_sales_to_return);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SaleToReturnAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        com.bsoft.inventorymanager.views.SearchAndFilterView searchView = findViewById(R.id.search_filter_view_sales);
        searchView.setHint("Search by Customer, ID or Invoice");
        searchView.setOnSearchListener(new com.bsoft.inventorymanager.views.SearchAndFilterView.OnSearchListener() {
            @Override
            public void onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    searchSalesRemotely(query.trim());
                    // view clears focus implicitly if needed, or we can add method
                }
            }

            @Override
            public void onQueryTextChange(String newText) {
                filterSalesLocally(newText);
            }
        });

        loadInitialSales();
    }

    private void loadInitialSales() {
        showLoadingIndicator("Loading recent sales...");
        Query query = FirebaseFirestore.getInstance().collection("sales")
                .orderBy("saleDate", Query.Direction.DESCENDING)
                .limit(100); // Limit to recent 100 for performance of local search

        if (currentCustomerId != null) {
            query = query.whereEqualTo("customerId", currentCustomerId);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            hideLoadingIndicator();
            allSales.clear();
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null) {
                    sale.setDocumentId(doc.getId());
                    allSales.add(sale);
                }
            }
            adapter.updateList(allSales);
        }).addOnFailureListener(e -> {
            hideLoadingIndicator();
            showErrorToast("Failed to load sales: " + e.getMessage());
        });
    }

    private void filterSalesLocally(String query, List<Sale> listToFilter, SaleToReturnAdapter adapterToUpdate) {
        // Helper to reuse logic if needed, but we can just use class members
        // This signature matched the old one, but I'll refactor for clarity using class
        // members
        filterSalesLocally(query);
    }

    private void filterSalesLocally(String query) {
        if (allSales == null)
            return;

        List<Sale> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Sale sale : allSales) {
            boolean matchesId = sale.getDocumentId() != null
                    && sale.getDocumentId().toLowerCase().contains(lowerCaseQuery);
            boolean matchesCustomer = sale.getCustomerName() != null
                    && sale.getCustomerName().toLowerCase().contains(lowerCaseQuery);
            boolean matchesInvoice = sale.getInvoiceNumber() != null
                    && sale.getInvoiceNumber().toLowerCase().contains(lowerCaseQuery);

            // Allow searching by exact amount (e.g. "500")
            boolean matchesAmount = String.valueOf((int) sale.getTotalAmount()).equals(lowerCaseQuery) ||
                    String.valueOf(sale.getTotalAmount()).contains(lowerCaseQuery);

            if (matchesId || matchesCustomer || matchesInvoice || matchesAmount) {
                filteredList.add(sale);
            }
        }
        adapter.updateList(filteredList);
    }

    private void searchSalesRemotely(String queryStr) {
        showLoadingIndicator("Searching...");

        // 1. Search by Document ID
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> idTask = FirebaseFirestore
                .getInstance().collection("sales")
                .whereEqualTo(com.google.firebase.firestore.FieldPath.documentId(), queryStr)
                .get();

        // 2. Search by Customer Name (Prefix)
        Query nameQuery = FirebaseFirestore.getInstance().collection("sales")
                .orderBy("customerName")
                .startAt(queryStr)
                .endAt(queryStr + "\uf8ff");
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> nameTask = nameQuery.get();

        // 3. Search by Invoice Number (Exact) - Assuming invoiceNumber is a field we
        // can query
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> invoiceTask = FirebaseFirestore
                .getInstance().collection("sales")
                .whereEqualTo("invoiceNumber", queryStr)
                .get();

        com.google.android.gms.tasks.Tasks.whenAllSuccess(idTask, nameTask, invoiceTask)
                .addOnSuccessListener(results -> {
                    hideLoadingIndicator();
                    List<Sale> searchResults = new ArrayList<>();

                    // Helper to add unique sales
                    java.util.Set<String> addedIds = new java.util.HashSet<>();

                    // Process results
                    for (Object result : results) {
                        com.google.firebase.firestore.QuerySnapshot snap = (com.google.firebase.firestore.QuerySnapshot) result;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snap) {
                            Sale sale = doc.toObject(Sale.class);
                            if (sale != null) {
                                sale.setDocumentId(doc.getId());
                                if (addedIds.contains(sale.getDocumentId()))
                                    continue;

                                if (currentCustomerId == null || currentCustomerId.equals(sale.getCustomerId())) {
                                    searchResults.add(sale);
                                    addedIds.add(sale.getDocumentId());
                                }
                            }
                        }
                    }

                    if (searchResults.isEmpty()) {
                        showSuccessToast("No sales found for '" + queryStr + "'");
                        adapter.updateList(searchResults);
                    } else {
                        allSales = new ArrayList<>(searchResults); // Update local cache
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
