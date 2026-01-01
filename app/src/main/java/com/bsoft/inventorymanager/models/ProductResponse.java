package com.bsoft.inventorymanager.models;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {

    @SerializedName("product")
    private ApiProduct product;

    @SerializedName("status")
    private int status;

    public ApiProduct getProduct() {
        return product;
    }

    public int getStatus() {
        return status;
    }
}
