package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.CustomerAdapter;
import com.bsoft.inventorymanager.model.Customer;
import com.bsoft.inventorymanager.viewmodels.CustomerViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CustomerActivity extends AppCompatActivity {

    private RecyclerView customersRecyclerView;
    private CustomerAdapter adapter;
    private final List<Customer> customerList = new ArrayList<>();
    private FloatingActionButton addCustomerFab;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_customer);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        addCustomerFab = findViewById(R.id.addCustomerFab);

        adapter = new CustomerAdapter(customerList, new CustomerAdapter.OnCustomerActionListener() {
            @Override
            public void onEdit(int position, Customer customer) {
                if (position >= 0 && position < customerList.size()) {
                    showEditCustomerDialog(customerList.get(position));
                }
            }

            @Override
            public void onDelete(int position, Customer customer) {
                if (position >= 0 && position < customerList.size()) {
                    showDeleteConfirmationDialog(customerList.get(position));
                }
            }
        });
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customersRecyclerView.setAdapter(adapter);

        addCustomerFab.setOnClickListener(v -> showAddCustomerDialog());

        observeViewModel();

        viewModel.loadCustomers(); // Helper to start loading
    }

    private void observeViewModel() {
        viewModel.getCustomers().observe(this, customers -> {
            if (customers != null) {
                customerList.clear();
                customerList.addAll(customers);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                // Operation (save/delete) successful, list will auto-refresh
            } else if (isSuccess != null && !isSuccess) {
                // Error handled by error observer
            }
        });
    }

    private void showAddCustomerDialog() {
        AddEditCustomerSheet sheet = AddEditCustomerSheet.newInstance(null);
        sheet.show(getSupportFragmentManager(), "AddEditCustomerSheet");
        // The sheet will handle saving via its own ViewModel or callback, but we need
        // to refresh the list when it closes
        // For now, we rely on the sheet to potentially trigger a refresh, or we can
        // listen for fragment results.
        // Given the current architecture, the Sheet likely updates Firestore directly
        // or via its own ViewModel.
        // If it updates Firestore, our loadCustomers() call needs to happen *after* the
        // sheet closes.
        // A better pattern is to use setFragmentResultListener.

        getSupportFragmentManager().setFragmentResultListener("customer_update", this, (requestKey, result) -> {
            viewModel.loadCustomers();
        });
    }

    private void showEditCustomerDialog(Customer customer) {
        if (customer == null || customer.getDocumentId().isEmpty()) {
            Toast.makeText(CustomerActivity.this, "Error: Invalid customer data. Cannot edit.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        AddEditCustomerSheet sheet = AddEditCustomerSheet.newInstance(customer);
        sheet.show(getSupportFragmentManager(), "AddEditCustomerSheet");

        getSupportFragmentManager().setFragmentResultListener("customer_update", this, (requestKey, result) -> {
            viewModel.loadCustomers();
        });
    }


    private void showDeleteConfirmationDialog(Customer customer) {
        if (customer == null || customer.getDocumentId().isEmpty()) {
            Toast.makeText(CustomerActivity.this, "Error: Invalid customer data. Cannot delete.", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setIcon(R.drawable.ic_customer)
                .setMessage("Are you sure you want to delete \"" + customer.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteCustomer(customer.getDocumentId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
