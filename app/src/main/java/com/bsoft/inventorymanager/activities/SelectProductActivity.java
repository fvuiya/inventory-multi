package com.bsoft.inventorymanager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectableProductAdapter;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.viewmodels.SelectProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectProductActivity extends AppCompatActivity
        implements SelectableProductAdapter.OnProductSelectedListener {

    private static final String TAG = "SelectProductActivity";
    public static final String EXTRA_SELECTED_PRODUCT = "com.bsoft.inventorymanager.activities.EXTRA_SELECTED_PRODUCT";
    public static final String EXTRA_IS_PURCHASE = "com.bsoft.inventorymanager.activities.EXTRA_IS_PURCHASE";

    private EditText searchEditText;
    private Spinner brandSpinner, categorySpinner;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView productsRecyclerView;
    private FloatingActionButton fabSearch;

    private SelectProductViewModel viewModel;
    private SelectableProductAdapter selectableProductAdapter;
    private ArrayAdapter<String> brandAdapter, categoryAdapter;

    private final List<String> brandList = new ArrayList<>();
    private final List<String> categoryList = new ArrayList<>();
    private final com.bsoft.inventorymanager.utils.Debouncer debouncer = new com.bsoft.inventorymanager.utils.Debouncer(
            300);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);

        viewModel = new ViewModelProvider(this).get(SelectProductViewModel.class);

        searchEditText = findViewById(R.id.search_product_edittext);
        brandSpinner = findViewById(R.id.brand_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_select_product);
        productsRecyclerView = findViewById(R.id.products_recycler_view_select_product);
        fabSearch = findViewById(R.id.fab_search_product);

        setupRecyclerView();
        setupSpinners();
        setupSearch();
        setupSwipeRefresh();
        observeViewModel();

        viewModel.initMetadata();
        viewModel.resetPaginationAndLoad();
    }

    private void setupRecyclerView() {
        boolean isPurchase = getIntent().getBooleanExtra(EXTRA_IS_PURCHASE, false);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectableProductAdapter = new SelectableProductAdapter(new ArrayList<>(), this, isPurchase);
        productsRecyclerView.setAdapter(selectableProductAdapter);

        productsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { // check for scroll down
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null
                            && layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 5) {
                        viewModel.loadNextPage();
                    }
                }
            }
        });
    }

    private void setupSpinners() {
        brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brandList);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandSpinner.setAdapter(brandAdapter);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                triggerFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        brandSpinner.setOnItemSelectedListener(spinnerListener);
        categorySpinner.setOnItemSelectedListener(spinnerListener);
    }

    private void triggerFilters() {
        String brand = brandSpinner.getSelectedItem() != null ? brandSpinner.getSelectedItem().toString()
                : "All Brands";
        String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString()
                : "All Categories";
        String search = searchEditText.getText().toString();
        viewModel.setFilters(brand, category, search);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                debouncer.debounce(SelectProductActivity.this::triggerFilters);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fabSearch.setOnClickListener(v -> {
            searchEditText.post(() -> {
                if (searchEditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        });

        searchEditText.setOnClickListener(v -> {
            if (v.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.resetPaginationAndLoad();
        });
    }

    private void observeViewModel() {
        viewModel.getProductsList().observe(this, products -> {
            selectableProductAdapter.setProducts(products);
            swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getBrandsList().observe(this, brands -> {
            brandList.clear();
            brandList.addAll(brands);
            brandAdapter.notifyDataSetChanged();
        });

        viewModel.getCategoriesList().observe(this, categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            categoryAdapter.notifyDataSetChanged();
        });

        viewModel.getIsLoading().observe(this, loading -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                // Show/hide a separate progress indicator if desired
            }
        });
    }

    @Override
    public void onProductSelected(Product product) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_PRODUCT, (android.os.Parcelable) product);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
