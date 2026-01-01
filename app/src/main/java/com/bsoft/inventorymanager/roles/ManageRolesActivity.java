package com.bsoft.inventorymanager.roles;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageRolesActivity extends AppCompatActivity {

    private ManageRolesViewModel viewModel;
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_roles);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Roles");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ManageRolesViewModel.class);

        RecyclerView employeesRecyclerView = findViewById(R.id.employeesRecyclerView);
        FloatingActionButton addEmployeeFab = findViewById(R.id.addEmployeeFab);

        adapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeActionListener() {
            @Override
            public void onEdit(Employee employee) {
                AddEditEmployeeSheet.newInstance(employee).show(getSupportFragmentManager(), "add_edit_sheet");
            }

            @Override
            public void onDelete(Employee employee) {
                showDeleteConfirmationDialog(employee);
            }
        });
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        employeesRecyclerView.setHasFixedSize(true);
        employeesRecyclerView.setAdapter(adapter);

        addEmployeeFab.setOnClickListener(v -> AddEditEmployeeSheet.newInstance(null).show(getSupportFragmentManager(), "add_edit_sheet"));

        viewModel.getEmployees().observe(this, employees -> adapter.submitList(employees));
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Employee employee) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Employee")
                .setIcon(R.drawable.ic_customer)
                .setMessage(getString(R.string.delete_employee_confirmation, employee.getName()))
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteEmployee(employee))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
