package com.bsoft.inventorymanager.models;

import com.google.firebase.firestore.Exclude; // Ensure this is present if you use documentId like this
import java.util.Date;
import java.util.List; // Assuming you might have OrderItems later

public class Order {
    @Exclude private String documentId; // For Firestore document ID

    private String customerId;
    private Date orderDate; // This should be java.util.Date
    private String status;
    private double totalAmount;
    // private List<OrderItem> items; // Uncomment if you have OrderItem class and list

    // Default constructor is required for Firestore deserialization
    public Order() {
    }

    // Constructor without items list (if you're not using it yet)
    public Order(String customerId, Date orderDate, String status, double totalAmount) {
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    // Constructor with items list (if you have OrderItems)
    /*
    public Order(String customerId, Date orderDate, String status, double totalAmount, List<OrderItem> items) {
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
    }
    */

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    @Exclude
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Date getOrderDate() { // Should return java.util.Date
        return orderDate;
    }

    public void setOrderDate(Date orderDate) { // Should accept java.util.Date
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /*
    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    */
}
