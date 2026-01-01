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
import com.bsoft.inventorymanager.models.Customer;
import com.bsoft.inventorymanager.viewmodels.ActivityFeedViewModel;
import com.bsoft.inventorymanager.viewmodels.CustomerProfileViewModel;

public class CustomerProfileActivity extends AppCompatActivity {

    private CustomerProfileViewModel profileViewModel;
    private ActivityFeedViewModel activityViewModel;
    private ActivityEventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        profileViewModel = new ViewModelProvider(this).get(CustomerProfileViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityFeedViewModel.class);

        RecyclerView historyRecyclerView = findViewById(R.id.rv_activity_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new ActivityEventAdapter();
        historyRecyclerView.setAdapter(eventAdapter);

        String customerId = getIntent().getStringExtra("customer_id");

        profileViewModel.getCustomer().observe(this, this::updateUi);
        activityViewModel.getActivityEvents().observe(this, eventAdapter::submitList);

        if (customerId != null) {
            profileViewModel.loadCustomer(customerId);
            activityViewModel.loadCustomerActivity(customerId);
        }
    }

    private void updateUi(Customer customer) {
        if (customer == null) return;

        ImageView photo = findViewById(R.id.iv_customer_photo);
        TextView name = findViewById(R.id.tv_customer_name);
        TextView age = findViewById(R.id.tv_customer_age);
        TextView phone = findViewById(R.id.tv_customer_phone);
        TextView address = findViewById(R.id.tv_customer_address);

        name.setText(customer.getName());
        age.setText("Age: " + customer.getAge());
        phone.setText("Phone: " + customer.getContactNumber());
        address.setText("Address: " + customer.getAddress());

        if (customer.getPhoto() != null && !customer.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(customer.getPhoto(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            photo.setImageBitmap(decodedByte);
        }
    }
}
