package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class PurchaseItem implements ReturnableItem {
    private String productId;
    private String productName;
    private String productCode; // Product code for reference
    private int quantity;
    private double pricePerItem;
    private double totalPrice;
    private int returnedQuantity = 0; // Number of items already returned from this purchase
    private double taxRate; // Tax rate for this item
    private double discountRate; // Discount rate for this item
    private String batchNumber; // Batch number for tracking
    private Timestamp expiryDate; // Expiry date for this batch
    private String notes; // Additional notes for this item

    public PurchaseItem() {
    }

    public PurchaseItem(String productId, String productName, int quantity, double pricePerItem) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.totalPrice = quantity * pricePerItem;
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public double getPricePerItem() {
        return pricePerItem;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = quantity * this.pricePerItem;
    }

    public void setPricePerItem(double pricePerItem) {
        this.pricePerItem = pricePerItem;
        this.totalPrice = this.quantity * pricePerItem;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(int returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("quantity", quantity);
        map.put("pricePerItem", pricePerItem);
        map.put("totalPrice", totalPrice);
        map.put("returnedQuantity", returnedQuantity);
        return map;
    }
}
