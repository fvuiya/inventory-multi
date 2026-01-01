package com.bsoft.inventorymanager.utils;

import android.util.Log;

import com.bsoft.inventorymanager.roles.CurrentUser;
import com.bsoft.inventorymanager.roles.Employee;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TransactionManager {
    
    private static final String TAG = "TransactionManager";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    /**
     * Performs a transaction to update product inventory with audit trail
     * @param productId The product ID to update
     * @param newQuantity The new quantity for the product
     * @param transactionType Type of transaction (sale, purchase, adjustment)
     * @param transactionCallback Callback for transaction result
     */
    public static void updateProductInventoryWithAudit(String productId, int newQuantity, 
                                                      String transactionType, TransactionCallback transactionCallback) {
        db.runTransaction(transaction -> {
            DocumentReference productRef = db.collection("products").document(productId);
            Map<String, Object> productSnapshot = transaction.get(productRef).getData();
            
            if (productSnapshot == null) {
                throw new RuntimeException("Product not found: " + productId);
            }
            
            // Get current quantity
            Long currentQuantityLong = (Long) productSnapshot.get("quantity");
            int currentQuantity = currentQuantityLong != null ? currentQuantityLong.intValue() : 0;
            
            // Update the product quantity
            transaction.update(productRef, "quantity", newQuantity);
            
            // Create audit trail entry
            Map<String, Object> auditEntry = new HashMap<>();
            auditEntry.put("productId", productId);
            auditEntry.put("previousQuantity", currentQuantity);
            auditEntry.put("newQuantity", newQuantity);
            auditEntry.put("transactionType", transactionType);
            auditEntry.put("timestamp", Timestamp.now());
            auditEntry.put("userId", CurrentUser.getInstance().getEmployee().getDocumentId());
            auditEntry.put("userName", CurrentUser.getInstance().getEmployee().getName());
            
            // Add audit entry to audit trail collection
            transaction.set(db.collection("audit_trail").document(), auditEntry);
            
            return true; // Success
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Transaction successful for product: " + productId);
            if (transactionCallback != null) {
                transactionCallback.onSuccess("Inventory updated successfully");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failed for product: " + productId, e);
            if (transactionCallback != null) {
                transactionCallback.onError("Transaction failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Performs a transaction to update multiple products (e.g., for a sale)
     * @param productUpdates Map of product IDs to new quantities
     * @param transactionType Type of transaction
     * @param transactionCallback Callback for transaction result
     */
    public static void updateMultipleProductsTransaction(Map<String, Integer> productUpdates,
                                                        String transactionType, TransactionCallback transactionCallback) {
        db.runTransaction(transaction -> {
            for (Map.Entry<String, Integer> entry : productUpdates.entrySet()) {
                String productId = entry.getKey();
                int newQuantity = entry.getValue();
                
                DocumentReference productRef = db.collection("products").document(productId);
                Map<String, Object> productSnapshot = transaction.get(productRef).getData();
                
                if (productSnapshot == null) {
                    throw new RuntimeException("Product not found: " + productId);
                }
                
                // Get current quantity
                Long currentQuantityLong = (Long) productSnapshot.get("quantity");
                int currentQuantity = currentQuantityLong != null ? currentQuantityLong.intValue() : 0;
                
                // Update the product quantity
                transaction.update(productRef, "quantity", newQuantity);
                
                // Create audit trail entry for each product
                Map<String, Object> auditEntry = new HashMap<>();
                auditEntry.put("productId", productId);
                auditEntry.put("previousQuantity", currentQuantity);
                auditEntry.put("newQuantity", newQuantity);
                auditEntry.put("transactionType", transactionType);
                auditEntry.put("timestamp", Timestamp.now());
                auditEntry.put("userId", CurrentUser.getInstance().getEmployee().getDocumentId());
                auditEntry.put("userName", CurrentUser.getInstance().getEmployee().getName());
                
                // Add audit entry to audit trail collection
                transaction.set(db.collection("audit_trail").document(), auditEntry);
            }
            
            return true; // Success
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Multiple product transaction successful");
            if (transactionCallback != null) {
                transactionCallback.onSuccess("All products updated successfully");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Multiple product transaction failed", e);
            if (transactionCallback != null) {
                transactionCallback.onError("Transaction failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Performs a financial transaction with proper validation and audit trail
     * @param transactionData Map containing transaction details
     * @param transactionCallback Callback for transaction result
     */
    public static void performFinancialTransaction(Map<String, Object> transactionData,
                                                  TransactionCallback transactionCallback) {
        db.runTransaction(transaction -> {
            // Add transaction to financial transactions collection
            transactionData.put("timestamp", Timestamp.now());
            transactionData.put("userId", CurrentUser.getInstance().getEmployee().getDocumentId());
            transactionData.put("userName", CurrentUser.getInstance().getEmployee().getName());
            
            // Create transaction document
            DocumentReference transactionRef = db.collection("financial_transactions").document();
            transaction.set(transactionRef, transactionData);
            
            // Create audit trail entry
            Map<String, Object> auditEntry = new HashMap<>();
            auditEntry.put("transactionId", transactionRef.getId());
            auditEntry.put("transactionType", transactionData.get("type"));
            auditEntry.put("amount", transactionData.get("amount"));
            auditEntry.put("timestamp", Timestamp.now());
            auditEntry.put("userId", CurrentUser.getInstance().getEmployee().getDocumentId());
            auditEntry.put("userName", CurrentUser.getInstance().getEmployee().getName());
            
            // Add audit entry
            transaction.set(db.collection("audit_trail").document(), auditEntry);
            
            return true; // Success
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Financial transaction successful");
            if (transactionCallback != null) {
                transactionCallback.onSuccess("Financial transaction completed successfully");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Financial transaction failed", e);
            if (transactionCallback != null) {
                transactionCallback.onError("Financial transaction failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Interface for transaction callbacks
     */
    public interface TransactionCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}