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
import com.bsoft.inventorymanager.adapters.SelectableSupplierAdapter;
import com.bsoft.inventorymanager.model.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@dagger.hilt.android.AndroidEntryPoint
public class SelectSupplierActivity extends AppCompatActivity
        implements SelectableSupplierAdapter.OnSupplierSelectedListener {

    private static final String TAG = "SelectSupplierActivity";
    public static final String EXTRA_SELECTED_SUPPLIER = "SELECTED_SUPPLIER";

    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView suppliersRecyclerView;
    private FloatingActionButton filterFab;

    private com.bsoft.inventorymanager.viewmodels.SupplierViewModel viewModel;
    private SelectableSupplierAdapter selectableSupplierAdapter;
    private final List<Supplier> allSuppliersList = new ArrayList<>();
    private final List<Supplier> displayedSuppliersList = new ArrayList<>();

    // Filter fields
    private boolean[] filterOptions = { false, false, false, false }; // By Rating, By Preferred, By Active Status, By
                                                                      // Payment Terms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_supplier);

        Log.d(TAG, "=== onCreate called ===");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Supplier");
        }

        searchEditText = findViewById(R.id.search_supplier_edittext);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_select_supplier);
        suppliersRecyclerView = findViewById(R.id.suppliers_recycler_view_select_supplier);
        filterFab = findViewById(R.id.fab_filter_supplier);

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.bsoft.inventorymanager.viewmodels.SupplierViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilterFab();
        setupSwipeRefresh();

        observeViewModel();

        loadSuppliers();
    }

    private void observeViewModel() {
        viewModel.getSuppliers().observe(this, suppliers -> {
            Log.d(TAG, "Suppliers loaded from ViewModel. Count: " + (suppliers != null ? suppliers.size() : "null"));
            allSuppliersList.clear();
            if (suppliers != null) {
                allSuppliersList.addAll(suppliers);
            }
            applyFiltersAndSearch();
            swipeRefreshLayout.setRefreshing(false);

            if (allSuppliersList.isEmpty()) {
                Toast.makeText(this, "No suppliers found in database", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Log.e(TAG, "Error loading suppliers: " + error);
                Toast.makeText(SelectSupplierActivity.this, "Error loading suppliers: " + error, Toast.LENGTH_LONG)
                        .show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading) {
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadSuppliers() {
        viewModel.loadSuppliers();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        suppliersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectableSupplierAdapter = new SelectableSupplierAdapter(displayedSuppliersList, this);
        suppliersRecyclerView.setAdapter(selectableSupplierAdapter);
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

    private void setupFilterFab() {
        filterFab.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        // Create a dialog for filtering suppliers
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Filter Suppliers");

        // Create filter options
        String[] filterOptionsLabels = { "By Rating", "By Preferred", "By Active Status", "By Payment Terms" };

        builder.setMultiChoiceItems(filterOptionsLabels, this.filterOptions, (dialog, which, isChecked) -> {
            this.filterOptions[which] = isChecked;
        });

        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Apply the selected filters
            applyFiltersAndSearch();
        });

        builder.setNegativeButton("Clear", (dialog, which) -> {
            // Clear all filters
            for (int i = 0; i < this.filterOptions.length; i++) {
                this.filterOptions[i] = false;
            }
            applyFiltersAndSearch();
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered for suppliers.");
            loadSuppliers();
        });
    }

    private final java.util.concurrent.ExecutorService filterExecutor = java.util.concurrent.Executors
            .newSingleThreadExecutor();

    private void applyFiltersAndSearch() {
        final String searchQuery = searchEditText.getText().toString().toLowerCase(Locale.getDefault()).trim();
        Log.d(TAG, "Applying search filter: '" + searchQuery + "'");

        // Create snapshot for background thread
        final List<Supplier> snapshot = new ArrayList<>(allSuppliersList);
        final boolean[] currentFilters = filterOptions.clone();

        filterExecutor.execute(() -> {
            List<Supplier> filteredList = new ArrayList<>();
            for (Supplier supplier : snapshot) {
                if (supplier == null)
                    continue;

                boolean searchMatch = searchQuery.isEmpty() ||
                        (supplier.getName() != null
                                && supplier.getName().toLowerCase(Locale.getDefault()).contains(searchQuery))
                        ||
                        (supplier.getContactNumber() != null
                                && supplier.getContactNumber().toLowerCase(Locale.getDefault()).contains(searchQuery))
                        ||
                        (supplier.getAddress() != null
                                && supplier.getAddress().toLowerCase(Locale.getDefault()).contains(searchQuery))
                        ||
                        (supplier.getTaxId() != null
                                && supplier.getTaxId().toLowerCase(Locale.getDefault()).contains(searchQuery));

                // Apply additional filters
                boolean filterMatch = true;
                if (currentFilters[0] && supplier.getRating() <= 0)
                    filterMatch = false;
                if (currentFilters[1] && !supplier.getPreferredSupplier())
                    filterMatch = false;
                if (currentFilters[2] && !supplier.isActive())
                    filterMatch = false;

                if (searchMatch && filterMatch) {
                    filteredList.add(supplier);
                }
            }

            runOnUiThread(() -> {
                displayedSuppliersList.clear();
                displayedSuppliersList.addAll(filteredList);
                Log.d(TAG, "Final displayed suppliers count: " + displayedSuppliersList.size());
                selectableSupplierAdapter.setSuppliers(displayedSuppliersList);

                if (displayedSuppliersList.isEmpty()) {
                    if (!searchQuery.isEmpty()) {
                        // Optional toast
                    } else if (allSuppliersList.isEmpty()) {
                        Toast.makeText(this, "No suppliers in database.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    @Override
    public void onSupplierSelected(Supplier supplier) {
        Log.d(TAG, "=== SUPPLIER SELECTED ===");
        if (supplier == null) {
            Log.e(TAG, "ERROR: Supplier is NULL in onSupplierSelected");
            return;
        }

        Log.d(TAG, "Supplier selected: " + supplier.getName());
        Log.d(TAG, "Supplier ID: " + supplier.getDocumentId());

        Intent resultIntent = new Intent();
        String json = com.bsoft.inventorymanager.utils.SupplierSerializationHelper.serialize(supplier);
        resultIntent.putExtra(EXTRA_SELECTED_SUPPLIER, json);
        Log.d(TAG, "Putting supplier in intent with key: " + EXTRA_SELECTED_SUPPLIER);

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
