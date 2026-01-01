package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class SaleReturn {
    private String documentId;
    private String originalSaleId;
    private String customerId;
    private String customerName;
    private Timestamp returnDate;
    private List<SaleReturnItem> items;
    private double totalRefundAmount;
    private String userId;

    // Constructors, Getters, Setters

    public SaleReturn() {}

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOriginalSaleId() {
        return originalSaleId;
    }

    public void setOriginalSaleId(String originalSaleId) {
        this.originalSaleId = originalSaleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returnDate) {
        this.returnDate = returnDate;
    }

    public List<SaleReturnItem> getItems() {
        return items;
    }

    public void setItems(List<SaleReturnItem> items) {
        this.items = items;
    }

    public double getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(double totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
