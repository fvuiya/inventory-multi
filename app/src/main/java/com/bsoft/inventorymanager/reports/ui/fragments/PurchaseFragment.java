package com.bsoft.inventorymanager.reports.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.reports.viewmodels.ReportsViewModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PurchaseFragment extends Fragment {

    private ReportsViewModel viewModel;
    private HorizontalBarChart totalSpendBySupplierChart, topPurchasedProductsChart;
    private LineChart purchaseOrdersOverTimeChart, purchaseOrdersOverTimeVolumeChart;
    private ProgressBar totalSpendBySupplierProgress, purchaseOrdersOverTimeProgress,
            purchaseOrdersOverTimeVolumeProgress, topPurchasedProductsProgress;
    private TextView totalSpendBySupplierNoData, purchaseOrdersOverTimeNoData, purchaseOrdersOverTimeVolumeNoData,
            topPurchasedProductsNoData;
    private ChipGroup timeframeChipGroup;
    // private Date startDate, endDate; // Removed

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        topPurchasedProductsChart = view.findViewById(R.id.top_purchased_products_chart);
        topPurchasedProductsProgress = view.findViewById(R.id.top_purchased_products_progress);
        topPurchasedProductsNoData = view.findViewById(R.id.top_purchased_products_no_data);
        totalSpendBySupplierChart = view.findViewById(R.id.total_spend_by_Supplier_chart);
        totalSpendBySupplierProgress = view.findViewById(R.id.total_spend_by_Supplier_progress);
        totalSpendBySupplierNoData = view.findViewById(R.id.total_spend_by_Supplier_no_data);
        purchaseOrdersOverTimeChart = view.findViewById(R.id.purchase_orders_over_time_chart);
        purchaseOrdersOverTimeProgress = view.findViewById(R.id.purchase_orders_over_time_progress);
        purchaseOrdersOverTimeNoData = view.findViewById(R.id.purchase_orders_over_time_no_data);
        purchaseOrdersOverTimeVolumeChart = view.findViewById(R.id.purchase_orders_over_time_volume_chart);
        purchaseOrdersOverTimeVolumeProgress = view.findViewById(R.id.purchase_orders_over_time_volume_progress);
        purchaseOrdersOverTimeVolumeNoData = view.findViewById(R.id.purchase_orders_over_time_volume_no_data);
        timeframeChipGroup = requireActivity().findViewById(R.id.timeframe_chip_group);

        setupCharts();
        // setupTimeframeChips(); // Listener moved to ReportsActivity
        observeViewModel();

        if (timeframeChipGroup.getCheckedChipId() != -1) {
            // setTimeframe(timeframeChipGroup.getCheckedChipId()); // Removed, use VM state
            loadAllCharts();
        } else {
            timeframeChipGroup.check(R.id.chip_week);
        }
    }

    private void setupCharts() {
        topPurchasedProductsChart.getDescription().setEnabled(false);
        XAxis topPurchasedXAxis = topPurchasedProductsChart.getXAxis();
        topPurchasedXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        topPurchasedXAxis.setGranularity(1f);
        topPurchasedXAxis.setDrawGridLines(false);
        topPurchasedProductsChart.getAxisLeft().setAxisMinimum(0f);
        topPurchasedProductsChart.getAxisRight().setEnabled(false);

        totalSpendBySupplierChart.getDescription().setEnabled(false);
        XAxis xAxis = totalSpendBySupplierChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        totalSpendBySupplierChart.getAxisLeft().setAxisMinimum(0f);
        totalSpendBySupplierChart.getAxisRight().setEnabled(false);

        purchaseOrdersOverTimeChart.getDescription().setEnabled(false);
        XAxis purchaseOrdersOverTimeXAxis = purchaseOrdersOverTimeChart.getXAxis();
        purchaseOrdersOverTimeXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        purchaseOrdersOverTimeXAxis.setGranularity(1f);
        purchaseOrdersOverTimeXAxis
                .setValueFormatter(new DateValueFormatter(new Date(), ReportsViewModel.Granularity.DAY, false));

        // Volume Chart
        purchaseOrdersOverTimeVolumeChart.getDescription().setEnabled(false);
        XAxis volumeXAxis = purchaseOrdersOverTimeVolumeChart.getXAxis();
        volumeXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        volumeXAxis.setGranularity(1f);
        volumeXAxis.setValueFormatter(new DateValueFormatter(new Date(), ReportsViewModel.Granularity.DAY, false));
    }

    // Listener moved to ReportsActivity

    private void observeViewModel() {
        viewModel.getTopPurchasedProductsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, topPurchasedProductsChart, topPurchasedProductsProgress, topPurchasedProductsNoData);
        });

        viewModel.getTopPurchasedProductsData().observe(getViewLifecycleOwner(), entries -> {
            viewModel.getTopPurchasedProductsLabels().observe(getViewLifecycleOwner(), labels -> {
                if (entries != null && !entries.isEmpty() && labels != null && !labels.isEmpty()) {
                    BarDataSet dataSet = new BarDataSet(entries, "Quantity Purchased");
                    BarData barData = new BarData(dataSet);
                    topPurchasedProductsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    topPurchasedProductsChart.setData(barData);
                } else {
                    topPurchasedProductsChart.clear();
                }
                topPurchasedProductsChart.invalidate();
            });
        });

        viewModel.getTotalSpendBySupplierState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, totalSpendBySupplierChart, totalSpendBySupplierProgress, totalSpendBySupplierNoData);
        });

        viewModel.getTotalSpendBySupplierData().observe(getViewLifecycleOwner(), entries -> {
            viewModel.getTotalSpendBySupplierLabels().observe(getViewLifecycleOwner(), labels -> {
                if (entries != null && !entries.isEmpty() && labels != null && !labels.isEmpty()) {
                    BarDataSet dataSet = new BarDataSet(entries, "Total Spend");
                    BarData barData = new BarData(dataSet);
                    totalSpendBySupplierChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    totalSpendBySupplierChart.setData(barData);
                } else {
                    totalSpendBySupplierChart.clear();
                }
                totalSpendBySupplierChart.invalidate();
            });
        });

        viewModel.getPurchaseOrdersOverTimeState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, purchaseOrdersOverTimeChart, purchaseOrdersOverTimeProgress,
                    purchaseOrdersOverTimeNoData);
        });

        viewModel.getPurchaseOrdersOverTimeData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Purchase Orders (Value)");
                LineData lineData = new LineData(dataSet);
                purchaseOrdersOverTimeChart.setData(lineData);
            } else {
                purchaseOrdersOverTimeChart.clear();
            }
            purchaseOrdersOverTimeChart.invalidate();
        });

        viewModel.getPurchaseOrdersOverTimeVolumeState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, purchaseOrdersOverTimeVolumeChart, purchaseOrdersOverTimeVolumeProgress,
                    purchaseOrdersOverTimeVolumeNoData);
        });

        viewModel.getPurchaseOrdersOverTimeVolumeData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Purchase Orders (Volume)");
                LineData lineData = new LineData(dataSet);
                purchaseOrdersOverTimeVolumeChart.setData(lineData);
            } else {
                purchaseOrdersOverTimeVolumeChart.clear();
            }
            purchaseOrdersOverTimeVolumeChart.invalidate();
        });

        viewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                loadAllCharts();
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
        loadAllCharts();
    }

    private void loadAllCharts() {
        Date startDate = viewModel.getCurrentStartDate();
        Date endDate = viewModel.getCurrentEndDate();

        if (startDate == null || endDate == null)
            return;

        ReportsViewModel.Granularity granularity = viewModel.getCurrentGranularity().getValue();
        if (granularity == null)
            granularity = ReportsViewModel.Granularity.DAY;

        // Check if we need to show date in Minute granularity (i.e., multi-day range)
        long diff = endDate.getTime() - startDate.getTime();
        boolean showDate = diff > TimeUnit.HOURS.toMillis(26);

        DateValueFormatter formatter = new DateValueFormatter(startDate, granularity, showDate);
        purchaseOrdersOverTimeChart.getXAxis().setValueFormatter(formatter);
        purchaseOrdersOverTimeVolumeChart.getXAxis().setValueFormatter(formatter);
        viewModel.loadTopPurchasedProducts(startDate, endDate);
        viewModel.loadTotalSpendBySupplier(startDate, endDate);
        viewModel.loadPurchaseOrdersOverTime(startDate, endDate);
        viewModel.loadPurchaseOrdersOverTimeVolume(startDate, endDate);
    }

    // Removed showDateRangePicker, getStartOfDay, getEndOfDay

    private static class DateValueFormatter extends ValueFormatter {
        private final SimpleDateFormat dateFormat;
        private final long startMillis;
        private final ReportsViewModel.Granularity granularity;

        DateValueFormatter(Date startDate, ReportsViewModel.Granularity granularity, boolean showDate) {
            this.startMillis = startDate.getTime();
            this.granularity = granularity;
            if (granularity == ReportsViewModel.Granularity.MINUTE) {
                if (showDate) {
                    this.dateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
                } else {
                    this.dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
                }
            } else if (granularity == ReportsViewModel.Granularity.HOUR) {
                this.dateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
            } else {
                this.dateFormat = new SimpleDateFormat("MMM dd", Locale.US);
            }
        }

        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            long multiplier;
            if (granularity == ReportsViewModel.Granularity.MINUTE) {
                multiplier = TimeUnit.MINUTES.toMillis(1);
            } else if (granularity == ReportsViewModel.Granularity.HOUR) {
                multiplier = TimeUnit.HOURS.toMillis(1);
            } else {
                multiplier = TimeUnit.DAYS.toMillis(1);
            }
            long millis = startMillis + (long) (value * multiplier);
            return dateFormat.format(new Date(millis));
        }
    }
}
