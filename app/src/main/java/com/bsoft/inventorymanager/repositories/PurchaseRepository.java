package com.bsoft.inventorymanager.repositories;

import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Supplier;
import java.util.List;

public interface PurchaseRepository {
    interface PurchaseCallback {
        void onSuccess(String purchaseId);

        void onFailure(Exception e);
    }

    interface ProductCallback {
        void onSuccess(Product product);

        void onFailure(Exception e);
    }

    interface GetPurchaseCallback {
        void onSuccess(Purchase purchase);

        void onFailure(Exception e);
    }

    void getPurchase(String purchaseId, GetPurchaseCallback callback);

    void savePurchase(Purchase purchase, List<PurchaseItem> items, PurchaseCallback callback);

    void updatePurchase(Purchase purchase, List<PurchaseItem> items, PurchaseCallback callback);

    void getProductByBarcode(String barcode, ProductCallback callback);

    void getProduct(String productId, ProductCallback callback);

    interface SuppliersCallback {
        void onSuccess(List<Supplier> suppliers);

        void onFailure(Exception e);
    }

    void getSuppliers(SuppliersCallback callback);
}
