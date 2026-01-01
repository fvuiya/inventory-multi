package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class NotificationsViewModel extends ViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private ListenerRegistration listenerRegistration;

    @Inject
    public NotificationsViewModel(FirebaseFirestore db) {
        this.db = db;
        startListening();
    }

    private void startListening() {
        isLoading.setValue(true);
        listenerRegistration = db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limit to last 50 notifications
                .addSnapshotListener((value, error) -> {
                    isLoading.setValue(false);
                    if (error != null) {
                        // Handle error
                        return;
                    }

                    if (value != null) {
                        List<Notification> list = value.toObjects(Notification.class);
                        notifications.setValue(list);
                    }
                });
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
