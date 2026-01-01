package com.bsoft.inventorymanager.repository;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ProductRepository {

    private final FirebaseFirestore db;

    public ProductRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public DocumentReference getProduct(String productId) {
        return db.collection("products").document(productId);
    }

    public Query getSalesByProductId(String productId) {
        return db.collection("sales").whereArrayContains("items.productId", productId);
    }

    public Query getPurchasesByProductId(String productId) {
        return db.collection("purchases").whereArrayContains("items.productId", productId);
    }
}
