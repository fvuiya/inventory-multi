package com.bsoft.inventorymanager.reports.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.reports.ui.LowStockAdapter;
import com.bsoft.inventorymanager.reports.ui.SlowMovingAdapter;
import com.bsoft.inventorymanager.reports.viewmodels.ReportsViewModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ProductsFragment extends Fragment {

    private ReportsViewModel viewModel;
    private HorizontalBarChart topSellingProductsChart, mostProfitableProductsChart;
    private RecyclerView lowStockRecyclerView, slowMovingRecyclerView;
    private ProgressBar topSellingProgress, mostProfitableProgress, lowStockProgress, slowMovingProgress,
            totalInventoryValueProgress;
    private TextView topSellingNoData, mostProfitableNoData, lowStockNoData, slowMovingNoData, totalInventoryValueText,
            totalInventoryValueNoData;
    private ChipGroup timeframeChipGroup;
    // private Date startDate, endDate; // Removed

    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        totalInventoryValueText = view.findViewById(R.id.total_inventory_value_text);
        totalInventoryValueProgress = view.findViewById(R.id.total_inventory_value_progress);
        totalInventoryValueNoData = view.findViewById(R.id.total_inventory_value_no_data);
        topSellingProductsChart = view.findViewById(R.id.top_selling_products_chart);
        topSellingProgress = view.findViewById(R.id.top_selling_progress);
        topSellingNoData = view.findViewById(R.id.top_selling_no_data);
        mostProfitableProductsChart = view.findViewById(R.id.most_profitable_products_chart);
        mostProfitableProgress = view.findViewById(R.id.most_profitable_progress);
        mostProfitableNoData = view.findViewById(R.id.most_profitable_no_data);
        lowStockRecyclerView = view.findViewById(R.id.low_stock_recycler_view);
        lowStockProgress = view.findViewById(R.id.low_stock_progress);
        lowStockNoData = view.findViewById(R.id.low_stock_no_data);
        slowMovingRecyclerView = view.findViewById(R.id.slow_moving_recycler_view);
        slowMovingProgress = view.findViewById(R.id.slow_moving_progress);
        slowMovingNoData = view.findViewById(R.id.slow_moving_no_data);
        timeframeChipGroup = requireActivity().findViewById(R.id.timeframe_chip_group);

        setupCharts();
        setupRecyclerViews();
        // setupTimeframeChips(); // Listener moved to ReportsActivity
        observeViewModel();

        if (timeframeChipGroup.getCheckedChipId() != -1) {
            // setTimeframe(timeframeChipGroup.getCheckedChipId()); // Removed, use VM state
            loadProductCharts();
        } else {
            timeframeChipGroup.check(R.id.chip_week);
        }
    }

    private void setupCharts() {
        // Setup for Top-Selling Chart
        topSellingProductsChart.getDescription().setEnabled(false);
        XAxis topSellingXAxis = topSellingProductsChart.getXAxis();
        topSellingXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        topSellingXAxis.setGranularity(1f);
        topSellingXAxis.setDrawGridLines(false);
        topSellingProductsChart.getAxisLeft().setAxisMinimum(0f);
        topSellingProductsChart.getAxisRight().setEnabled(false);

        // Setup for Most-Profitable Chart
        mostProfitableProductsChart.getDescription().setEnabled(false);
        XAxis mostProfitableXAxis = mostProfitableProductsChart.getXAxis();
        mostProfitableXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mostProfitableXAxis.setGranularity(1f);
        mostProfitableXAxis.setDrawGridLines(false);
        mostProfitableProductsChart.getAxisLeft().setAxisMinimum(0f);
        mostProfitableProductsChart.getAxisRight().setEnabled(false);
    }

    private void setupRecyclerViews() {
        lowStockRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        slowMovingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // Listener moved to ReportsActivity

    private void observeViewModel() {
        viewModel.getTotalInventoryValueState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, totalInventoryValueText, totalInventoryValueProgress, totalInventoryValueNoData);
        });

        viewModel.getTotalInventoryValueData().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                totalInventoryValueText.setText(format.format(value));
            }
        });

        viewModel.getTopSellingProductsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, topSellingProductsChart, topSellingProgress, topSellingNoData);
        });

        viewModel.getTopSellingProductsData().observe(getViewLifecycleOwner(), entries -> {
            viewModel.getTopSellingProductsLabels().observe(getViewLifecycleOwner(), labels -> {
                if (entries != null && !entries.isEmpty() && labels != null && !labels.isEmpty()) {
                    BarDataSet dataSet = new BarDataSet(entries, "Quantity Sold");
                    BarData barData = new BarData(dataSet);
                    topSellingProductsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    topSellingProductsChart.setData(barData);
                } else {
                    topSellingProductsChart.clear();
                }
                topSellingProductsChart.invalidate();
            });
        });

        viewModel.getMostProfitableProductsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, mostProfitableProductsChart, mostProfitableProgress, mostProfitableNoData);
        });

        viewModel.getMostProfitableProductsData().observe(getViewLifecycleOwner(), entries -> {
            viewModel.getMostProfitableProductsLabels().observe(getViewLifecycleOwner(), labels -> {
                if (entries != null && !entries.isEmpty() && labels != null && !labels.isEmpty()) {
                    BarDataSet dataSet = new BarDataSet(entries, "Total Profit");
                    BarData barData = new BarData(dataSet);
                    mostProfitableProductsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    mostProfitableProductsChart.setData(barData);
                } else {
                    mostProfitableProductsChart.clear();
                }
                mostProfitableProductsChart.invalidate();
            });
        });

        viewModel.getLowStockProductsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, lowStockRecyclerView, lowStockProgress, lowStockNoData);
        });

        viewModel.getLowStockProductsData().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                LowStockAdapter adapter = new LowStockAdapter(products);
                lowStockRecyclerView.setAdapter(adapter);
            } else {
                lowStockRecyclerView.setAdapter(new LowStockAdapter(new ArrayList<>()));
            }
        });

        viewModel.getSlowMovingProductsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, slowMovingRecyclerView, slowMovingProgress, slowMovingNoData);
        });

        viewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                loadProductCharts();
            }
        });

        viewModel.getSlowMovingProductsData().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                SlowMovingAdapter adapter = new SlowMovingAdapter(products);
                slowMovingRecyclerView.setAdapter(adapter);
            } else {
                slowMovingRecyclerView.setAdapter(new SlowMovingAdapter(new ArrayList<>()));
            }
        });
    }

    private void updateChartUi(ReportsViewModel.UiState state, View chart, View progress, View noData) {
        progress.setVisibility(state == ReportsViewModel.UiState.LOADING ? View.VISIBLE : View.GONE);
        noData.setVisibility(state == ReportsViewModel.UiState.NO_DATA ? View.VISIBLE : View.GONE);
        chart.setVisibility(state == ReportsViewModel.UiState.HAS_DATA ? View.VISIBLE : View.GONE);
    }

    // Simplified setTimeframe (called by Activity refresh)
    public void setTimeframe(int checkedId) {
        loadProductCharts();
    }

    private void loadProductCharts() {
        viewModel.loadTotalInventoryValue();
        viewModel.loadLowStockProducts();

        Date startDate = viewModel.getCurrentStartDate();
        Date endDate = viewModel.getCurrentEndDate();

        if (startDate == null || endDate == null)
            return;

        viewModel.loadSlowMovingProducts(startDate, endDate);
        viewModel.loadTopSellingProducts(startDate, endDate);
        viewModel.loadMostProfitableProducts(startDate, endDate);
    }

    // Removed showDateRangePicker, getStartOfDay, getEndOfDay

}
