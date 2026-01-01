package com.bsoft.inventorymanager.reports.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.reports.ui.fragments.CustomerFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.ProductsFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.PurchaseFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.SalesFragment;
import com.bsoft.inventorymanager.reports.viewmodels.ReportsViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.atomic.AtomicInteger;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReportsActivity extends AppCompatActivity {

    private ReportsViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private final AtomicInteger refreshCounter = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshCurrentFragment);

        observeViewModel();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Financial");
                            break;
                        case 1:
                            tab.setText("Sales");
                            break;
                        case 2:
                            tab.setText("Products");
                            break;
                        case 3:
                            tab.setText("Customers");
                            break;
                        case 4:
                            tab.setText("Purchases");
                            break;
                    }
                }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                refreshCurrentFragment();
            }
        });

        ChipGroup chipGroup = findViewById(R.id.timeframe_chip_group);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_custom) {
                showDateRangePicker();
            } else if (checkedId != -1) {
                calculateAndSetDateRange(checkedId);
                refreshCurrentFragment();
            }
        });

        // Trigger initial load based on default selection from XML or force default
        if (chipGroup.getCheckedChipId() != -1) {
            calculateAndSetDateRange(chipGroup.getCheckedChipId());
            refreshCurrentFragment();
        } else {
            chipGroup.check(R.id.chip_today);
        }
    }

    private void observeViewModel() {
        viewModel.getSalesOverTimeState().observe(this, this::handleRefreshState);
        viewModel.getProfitOverTimeState().observe(this, this::handleRefreshState);
        viewModel.getAverageOrderValueState().observe(this, this::handleRefreshState);
        viewModel.getSalesByCategoryState().observe(this, this::handleRefreshState);
        viewModel.getTotalRevenueState().observe(this, this::handleRefreshState);
        viewModel.getTotalProfitState().observe(this, this::handleRefreshState);
        viewModel.getTotalExpensesState().observe(this, this::handleRefreshState);
        viewModel.getNetProfitState().observe(this, this::handleRefreshState);
        viewModel.getTotalTransactionsState().observe(this, this::handleRefreshState);
        viewModel.getTopSellingProductsState().observe(this, this::handleRefreshState);
        viewModel.getMostProfitableProductsState().observe(this, this::handleRefreshState);
        viewModel.getLowStockProductsState().observe(this, this::handleRefreshState);
        viewModel.getSlowMovingProductsState().observe(this, this::handleRefreshState);
        viewModel.getTotalInventoryValueState().observe(this, this::handleRefreshState);
        viewModel.getTopCustomersState().observe(this, this::handleRefreshState);
        viewModel.getNewVsReturningState().observe(this, this::handleRefreshState);
        viewModel.getCustomerAcquisitionState().observe(this, this::handleRefreshState);
        viewModel.getLapsedCustomersState().observe(this, this::handleRefreshState);
        viewModel.getTotalSpendBySupplierState().observe(this, this::handleRefreshState);
        viewModel.getPurchaseOrdersOverTimeState().observe(this, this::handleRefreshState);
        viewModel.getPurchaseOrdersOverTimeVolumeState().observe(this, this::handleRefreshState);
        viewModel.getTopPurchasedProductsState().observe(this, this::handleRefreshState);
    }

    private void handleRefreshState(ReportsViewModel.UiState state) {
        if (swipeRefreshLayout.isRefreshing() && state != ReportsViewModel.UiState.LOADING) {
            if (refreshCounter.decrementAndGet() <= 0) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void refreshCurrentFragment() {
        // Trigger a refresh in the ViewModel, which all active fragments observe
        viewModel.triggerRefresh();

        // Handle SwipeRefreshLayout visualization
        if (viewModel.getCurrentStartDate() != null) {
            // Set expected counters based on the current tab to manage the spinner
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == 0)
                refreshCounter.set(3); // Financial
            else if (currentItem == 1)
                refreshCounter.set(7); // Sales
            else if (currentItem == 2)
                refreshCounter.set(5); // Products
            else if (currentItem == 3)
                refreshCounter.set(4); // Customers
            else if (currentItem == 4)
                refreshCounter.set(4); // Purchases
            else
                swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showDateRangePicker() {
        com.google.android.material.datepicker.MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .dateRangePicker();
        builder.setTitleText("Select date range");

        com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            // 1. Get the Date (Year/Month/Day) from the Picker's UTC selection
            java.util.Calendar utcStart = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcStart.setTimeInMillis(selection.first);

            java.util.Calendar utcEnd = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcEnd.setTimeInMillis(selection.second);

            // 2. Apply that Date to a Local Calendar to get Local Start/End of Day
            java.util.Calendar localStart = java.util.Calendar.getInstance();
            localStart.set(utcStart.get(java.util.Calendar.YEAR), utcStart.get(java.util.Calendar.MONTH),
                    utcStart.get(java.util.Calendar.DAY_OF_MONTH));
            getStartOfDay(localStart);

            java.util.Calendar localEnd = java.util.Calendar.getInstance();
            localEnd.set(utcEnd.get(java.util.Calendar.YEAR), utcEnd.get(java.util.Calendar.MONTH),
                    utcEnd.get(java.util.Calendar.DAY_OF_MONTH));
            getEndOfDay(localEnd);

            viewModel.setDateRange(localStart.getTime(), localEnd.getTime());
            refreshCurrentFragment();
        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void calculateAndSetDateRange(int checkedId) {
        // Use Local TimeZone for standard "Today", "Week", "Month" chips
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date startDate = null;
        java.util.Date endDate = null;

        if (checkedId == R.id.chip_today) {
            startDate = getStartOfDay(cal).getTime();
            endDate = getEndOfDay(cal).getTime();
        } else if (checkedId == R.id.chip_week) {
            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            startDate = getStartOfDay(cal).getTime();
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            endDate = getEndOfDay(cal).getTime();
        } else if (checkedId == R.id.chip_month) {
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            startDate = getStartOfDay(cal).getTime();
            cal.add(java.util.Calendar.MONTH, 1);
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
            endDate = getEndOfDay(cal).getTime();
        }

        if (startDate != null && endDate != null) {
            viewModel.setDateRange(startDate, endDate);
        }
    }

    private java.util.Calendar getStartOfDay(java.util.Calendar cal) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal;
    }

    private java.util.Calendar getEndOfDay(java.util.Calendar cal) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal;
    }
}
