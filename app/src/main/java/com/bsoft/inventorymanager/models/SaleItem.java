package com.bsoft.inventorymanager.models;

import java.util.HashMap;
import java.util.Map;

public class SaleItem implements ReturnableItem {
    private String productId;
    private String productName;
    private int quantity;
    private double pricePerItem; // Price of the item at the time of sale
    private double totalPrice; // quantity * pricePerItem
    private int returnedQuantity = 0; // Number of items already returned from this sale
    private String category;
    private String brand;
    private double costPrice; // Cost of the item at the time of sale

    // Required empty public constructor for Firestore deserialization
    public SaleItem() {
    }

    public SaleItem(String productId, String productName, int quantity, double pricePerItem, double costPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.costPrice = costPrice;
        this.totalPrice = quantity * pricePerItem;
    }

    // Getters
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

    public double getCostPrice() {
        return costPrice;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Recalculate totalPrice if quantity or pricePerItem changes
        this.totalPrice = quantity * this.pricePerItem;
    }

    public void setPricePerItem(double pricePerItem) {
        this.pricePerItem = pricePerItem;
        // Recalculate totalPrice if quantity or pricePerItem changes
        this.totalPrice = this.quantity * pricePerItem;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public int getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(int returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("quantity", quantity);
        map.put("pricePerItem", pricePerItem);
        map.put("costPrice", costPrice);
        map.put("totalPrice", totalPrice);
        map.put("returnedQuantity", returnedQuantity);
        return map;
    }
}
