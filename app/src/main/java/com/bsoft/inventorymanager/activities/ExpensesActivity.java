package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ExpenseAdapter;
import com.bsoft.inventorymanager.models.Expense;
import com.bsoft.inventorymanager.viewmodels.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpensesActivity extends BaseActivity
        implements ExpenseAdapter.OnExpenseInteractionListener, AddEditExpenseSheet.OnExpenseSavedListener {

    private MainViewModel viewModel;
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        Toolbar toolbar = findViewById(R.id.toolbar_expenses);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_expenses);
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshData());

        setupRecyclerView();
        setupFAB();
        observeViewModel();

        // Initial load only if needed (optional optimization, but good practice)
        if (viewModel.getExpenses().getValue() == null || viewModel.getExpenses().getValue().isEmpty()) {
            viewModel.loadNextPageExpenses();
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_expenses);
        adapter = new ExpenseAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadNextPageExpenses();
                }
            }
        });
    }

    private void setupFAB() {
        FloatingActionButton fab = findViewById(R.id.fab_add_expense);
        fab.setOnClickListener(v -> {
            AddEditExpenseSheet sheet = AddEditExpenseSheet.newInstance(null);
            sheet.show(getSupportFragmentManager(), "AddExpenseSheet");
        });
    }

    private void observeViewModel() {
        viewModel.getExpenses().observe(this, expenses -> {
            adapter.setExpenses(expenses);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
    }

    @Override
    public void onDeleteExpense(Expense expense) {
        showLoadingIndicator("Deleting...");
        viewModel.deleteExpense(expense.getDocumentId(),
                new com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback() {
                    @Override
                    public void onSuccess() {
                        hideLoadingIndicator();
                        Toast.makeText(ExpensesActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                        viewModel.refreshData();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        hideLoadingIndicator();
                        Toast.makeText(ExpensesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEditExpense(Expense expense) {
        AddEditExpenseSheet sheet = AddEditExpenseSheet.newInstance(expense);
        sheet.show(getSupportFragmentManager(), "EditExpenseSheet");
    }

    @Override
    public void onExpenseSaved(Expense expense) {
        showLoadingIndicator("Saving...");
        viewModel.saveExpense(expense, new com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess() {
                hideLoadingIndicator();
                Toast.makeText(ExpensesActivity.this, "Expense saved", Toast.LENGTH_SHORT).show();
                viewModel.refreshData();
            }

            @Override
            public void onFailure(Exception e) {
                hideLoadingIndicator();
                Toast.makeText(ExpensesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
