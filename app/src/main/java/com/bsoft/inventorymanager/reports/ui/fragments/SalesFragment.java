package com.bsoft.inventorymanager.reports.ui.fragments;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SalesFragment extends Fragment {

    private ReportsViewModel viewModel;
    private LineChart salesOverTimeChart, profitOverTimeChart;
    private PieChart salesByCategoryChart;
    private ProgressBar salesChartProgress, categoryChartProgress, profitChartProgress, averageOrderValueProgress,
            totalRevenueProgress, totalProfitProgress, totalTransactionsProgress;
    private TextView salesChartNoData, categoryChartNoData, profitChartNoData, averageOrderValueText,
            averageOrderValueNoData, totalRevenueText, totalRevenueNoData, totalProfitText, totalProfitNoData,
            totalTransactionsText, totalTransactionsNoData;
    private ChipGroup timeframeChipGroup;
    // private Date startDate, endDate; // Removed local dates

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        totalRevenueText = view.findViewById(R.id.total_revenue_text);
        totalRevenueProgress = view.findViewById(R.id.total_revenue_progress);
        totalRevenueNoData = view.findViewById(R.id.total_revenue_no_data);
        totalProfitText = view.findViewById(R.id.total_profit_text);
        totalProfitProgress = view.findViewById(R.id.total_profit_progress);
        totalProfitNoData = view.findViewById(R.id.total_profit_no_data);
        totalTransactionsText = view.findViewById(R.id.total_transactions_text);
        totalTransactionsProgress = view.findViewById(R.id.total_transactions_progress);
        totalTransactionsNoData = view.findViewById(R.id.total_transactions_no_data);
        averageOrderValueText = view.findViewById(R.id.average_order_value_text);
        averageOrderValueProgress = view.findViewById(R.id.average_order_value_progress);
        averageOrderValueNoData = view.findViewById(R.id.average_order_value_no_data);
        salesOverTimeChart = view.findViewById(R.id.sales_over_time_chart);
        salesChartProgress = view.findViewById(R.id.sales_chart_progress);
        salesChartNoData = view.findViewById(R.id.sales_chart_no_data);
        profitOverTimeChart = view.findViewById(R.id.profit_over_time_chart);
        profitChartProgress = view.findViewById(R.id.profit_chart_progress);
        profitChartNoData = view.findViewById(R.id.profit_chart_no_data);
        salesByCategoryChart = view.findViewById(R.id.sales_by_category_chart);
        categoryChartProgress = view.findViewById(R.id.category_chart_progress);
        categoryChartNoData = view.findViewById(R.id.category_chart_no_data);
        timeframeChipGroup = requireActivity().findViewById(R.id.timeframe_chip_group);

        setupCharts();
        // setupTimeframeChips(); // Removed as listener is now in Activity
        observeViewModel();

        if (timeframeChipGroup.getCheckedChipId() != -1) {
            // setTimeframe(timeframeChipGroup.getCheckedChipId()); // Removed, use VM state
            loadAllCharts();
        } else {
            timeframeChipGroup.check(R.id.chip_week);
            // Default check will trigger listener in Activity if not already checked
        }
    }

    private void setupCharts() {
        // Sales Line Chart
        salesOverTimeChart.getDescription().setEnabled(false);
        XAxis salesXAxis = salesOverTimeChart.getXAxis();
        salesXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        salesXAxis.setGranularity(1f);
        salesXAxis.setValueFormatter(new DateValueFormatter(new Date(), ReportsViewModel.Granularity.DAY, false));
        salesOverTimeChart.getAxisLeft().setDrawGridLines(true);

        // Profit Over Time Chart
        profitOverTimeChart.getDescription().setEnabled(false);
        XAxis profitXAxis = profitOverTimeChart.getXAxis();
        profitXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        profitXAxis.setDrawGridLines(false);
        profitXAxis.setValueFormatter(new DateValueFormatter(new Date(), ReportsViewModel.Granularity.DAY, false));

        // Pie Chart
        salesByCategoryChart.setUsePercentValues(true);
        salesByCategoryChart.getDescription().setEnabled(false);
        salesByCategoryChart.setExtraOffsets(5, 10, 5, 5);
        salesByCategoryChart.setDrawHoleEnabled(true);
        salesByCategoryChart.setHoleColor(Color.TRANSPARENT);
        salesByCategoryChart.setTransparentCircleRadius(61f);
        salesByCategoryChart.getLegend().setEnabled(false);
    }

    // Listener moved to ReportsActivity

    private void observeViewModel() {
        viewModel.getTotalRevenueState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, totalRevenueText, totalRevenueProgress, totalRevenueNoData);
        });

        viewModel.getTotalRevenueData().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                totalRevenueText.setText(format.format(value));
            }
        });

        viewModel.getTotalProfitState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, totalProfitText, totalProfitProgress, totalProfitNoData);
        });

        viewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                loadAllCharts();
            }
        });

        viewModel.getTotalProfitData().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                totalProfitText.setText(format.format(value));
            }
        });

        viewModel.getTotalTransactionsState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, totalTransactionsText, totalTransactionsProgress, totalTransactionsNoData);
        });

        viewModel.getTotalTransactionsData().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                totalTransactionsText.setText(String.valueOf(value));
            }
        });

        viewModel.getAverageOrderValueState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, averageOrderValueText, averageOrderValueProgress, averageOrderValueNoData);
        });

        viewModel.getAverageOrderValueData().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance();
                averageOrderValueText.setText(format.format(value));
            }
        });

        viewModel.getSalesOverTimeState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, salesOverTimeChart, salesChartProgress, salesChartNoData);
        });

        viewModel.getSalesOverTimeData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Sales");
                LineData lineData = new LineData(dataSet);
                salesOverTimeChart.setData(lineData);
            } else {
                salesOverTimeChart.clear();
            }
            salesOverTimeChart.invalidate();
        });

        viewModel.getProfitOverTimeState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, profitOverTimeChart, profitChartProgress, profitChartNoData);
        });

        viewModel.getProfitOverTimeData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Profit");
                LineData lineData = new LineData(dataSet);
                profitOverTimeChart.setData(lineData);
            } else {
                profitOverTimeChart.clear();
            }
            profitOverTimeChart.invalidate();
        });

        viewModel.getSalesByCategoryState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, salesByCategoryChart, categoryChartProgress, categoryChartNoData);
        });

        viewModel.getSalesByCategoryData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setSliceSpace(3f);
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                PieData pieData = new PieData(dataSet);
                pieData.setValueFormatter(new PercentFormatter(salesByCategoryChart));
                pieData.setValueTextSize(12f);
                pieData.setValueTextColor(Color.BLACK);
                salesByCategoryChart.setData(pieData);
            } else {
                salesByCategoryChart.clear();
            }
            salesByCategoryChart.invalidate();
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
        salesOverTimeChart.getXAxis().setValueFormatter(formatter);
        profitOverTimeChart.getXAxis().setValueFormatter(formatter);
        viewModel.loadTotalRevenue(startDate, endDate);
        viewModel.loadTotalProfit(startDate, endDate);
        viewModel.loadTotalTransactions(startDate, endDate);
        viewModel.loadAverageOrderValue(startDate, endDate);
        viewModel.loadSalesOverTime(startDate, endDate);
        viewModel.loadProfitOverTime(startDate, endDate);
        viewModel.loadSalesByCategory(startDate, endDate);
    }

    // showDateRangePicker removed
    // Helper methods getStartOfDay/getEndOfDay removed

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
