package com.bsoft.inventorymanager.reports.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.reports.viewmodels.ReportsViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FinancialFragment extends RefreshableFragment {

    private ReportsViewModel viewModel;
    private TextView tvRevenue, tvGrossProfit, tvExpenses, tvNetProfit;
    // private int currentChipId = R.id.chip_week; // Removed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_financial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRevenue = view.findViewById(R.id.tv_total_revenue);
        tvGrossProfit = view.findViewById(R.id.tv_gross_profit);
        tvExpenses = view.findViewById(R.id.tv_total_expenses);
        tvNetProfit = view.findViewById(R.id.tv_net_profit);

        viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        observeViewModel();

        // Initial load
        // Initial load relies on Activity/VM state
        // setTimeframe(R.id.chip_week); // Removed
        loadData();
    }

    private void observeViewModel() {
        viewModel.getTotalRevenueData().observe(getViewLifecycleOwner(),
                value -> tvRevenue.setText(String.format(Locale.US, "$%.2f", value)));

        viewModel.getTotalProfitData().observe(getViewLifecycleOwner(),
                value -> tvGrossProfit.setText(String.format(Locale.US, "$%.2f", value)));

        viewModel.getTotalExpensesData().observe(getViewLifecycleOwner(),
                value -> tvExpenses.setText(String.format(Locale.US, "$%.2f", value)));

        viewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                setTimeframe(0); // Trigger reload
            }
        });

        viewModel.getNetProfitData().observe(getViewLifecycleOwner(),
                value -> tvNetProfit.setText(String.format(Locale.US, "$%.2f", value)));
    }

    // Removed @Override
    // Simplified setTimeframe (called by Activity refresh)
    public void setTimeframe(int chipId) {
        loadData();
    }

    private void loadData() {
        Date startDate = viewModel.getCurrentStartDate();
        Date endDate = viewModel.getCurrentEndDate();

        if (startDate == null || endDate == null)
            return;

        viewModel.loadTotalRevenue(startDate, endDate);
        viewModel.loadTotalProfit(startDate, endDate);
        viewModel.loadTotalExpenses(startDate, endDate);
    }

    @Override
    protected void onRefresh() {
        loadData();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
