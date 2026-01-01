package com.bsoft.inventorymanager.reports.ui.fragments;

import android.graphics.Color;
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
import android.widget.Toast;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.reports.ui.LapsedCustomerAdapter;
import com.bsoft.inventorymanager.reports.viewmodels.ReportsViewModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CustomerFragment extends Fragment {

    private ReportsViewModel viewModel;
    private HorizontalBarChart topCustomersChart;
    private PieChart newVsReturningChart;
    private LineChart customerAcquisitionChart;
    private RecyclerView lapsedCustomersRecyclerView;
    private ProgressBar topCustomersProgress, newVsReturningProgress, customerAcquisitionProgress,
            lapsedCustomersProgress;
    private TextView topCustomersNoData, newVsReturningNoData, customerAcquisitionNoData, lapsedCustomersNoData;
    private ChipGroup timeframeChipGroup;
    // private Date startDate, endDate; // Removed
    private List<String> topCustomerLabels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        topCustomersChart = view.findViewById(R.id.top_customers_chart);
        topCustomersProgress = view.findViewById(R.id.top_customers_progress);
        topCustomersNoData = view.findViewById(R.id.top_customers_no_data);
        newVsReturningChart = view.findViewById(R.id.new_vs_returning_chart);
        newVsReturningProgress = view.findViewById(R.id.new_vs_returning_progress);
        newVsReturningNoData = view.findViewById(R.id.new_vs_returning_no_data);
        customerAcquisitionChart = view.findViewById(R.id.customer_acquisition_chart);
        customerAcquisitionProgress = view.findViewById(R.id.customer_acquisition_progress);
        customerAcquisitionNoData = view.findViewById(R.id.customer_acquisition_no_data);
        lapsedCustomersRecyclerView = view.findViewById(R.id.lapsed_customers_recycler_view);
        lapsedCustomersProgress = view.findViewById(R.id.lapsed_customers_progress);
        lapsedCustomersNoData = view.findViewById(R.id.lapsed_customers_no_data);
        timeframeChipGroup = requireActivity().findViewById(R.id.timeframe_chip_group);

        setupCharts();
        setupRecyclerViews();
        // setupTimeframeChips(); // Listener moved to ReportsActivity
        observeViewModel();

        if (timeframeChipGroup.getCheckedChipId() != -1) {
            // setTimeframe(timeframeChipGroup.getCheckedChipId()); // Removed
            loadAllCharts();
        } else {
            timeframeChipGroup.check(R.id.chip_week);
        }
    }

    private void setupCharts() {
        // Top Customers Chart
        topCustomersChart.getDescription().setEnabled(false);
        XAxis xAxis = topCustomersChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        topCustomersChart.getAxisLeft().setAxisMinimum(0f);
        topCustomersChart.getAxisRight().setEnabled(false);

        topCustomersChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (topCustomerLabels != null && e != null) {
                    int index = (int) e.getX();
                    if (index >= 0 && index < topCustomerLabels.size()) {
                        String customerName = topCustomerLabels.get(topCustomerLabels.size() - 1 - index);
                        Toast.makeText(getContext(), customerName, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // New vs Returning Chart
        newVsReturningChart.setUsePercentValues(true);
        newVsReturningChart.getDescription().setEnabled(false);
        newVsReturningChart.setExtraOffsets(5, 10, 5, 5);
        newVsReturningChart.setDrawHoleEnabled(true);
        newVsReturningChart.setHoleColor(Color.TRANSPARENT);
        newVsReturningChart.setTransparentCircleRadius(61f);

        // Customer Acquisition Chart
        customerAcquisitionChart.getDescription().setEnabled(false);
        XAxis xAxisAcquisition = customerAcquisitionChart.getXAxis();
        xAxisAcquisition.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisAcquisition.setGranularity(1f);
        xAxisAcquisition.setValueFormatter(new DateValueFormatter(new Date(), ReportsViewModel.Granularity.DAY, false));
    }

    private void setupRecyclerViews() {
        lapsedCustomersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // Listener moved to ReportsActivity

    private void observeViewModel() {
        viewModel.getTopCustomersState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, topCustomersChart, topCustomersProgress, topCustomersNoData);
        });

        viewModel.getTopCustomersLabels().observe(getViewLifecycleOwner(), labels -> {
            this.topCustomerLabels = labels;
        });

        viewModel.getTopCustomersData().observe(getViewLifecycleOwner(), entries -> {
            viewModel.getTopCustomersLabels().observe(getViewLifecycleOwner(), labels -> {
                if (entries != null && !entries.isEmpty() && labels != null && !labels.isEmpty()) {
                    BarDataSet dataSet = new BarDataSet(entries, "Total Spend");
                    BarData barData = new BarData(dataSet);
                    topCustomersChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    topCustomersChart.setData(barData);
                } else {
                    topCustomersChart.clear();
                }
                topCustomersChart.invalidate();
            });
        });

        viewModel.getNewVsReturningState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, newVsReturningChart, newVsReturningProgress, newVsReturningNoData);
        });

        viewModel.getNewVsReturningData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setSliceSpace(3f);
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                PieData pieData = new PieData(dataSet);
                pieData.setValueFormatter(new PercentFormatter(newVsReturningChart));
                pieData.setValueTextSize(12f);
                pieData.setValueTextColor(Color.BLACK);
                newVsReturningChart.setData(pieData);
            } else {
                newVsReturningChart.clear();
            }
            newVsReturningChart.invalidate();
        });

        viewModel.getCustomerAcquisitionState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, customerAcquisitionChart, customerAcquisitionProgress, customerAcquisitionNoData);
        });

        viewModel.getCustomerAcquisitionData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "New Customers");
                LineData lineData = new LineData(dataSet);
                customerAcquisitionChart.setData(lineData);
            } else {
                customerAcquisitionChart.clear();
            }
            customerAcquisitionChart.invalidate();
        });

        viewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                loadAllCharts();
            }
        });

        viewModel.getLapsedCustomersState().observe(getViewLifecycleOwner(), state -> {
            updateChartUi(state, lapsedCustomersRecyclerView, lapsedCustomersProgress, lapsedCustomersNoData);
        });

        viewModel.getLapsedCustomersData().observe(getViewLifecycleOwner(), customers -> {
            if (customers != null && !customers.isEmpty()) {
                LapsedCustomerAdapter adapter = new LapsedCustomerAdapter(customers);
                lapsedCustomersRecyclerView.setAdapter(adapter);
            } else {
                lapsedCustomersRecyclerView.setAdapter(new LapsedCustomerAdapter(new ArrayList<>()));
            }
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
        viewModel.loadLapsedCustomers();

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

        customerAcquisitionChart.getXAxis().setValueFormatter(new DateValueFormatter(startDate, granularity, showDate));
        viewModel.loadTopCustomersBySpend(startDate, endDate);
        viewModel.loadNewVsReturningCustomers(startDate, endDate);
        viewModel.loadCustomerAcquisitionOverTime(startDate, endDate);
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
