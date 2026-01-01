package com.bsoft.inventorymanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.MultiSelectCustomerAdapter;
import com.bsoft.inventorymanager.models.Customer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendMessageActivity extends AppCompatActivity {

    private EditText editTextMinAge;
    private EditText editTextMaxAge;
    private EditText editTextAddress;
    private Button buttonFilter;
    private RecyclerView customersRecyclerView;
    private Button buttonSend;

    private FirebaseFirestore db;
    private List<Customer> allCustomers;
    private List<Customer> filteredCustomers;
    private MultiSelectCustomerAdapter adapter;
    private String offerDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        editTextMinAge = findViewById(R.id.editTextMinAge);
        editTextMaxAge = findViewById(R.id.editTextMaxAge);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonFilter = findViewById(R.id.buttonFilter);
        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        buttonSend = findViewById(R.id.buttonSend);

        db = FirebaseFirestore.getInstance();
        allCustomers = new ArrayList<>();
        filteredCustomers = new ArrayList<>();
        adapter = new MultiSelectCustomerAdapter(filteredCustomers);

        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customersRecyclerView.setAdapter(adapter);

        offerDescription = getIntent().getStringExtra("offer_description");

        loadAllCustomers();

        buttonFilter.setOnClickListener(v -> filterCustomers());

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void loadAllCustomers() {
        db.collection("customers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allCustomers.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Customer customer = document.toObject(Customer.class);
                    customer.setDocumentId(document.getId());
                    allCustomers.add(customer);
                }
                filterCustomers(); // Initially, show all customers
            }
        });
    }

    private void filterCustomers() {
        String minAgeStr = editTextMinAge.getText().toString();
        String maxAgeStr = editTextMaxAge.getText().toString();
        String address = editTextAddress.getText().toString().trim();

        int minAge = -1;
        if (!minAgeStr.isEmpty()) {
            minAge = Integer.parseInt(minAgeStr);
        }

        int maxAge = -1;
        if (!maxAgeStr.isEmpty()) {
            maxAge = Integer.parseInt(maxAgeStr);
        }

        final int finalMinAge = minAge;
        final int finalMaxAge = maxAge;

        filteredCustomers.clear();
        filteredCustomers.addAll(allCustomers.stream()
                .filter(customer -> (finalMinAge == -1 || customer.getAge() >= finalMinAge))
                .filter(customer -> (finalMaxAge == -1 || customer.getAge() <= finalMaxAge))
                .filter(customer -> (address.isEmpty() || customer.getAddress().toLowerCase().contains(address.toLowerCase())))
                .collect(Collectors.toList()));

        adapter.setCustomers(filteredCustomers);
    }

    private void sendMessage() {
        List<Customer> selectedCustomers = adapter.getSelectedCustomers();
        if (selectedCustomers.isEmpty()) {
            return; // No customers selected
        }

        List<String> phoneNumbers = selectedCustomers.stream()
                .map(Customer::getContactNumber)
                .collect(Collectors.toList());

        String phones = TextUtils.join(";", phoneNumbers);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phones));
        intent.putExtra("sms_body", offerDescription);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
