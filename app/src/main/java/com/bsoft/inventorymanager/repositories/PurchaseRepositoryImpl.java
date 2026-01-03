package com.bsoft.inventorymanager.repositories;

import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.model.Supplier;
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
                        // Manual mapping to handle Timestamp -> Long conversion
                        Supplier s = new Supplier();
                        s.setDocumentId(doc.getId());
                        s.setName(doc.getString("name"));
                        s.setContactNumber(doc.getString("contactNumber"));
                        s.setAddress(doc.getString("address"));
                        s.setAge(doc.getLong("age") != null ? doc.getLong("age").intValue() : 0);
                        s.setPhoto(doc.getString("photo"));
                        s.setActive(Boolean.TRUE.equals(doc.getBoolean("isActive")));
                        s.setRating(doc.getDouble("rating") != null ? doc.getDouble("rating") : 0.0);
                        s.setPaymentTerms(doc.getString("paymentTerms"));
                        s.setLeadTime(doc.getLong("leadTime") != null ? doc.getLong("leadTime").intValue() : 0);
                        s.setPerformanceScore(
                                doc.getDouble("performanceScore") != null ? doc.getDouble("performanceScore") : 0.0);
                        s.setPreferredSupplier(Boolean.TRUE.equals(doc.getBoolean("preferredSupplier")));
                        s.setOutstandingPayment(
                                doc.getDouble("outstandingPayment") != null ? doc.getDouble("outstandingPayment")
                                        : 0.0);
                        s.setContractDetails(doc.getString("contractDetails"));
                        s.setProductsSupplied(doc.getString("productsSupplied"));

                        // Handle Timestamps safely (can be Timestamp or Long)
                        Object lastDeliveryObj = doc.get("lastDeliveryDate");
                        long lastDeliveryTime = 0;
                        if (lastDeliveryObj instanceof com.google.firebase.Timestamp) {
                            lastDeliveryTime = ((com.google.firebase.Timestamp) lastDeliveryObj).toDate().getTime();
                        } else if (lastDeliveryObj instanceof Number) {
                            lastDeliveryTime = ((Number) lastDeliveryObj).longValue();
                        }
                        s.setLastDeliveryDate(lastDeliveryTime);

                        s.setBankAccount(doc.getString("bankAccount"));
                        s.setTaxId(doc.getString("taxId"));

                        Object creationObj = doc.get("creationDate");
                        long creationTime = 0;
                        if (creationObj instanceof com.google.firebase.Timestamp) {
                            creationTime = ((com.google.firebase.Timestamp) creationObj).toDate().getTime();
                        } else if (creationObj instanceof Number) {
                            creationTime = ((Number) creationObj).longValue();
                        }
                        s.setCreationDate(creationTime);

                        s.setTotalSupplyAmount(
                                doc.getDouble("totalSupplyAmount") != null ? doc.getDouble("totalSupplyAmount") : 0.0);
                        s.setSupplyFrequency(
                                doc.getLong("supplyFrequency") != null ? doc.getLong("supplyFrequency").intValue() : 0);
                        s.setSupplierType(doc.getString("supplierType"));
                        s.setSupplierTier(doc.getString("supplierTier"));

                        suppliers.add(s);
                    }
                    callback.onSuccess(suppliers);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
