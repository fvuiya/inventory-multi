package com.bsoft.inventorymanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectableCustomerAdapter;
import com.bsoft.inventorymanager.models.Customer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectCustomerActivity extends AppCompatActivity
        implements SelectableCustomerAdapter.OnCustomerSelectedListener {

    private static final String TAG = "SelectCustomerActivity";
    public static final String EXTRA_SELECTED_CUSTOMER = "SELECTED_CUSTOMER";

    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView customersRecyclerView;

    private FirebaseFirestore db;
    private SelectableCustomerAdapter selectableCustomerAdapter;
    private List<Customer> allCustomersList = new ArrayList<>();
    private List<Customer> displayedCustomersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_customer);

        Log.d(TAG, "=== onCreate called ===");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Customer");
        }

        searchEditText = findViewById(R.id.search_customer_edittext);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_select_customer);
        customersRecyclerView = findViewById(R.id.customers_recycler_view_select_customer);
        FloatingActionButton fabSearch = findViewById(R.id.fab_search_customer);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();

        fabSearch.setOnClickListener(v -> {
            searchEditText.post(() -> {
                if (searchEditText.requestFocus()) {
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                            android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        });

        swipeRefreshLayout.setRefreshing(true);
        loadCustomersFromFirestore();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectableCustomerAdapter = new SelectableCustomerAdapter(displayedCustomersList, this);
        customersRecyclerView.setAdapter(selectableCustomerAdapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    private com.bsoft.inventorymanager.utils.Debouncer debouncer = new com.bsoft.inventorymanager.utils.Debouncer(300);

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Search text changed: " + s.toString());
                debouncer.debounce(() -> applyFiltersAndSearch());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Ensure clicking the edit text also forces keyboard check
        searchEditText.setOnClickListener(v -> {
            if (v.requestFocus()) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(v, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered for customers.");
            loadCustomersFromFirestore();
        });
    }

    private void loadCustomersFromFirestore() {
        Log.d(TAG, "=== Loading customers from Firestore ===");
        db.collection("customers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore query successful. Document count: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "WARNING: No customers found in Firestore!");
                        Toast.makeText(this, "No customers found in database", Toast.LENGTH_LONG).show();
                    }

                    allCustomersList.clear();
                    int customerCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing document ID: " + document.getId());

                            Customer customer = document.toObject(Customer.class);
                            Log.d(TAG, "Customer object created. Name: " + customer.getName());

                            customer.setDocumentId(document.getId());
                            allCustomersList.add(customer);

                            Log.d(TAG, "Added customer #" + (++customerCount) + ": " +
                                    (customer.getName() != null ? customer.getName() : "NULL NAME") +
                                    " (ID: " + customer.getDocumentId() + ")" +
                                    " (Phone: "
                                    + (customer.getContactNumber() != null ? customer.getContactNumber() : "NULL")
                                    + ")");

                        } catch (Exception e) {
                            Log.e(TAG, "Error processing customer document " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Total customers processed: " + allCustomersList.size());
                    applyFiltersAndSearch();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "=== FIRESTORE ERROR ===", e);
                    Toast.makeText(SelectCustomerActivity.this, "Error loading customers: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private final java.util.concurrent.ExecutorService filterExecutor = java.util.concurrent.Executors
            .newSingleThreadExecutor();

    private void applyFiltersAndSearch() {
        final String searchQuery = searchEditText.getText().toString().toLowerCase(Locale.getDefault()).trim();
        Log.d(TAG, "Applying search filter: '" + searchQuery + "'");

        // Create snapshot for background thread
        final List<Customer> snapshot = new ArrayList<>(allCustomersList);

        filterExecutor.execute(() -> {
            List<Customer> filteredList = new ArrayList<>();
            for (Customer customer : snapshot) {
                if (customer == null)
                    continue;

                boolean searchMatch = searchQuery.isEmpty() ||
                        (customer.getName() != null
                                && customer.getName().toLowerCase(Locale.getDefault()).contains(searchQuery))
                        ||
                        (customer.getContactNumber() != null
                                && customer.getContactNumber().toLowerCase(Locale.getDefault()).contains(searchQuery));

                if (searchMatch) {
                    filteredList.add(customer);
                }
            }

            runOnUiThread(() -> {
                displayedCustomersList.clear();
                displayedCustomersList.addAll(filteredList);
                Log.d(TAG, "Final displayed customers count: " + displayedCustomersList.size());
                selectableCustomerAdapter.setCustomers(displayedCustomersList);

                if (displayedCustomersList.isEmpty()) {
                    if (!searchQuery.isEmpty()) {
                        // Optional toast
                    } else if (allCustomersList.isEmpty()) {
                        Toast.makeText(this, "No customers in database.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    @Override
    public void onCustomerSelected(Customer customer) {
        Log.d(TAG, "=== CUSTOMER SELECTED ===");
        if (customer == null) {
            Log.e(TAG, "ERROR: Customer is NULL in onCustomerSelected");
            return;
        }

        Log.d(TAG, "Customer selected: " + customer.getName());
        Log.d(TAG, "Customer ID: " + customer.getDocumentId());

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_CUSTOMER, customer);
        Log.d(TAG, "Putting customer in intent with key: " + EXTRA_SELECTED_CUSTOMER);

        setResult(Activity.RESULT_OK, resultIntent);
        Log.d(TAG, "Setting result and finishing activity");
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Home button pressed, setting result canceled");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}