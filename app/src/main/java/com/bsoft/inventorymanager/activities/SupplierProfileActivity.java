package com.bsoft.inventorymanager.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ActivityEventAdapter;
import com.bsoft.inventorymanager.model.Supplier;
import com.bsoft.inventorymanager.viewmodels.ActivityFeedViewModel;
import com.bsoft.inventorymanager.viewmodels.SupplierProfileViewModel;

@dagger.hilt.android.AndroidEntryPoint
public class SupplierProfileActivity extends AppCompatActivity {

    private SupplierProfileViewModel profileViewModel;
    private ActivityFeedViewModel activityViewModel;
    private ActivityEventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_profile);

        profileViewModel = new ViewModelProvider(this).get(SupplierProfileViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityFeedViewModel.class);

        RecyclerView historyRecyclerView = findViewById(R.id.rv_activity_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new ActivityEventAdapter();
        historyRecyclerView.setAdapter(eventAdapter);

        String supplierId = getIntent().getStringExtra("supplier_id");

        profileViewModel.getSupplier().observe(this, this::updateUi);
        activityViewModel.getActivityEvents().observe(this, eventAdapter::submitList);

        if (supplierId != null) {
            profileViewModel.loadSupplier(supplierId);
            activityViewModel.loadSupplierActivity(supplierId);
        }
    }

    private void updateUi(Supplier supplier) {
        if (supplier == null)
            return;

        ImageView photo = findViewById(R.id.iv_supplier_photo);
        TextView name = findViewById(R.id.tv_supplier_name);
        TextView age = findViewById(R.id.tv_supplier_age);
        TextView phone = findViewById(R.id.tv_supplier_phone);
        TextView address = findViewById(R.id.tv_supplier_address);

        name.setText(supplier.getName());
        age.setText("Age: " + supplier.getAge());
        phone.setText("Phone: " + supplier.getContactNumber());
        address.setText("Address: " + supplier.getAddress());

        TextView typeTier = findViewById(R.id.tv_supplier_type_tier);
        String type = supplier.getSupplierType() != null ? supplier.getSupplierType() : "Standard";
        String tier = supplier.getSupplierTier() != null ? supplier.getSupplierTier() : "Bronze";
        typeTier.setText(type + " • " + tier);

        TextView stats = findViewById(R.id.tv_supplier_stats);
        stats.setText(String.format("Total Supplied: ৳%.2f (%d)", supplier.getTotalSupplyAmount(),
                supplier.getSupplyFrequency()));

        if (supplier.getPhoto() != null && !supplier.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(supplier.getPhoto(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            photo.setImageBitmap(decodedByte);
        }
    }
}
