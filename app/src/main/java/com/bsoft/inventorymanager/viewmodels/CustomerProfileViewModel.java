package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Customer;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerProfileViewModel extends ViewModel {

    private final MutableLiveData<Customer> customer = new MutableLiveData<>();

    public LiveData<Customer> getCustomer() {
        return customer;
    }

    public void loadCustomer(String customerId) {
        FirebaseFirestore.getInstance().collection("customers").document(customerId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Customer cust = snapshot.toObject(Customer.class);
                if (cust != null) {
                    cust.setDocumentId(snapshot.getId());
                    customer.setValue(cust);
                }
            }
        });
    }
}
