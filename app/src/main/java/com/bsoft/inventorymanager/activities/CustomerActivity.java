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
import com.bsoft.inventorymanager.adapters.CustomerAdapter;
import com.bsoft.inventorymanager.models.Customer;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerActivity extends AppCompatActivity {

    private RecyclerView customersRecyclerView;
    private CustomerAdapter adapter;
    private final List<Customer> customerList = new ArrayList<>();
    private FloatingActionButton addCustomerFab;
    private FirebaseFirestore db;
    private CollectionReference customersCollection;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageView dialogCustomerImage;
    private Uri imageUri;
    private String imageBase64;

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

        db = FirebaseFirestore.getInstance();
        customersCollection = db.collection("customers");

        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        addCustomerFab = findViewById(R.id.addCustomerFab);

        initializeLaunchers();

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

        loadCustomersFromFirestore();
    }

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && imageUri != null) {
                processImageUri(imageUri);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                processImageUri(uri);
            }
        });
    }

    private void processImageUri(Uri uri) {
        this.imageUri = uri;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            dialogCustomerImage.setImageBitmap(bitmap);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 100, 100);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            this.imageBase64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxHeight;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void launchCamera() {
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new android.content.ContentValues());
        takePictureLauncher.launch(imageUri);
    }

    private void loadCustomersFromFirestore() {
        customersCollection.whereEqualTo("isActive", true)
            .addSnapshotListener((value, error) -> {
            if (error != null) {
                android.util.Log.e("CustomerActivity", "Error fetching customers: " + error.getMessage(), error);
                Toast.makeText(this, "Error fetching customers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                customerList.clear();
                for (QueryDocumentSnapshot document : value) {
                    Customer customer = document.toObject(Customer.class);
                    customer.setDocumentId(document.getId());
                    customerList.add(customer);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddCustomerDialog() {
        AddEditCustomerSheet sheet = AddEditCustomerSheet.newInstance(null);
        // When the sheet dismisses, reload customers. Also, the sheet will handle adding the customer to Firestore.
        sheet.show(getSupportFragmentManager(), "AddEditCustomerSheet");
        // Listen for dismiss to detect newly added customers - AddEditCustomerSheet will write directly to Firestore.
    }

    private void showEditCustomerDialog(Customer customer) {
        if (customer == null || customer.getDocumentId() == null || customer.getDocumentId().isEmpty()) {
            Toast.makeText(CustomerActivity.this, "Error: Invalid customer data. Cannot edit.", Toast.LENGTH_LONG).show();
            return;
        }

        AddEditCustomerSheet sheet = AddEditCustomerSheet.newInstance(customer);
        sheet.show(getSupportFragmentManager(), "AddEditCustomerSheet");
    }

    private void showDeleteConfirmationDialog(Customer customer) {
        if (customer == null || customer.getDocumentId() == null || customer.getDocumentId().isEmpty()) {
            Toast.makeText(CustomerActivity.this, "Error: Invalid customer data. Cannot delete.", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setIcon(R.drawable.ic_customer)
                .setMessage("Are you sure you want to delete \"" + customer.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    customersCollection.document(customer.getDocumentId()).update("isActive", false)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CustomerActivity.this, "Customer deleted.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CustomerActivity.this, "Error deleting customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
