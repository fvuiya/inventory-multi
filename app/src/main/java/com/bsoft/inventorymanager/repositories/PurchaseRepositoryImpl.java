package com.bsoft.inventorymanager.repositories;

import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.models.Supplier;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private final FirebaseFirestore db;

    @Inject
    public PurchaseRepositoryImpl(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void savePurchase(Purchase purchase, List<PurchaseItem> items, PurchaseCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference purchaseRef;

        if (purchase.getDocumentId() == null || purchase.getDocumentId().isEmpty()) {
            purchaseRef = db.collection("purchases").document();
            purchase.setDocumentId(purchaseRef.getId());
        } else {
            purchaseRef = db.collection("purchases").document(purchase.getDocumentId());
        }

        batch.set(purchaseRef, purchase); // Save the Purchase document

        // Update stock for each item (INCREMENT for purchases)
        for (PurchaseItem item : items) {
            if (item.getProductId() != null) {
                DocumentReference productRef = db.collection("products").document(item.getProductId());
                batch.update(productRef, "quantity", FieldValue.increment(item.getQuantity()));
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(purchase.getDocumentId()))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updatePurchase(Purchase purchase, List<PurchaseItem> items, PurchaseCallback callback) {
        // Similar to updateSale, stock logic on update is complex.
        // For now, mirroring SaleRepositoryImpl approach of basic save, but
        // acknowledged complexity.
        // In a real scenario, we'd need to diff existing items to revert stock.

        WriteBatch batch = db.batch();
        DocumentReference purchaseRef = db.collection("purchases").document(purchase.getDocumentId());
        batch.set(purchaseRef, purchase);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(purchase.getDocumentId()))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getPurchase(String purchaseId, GetPurchaseCallback callback) {
        db.collection("purchases").document(purchaseId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Purchase purchase = documentSnapshot.toObject(Purchase.class);
                        if (purchase != null) {
                            purchase.setDocumentId(documentSnapshot.getId());
                            callback.onSuccess(purchase);
                        } else {
                            callback.onFailure(new Exception("Purchase data error"));
                        }
                    } else {
                        callback.onFailure(new Exception("Purchase not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getProductByBarcode(String barcode, ProductCallback callback) {
        db.collection("products")
                .whereEqualTo("productCode", barcode)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Product product = querySnapshot.getDocuments().get(0).toObject(Product.class);
                        if (product != null) {
                            product.setDocumentId(querySnapshot.getDocuments().get(0).getId());
                            callback.onSuccess(product);
                        } else {
                            callback.onFailure(new Exception("Product not found"));
                        }
                    } else {
                        callback.onFailure(new Exception("Product not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getProduct(String productId, ProductCallback callback) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setDocumentId(documentSnapshot.getId());
                            callback.onSuccess(product);
                        } else {
                            callback.onFailure(new Exception("Product data error"));
                        }
                    } else {
                        callback.onFailure(new Exception("Product not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getSuppliers(SuppliersCallback callback) {
        db.collection("suppliers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Supplier> suppliers = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Supplier s = doc.toObject(Supplier.class);
                        s.setDocumentId(doc.getId());
                        suppliers.add(s);
                    }
                    callback.onSuccess(suppliers);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
