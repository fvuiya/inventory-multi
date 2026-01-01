package com.bsoft.inventorymanager.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ActivityRepository {

    private final FirebaseFirestore db;

    public ActivityRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Query getSalesByProductId(String productId) {
        return db.collection("sales").whereArrayContains("productIds", productId);
    }

    public Query getPurchasesByProductId(String productId) {
        return db.collection("purchases").whereArrayContains("productIds", productId);
    }

    public Query getDamagesByProductId(String productId) {
        return db.collection("damages").whereEqualTo("productId", productId);
    }

    public Query getSalesByUserId(String userId) {
        return db.collection("sales").whereEqualTo("userId", userId);
    }

    public Query getPurchasesByUserId(String userId) {
        return db.collection("purchases").whereEqualTo("userId", userId);
    }

    public Query getDamagesByUserId(String userId) {
        return db.collection("damages").whereEqualTo("userId", userId);
    }

    public Query getSalesByCustomerId(String customerId) {
        return db.collection("sales").whereEqualTo("customerId", customerId);
    }

    public Query getPurchasesBySupplierId(String supplierId) {
        return db.collection("purchases").whereEqualTo("supplierId", supplierId);
    }
}
