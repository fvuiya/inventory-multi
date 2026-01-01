package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bsoft.inventorymanager.models.ActivityEvent;
import com.bsoft.inventorymanager.models.Damage;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.repository.ActivityRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityFeedViewModel extends ViewModel {

    private final ActivityRepository repository;
    private final MutableLiveData<List<ActivityEvent>> activityEvents = new MutableLiveData<>();

    public ActivityFeedViewModel() {
        this.repository = new ActivityRepository();
    }

    public LiveData<List<ActivityEvent>> getActivityEvents() {
        return activityEvents;
    }

    public void loadProductActivity(String productId) {
        Task<QuerySnapshot> salesTask = repository.getSalesByProductId(productId).get();
        Task<QuerySnapshot> purchasesTask = repository.getPurchasesByProductId(productId).get();
        Task<QuerySnapshot> damagesTask = repository.getDamagesByProductId(productId).get();

        Tasks.whenAllSuccess(salesTask, purchasesTask, damagesTask).addOnSuccessListener(results -> {
            List<ActivityEvent> combinedList = new ArrayList<>();

            QuerySnapshot salesSnapshot = (QuerySnapshot) results.get(0);
            for (Sale sale : salesSnapshot.toObjects(Sale.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.SALE, sale.getSaleDate(), sale));
            }

            QuerySnapshot purchasesSnapshot = (QuerySnapshot) results.get(1);
            for (Purchase purchase : purchasesSnapshot.toObjects(Purchase.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.PURCHASE, purchase.getPurchaseDate(), purchase));
            }

            QuerySnapshot damagesSnapshot = (QuerySnapshot) results.get(2);
            for (Damage damage : damagesSnapshot.toObjects(Damage.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.DAMAGE, damage.getDate(), damage));
            }

            Collections.sort(combinedList);
            activityEvents.setValue(combinedList);
        });
    }

    public void loadEmployeeActivity(String userId) {
        Task<QuerySnapshot> salesTask = repository.getSalesByUserId(userId).get();
        Task<QuerySnapshot> purchasesTask = repository.getPurchasesByUserId(userId).get();
        Task<QuerySnapshot> damagesTask = repository.getDamagesByUserId(userId).get();

        Tasks.whenAllSuccess(salesTask, purchasesTask, damagesTask).addOnSuccessListener(results -> {
            List<ActivityEvent> combinedList = new ArrayList<>();

            QuerySnapshot salesSnapshot = (QuerySnapshot) results.get(0);
            for (Sale sale : salesSnapshot.toObjects(Sale.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.SALE, sale.getSaleDate(), sale));
            }

            QuerySnapshot purchasesSnapshot = (QuerySnapshot) results.get(1);
            for (Purchase purchase : purchasesSnapshot.toObjects(Purchase.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.PURCHASE, purchase.getPurchaseDate(), purchase));
            }

            QuerySnapshot damagesSnapshot = (QuerySnapshot) results.get(2);
            for (Damage damage : damagesSnapshot.toObjects(Damage.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.DAMAGE, damage.getDate(), damage));
            }

            Collections.sort(combinedList);
            activityEvents.setValue(combinedList);
        });
    }

    public void loadCustomerActivity(String customerId) {
        repository.getSalesByCustomerId(customerId).get().addOnSuccessListener(salesSnapshot -> {
            List<ActivityEvent> combinedList = new ArrayList<>();
            for (Sale sale : salesSnapshot.toObjects(Sale.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.SALE, sale.getSaleDate(), sale));
            }
            Collections.sort(combinedList);
            activityEvents.setValue(combinedList);
        });
    }

    public void loadSupplierActivity(String supplierId) {
        repository.getPurchasesBySupplierId(supplierId).get().addOnSuccessListener(purchasesSnapshot -> {
            List<ActivityEvent> combinedList = new ArrayList<>();
            for (Purchase purchase : purchasesSnapshot.toObjects(Purchase.class)) {
                combinedList.add(new ActivityEvent(ActivityEvent.EventType.PURCHASE, purchase.getPurchaseDate(), purchase));
            }
            Collections.sort(combinedList);
            activityEvents.setValue(combinedList);
        });
    }
}
