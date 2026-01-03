package com.bsoft.inventorymanager.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SupplierAdapter;
import com.bsoft.inventorymanager.model.Supplier;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@dagger.hilt.android.AndroidEntryPoint
public class SupplierActivity extends AppCompatActivity {

    private RecyclerView suppliersRecyclerView;
    private SupplierAdapter adapter;
    private final List<Supplier> supplierList = new ArrayList<>();
    private FloatingActionButton addSupplierFab;
    private com.bsoft.inventorymanager.viewmodels.SupplierViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_supplier);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.bsoft.inventorymanager.viewmodels.SupplierViewModel.class);

        suppliersRecyclerView = findViewById(R.id.recycler_view_suppliers);
        addSupplierFab = findViewById(R.id.fab_add_supplier);

        adapter = new SupplierAdapter(supplierList, new SupplierAdapter.OnSupplierActionListener() {
            @Override
            public void onEdit(int position, Supplier supplier) {
                if (position >= 0 && position < supplierList.size()) {
                    showEditSupplierDialog(supplierList.get(position));
                }
            }

            @Override
            public void onDelete(int position, Supplier supplier) {
                if (position >= 0 && position < supplierList.size()) {
                    showDeleteConfirmationDialog(supplierList.get(position));
                }
            }
        });
        suppliersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suppliersRecyclerView.setAdapter(adapter);

        addSupplierFab.setOnClickListener(v -> showAddSupplierDialog());

        viewModel.getSuppliers().observe(this, suppliers -> {
            supplierList.clear();
            if (suppliers != null) {
                supplierList.addAll(suppliers);
            }
            adapter.notifyDataSetChanged();
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            // Optional: Show loading indicator
        });

        viewModel.loadSuppliers();
    }

    private void showAddSupplierDialog() {
        AddEditSupplierSheet sheet = AddEditSupplierSheet.newInstance(null);
        sheet.show(getSupportFragmentManager(), "AddEditSupplierSheet");
    }

    private void showEditSupplierDialog(Supplier supplier) {
        if (supplier == null || supplier.getDocumentId().isEmpty()) {
            return;
        }

        AddEditSupplierSheet sheet = AddEditSupplierSheet.newInstance(supplier);
        sheet.show(getSupportFragmentManager(), "AddEditSupplierSheet");
    }

    private void showDeleteConfirmationDialog(Supplier supplier) {
        if (supplier == null || supplier.getDocumentId().isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Supplier")
                .setMessage("Are you sure you want to delete \"" + supplier.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSupplier(supplier.getDocumentId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
