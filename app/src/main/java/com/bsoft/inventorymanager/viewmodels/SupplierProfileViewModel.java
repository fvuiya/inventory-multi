package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Supplier;
import com.google.firebase.firestore.FirebaseFirestore;

public class SupplierProfileViewModel extends ViewModel {

    private final MutableLiveData<Supplier> supplier = new MutableLiveData<>();

    public LiveData<Supplier> getSupplier() {
        return supplier;
    }

    public void loadSupplier(String supplierId) {
        FirebaseFirestore.getInstance().collection("suppliers").document(supplierId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Supplier sup = snapshot.toObject(Supplier.class);
                if (sup != null) {
                    sup.setDocumentId(snapshot.getId());
                    supplier.setValue(sup);
                }
            }
        });
    }
}
