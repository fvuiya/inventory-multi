package com.bsoft.inventorymanager.network;

import com.bsoft.inventorymanager.models.ProductResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProductApiService {
    @GET("api/v2/product/{barcode}")
    Call<ProductResponse> getProductByBarcode(@Path("barcode") String barcode);
}
