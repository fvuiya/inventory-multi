package com.bsoft.inventorymanager.models;

import com.google.gson.annotations.SerializedName;

public class ApiProduct {

    @SerializedName("product_name")
    private String productName;

    @SerializedName("brands")
    private String brands;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("categories")
    private String categories;

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getBrands() {
        return brands;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategories() {
        return categories;
    }
}
