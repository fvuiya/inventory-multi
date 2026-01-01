package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Damage {

    @DocumentId
    private String documentId;
    private String productId;
    private String productName;
    private int quantity;
    private String reason;
    private String userId;
    private Timestamp date;

    public Damage() {
        // Default constructor for Firestore
    }

    public Damage(String productId, String productName, int quantity, String reason, String userId, Timestamp date) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.reason = reason;
        this.userId = userId;
        this.date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
