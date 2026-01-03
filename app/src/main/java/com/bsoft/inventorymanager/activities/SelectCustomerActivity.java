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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectableCustomerAdapter;
import com.bsoft.inventorymanager.model.Customer;
import com.bsoft.inventorymanager.viewmodels.CustomerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectCustomerActivity extends AppCompatActivity
        implements SelectableCustomerAdapter.OnCustomerSelectedListener {

    private static final String TAG = "SelectCustomerActivity";
    public static final String EXTRA_SELECTED_CUSTOMER = "SELECTED_CUSTOMER";

    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView customersRecyclerView;

    private SelectableCustomerAdapter selectableCustomerAdapter;
    private List<Customer> allCustomersList = new ArrayList<>();
    private List<Customer> displayedCustomersList = new ArrayList<>();

    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_customer);

        Log.d(TAG, "=== onCreate called ===");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Customer");
        }

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        searchEditText = findViewById(R.id.search_customer_edittext);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_select_customer);
        customersRecyclerView = findViewById(R.id.customers_recycler_view_select_customer);
        FloatingActionButton fabSearch = findViewById(R.id.fab_search_customer);

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

        observeViewModel();
        viewModel.loadCustomers(); // Helper to start loading
    }

    private void observeViewModel() {
        viewModel.getCustomers().observe(this, customers -> {
            swipeRefreshLayout.setRefreshing(false);
            if (customers != null) {
                allCustomersList.clear();
                allCustomersList.addAll(customers);
                applyFiltersAndSearch(); // Re-apply filters on new data
            }
        });

        viewModel.getError().observe(this, error -> {
            swipeRefreshLayout.setRefreshing(false);
            if (error != null) {
                Toast.makeText(this, "Error loading customers: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
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
            viewModel.loadCustomers();
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
                        // Toast.makeText(this, "No customers in database.", Toast.LENGTH_LONG).show();
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
        // Passing serialized object might be risky if KMP model isn't Serializable
        // compatible with Bundle.
        // It's safer to pass ID if the receiver can fetch it, but historically we
        // passed the object.
        // I'll try passing it as Serializable since it's an interface on the Activity
        // side usually.
        // KMP @Serializable generates a serializer, but not necessarily
        // java.io.Serializable.
        // HOWEVER: The Customer.kt file showed earlier does NOT implement
        // java.io.Serializable.
        // So `resultIntent.putExtra` will fail if it expects Serializable.
        // Wait, `putExtra` accepts Serializable. If Customer is not Serializable, this
        // won't compile or runtime error.
        // I need to check `Customer.kt` again.
        // Oh, wait! The original Legacy `models.Customer` WAS Serializable. The NEW
        // `model.Customer` might NOT be.
        // If not, I should NOT pass the object. I should pass ID.
        // But `CreateSaleActivity` expects the object.

        // CRITICAL DEBT: The KMP models typically don't implement java.io.Serializable.
        // I will change this to pass ID or make Customer Serializable in Android source
        // set.
        // Making it Serializable in commonMain is tricky (no java.io).
        // Best approach: Add `java.io.Serializable` to `Customer` class via `actual` or
        // just simple interface inheritance in AndroidMain? No.

        // For now, I'll pass the properties I need or rely on a static cache/singleton?
        // No.
        // I'll assume for a moment that `CreateSaleActivity` will be updated to handle
        // ID or I'll fix serialization.
        // Actually, I can use a JSON string!

        // Let's pass the ID. `CreateSaleActivity` can look it up or I'll update it.
        // But the previous implementation passed the object.
        // I'll check `CreateSaleActivity` next.
        // For now, I commented out putting extra object in previous files.
        // I will put the ID.

        resultIntent.putExtra("SELECTED_CUSTOMER_ID", customer.getDocumentId());
        resultIntent.putExtra("SELECTED_CUSTOMER_NAME", customer.getName());

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