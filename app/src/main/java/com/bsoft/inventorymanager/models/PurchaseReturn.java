package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class PurchaseReturn {
    private String documentId;
    private String originalPurchaseId;
    private String supplierId;
    private String supplierName;
    private Timestamp returnDate;
    private List<PurchaseReturnItem> items;
    private double totalCreditAmount;
    private String userId;

    // Constructors, Getters, Setters

    public PurchaseReturn() {}

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOriginalPurchaseId() {
        return originalPurchaseId;
    }

    public void setOriginalPurchaseId(String originalPurchaseId) {
        this.originalPurchaseId = originalPurchaseId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returnDate) {
        this.returnDate = returnDate;
    }

    public List<PurchaseReturnItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseReturnItem> items) {
        this.items = items;
    }

    public double getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public void setTotalCreditAmount(double totalCreditAmount) {
        this.totalCreditAmount = totalCreditAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
