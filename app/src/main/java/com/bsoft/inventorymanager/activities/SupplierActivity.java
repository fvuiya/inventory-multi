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
import com.bsoft.inventorymanager.models.Supplier;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SupplierActivity extends AppCompatActivity {

    private RecyclerView suppliersRecyclerView;
    private SupplierAdapter adapter;
    private final List<Supplier> supplierList = new ArrayList<>();
    private FloatingActionButton addSupplierFab;
    private FirebaseFirestore db;
    private CollectionReference suppliersCollection;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageView dialogSupplierImage;
    private Uri imageUri;
    private String imageBase64;

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

        db = FirebaseFirestore.getInstance();
        suppliersCollection = db.collection("suppliers");

        suppliersRecyclerView = findViewById(R.id.recycler_view_suppliers);
        addSupplierFab = findViewById(R.id.fab_add_supplier);

        initializeLaunchers();

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

        loadSuppliersFromFirestore();
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
            dialogSupplierImage.setImageBitmap(bitmap);
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

    private void loadSuppliersFromFirestore() {
        suppliersCollection.whereEqualTo("isActive", true)
            .addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            if (value != null) {
                supplierList.clear();
                for (QueryDocumentSnapshot document : value) {
                    Supplier supplier = document.toObject(Supplier.class);
                    supplier.setDocumentId(document.getId());
                    supplierList.add(supplier);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddSupplierDialog() {
        AddEditSupplierSheet sheet = AddEditSupplierSheet.newInstance(null);
        sheet.show(getSupportFragmentManager(), "AddEditSupplierSheet");
    }

    private void showEditSupplierDialog(Supplier supplier) {
        if (supplier == null || supplier.getDocumentId() == null || supplier.getDocumentId().isEmpty()) {
            return;
        }

        AddEditSupplierSheet sheet = AddEditSupplierSheet.newInstance(supplier);
        sheet.show(getSupportFragmentManager(), "AddEditSupplierSheet");
    }

    private void showDeleteConfirmationDialog(Supplier supplier) {
        if (supplier == null || supplier.getDocumentId() == null || supplier.getDocumentId().isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Supplier")
                .setMessage("Are you sure you want to delete \"" + supplier.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    suppliersCollection.document(supplier.getDocumentId()).update("isActive", false)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SupplierActivity.this, "Supplier deleted.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SupplierActivity.this, "Error deleting supplier: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
