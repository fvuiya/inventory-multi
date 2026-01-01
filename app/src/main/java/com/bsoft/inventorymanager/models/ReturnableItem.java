package com.bsoft.inventorymanager.models;

public interface ReturnableItem {
    String getProductId();
    String getProductName();
    int getQuantity();
    double getPricePerItem();
}
