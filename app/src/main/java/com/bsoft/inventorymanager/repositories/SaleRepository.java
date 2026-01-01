package com.bsoft.inventorymanager.repositories;

import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.models.SaleItem;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Customer;
import java.util.List;

public interface SaleRepository {
    interface SaleCallback {
        void onSuccess(String saleId);

        void onFailure(Exception e);
    }

    interface ProductCallback {
        void onSuccess(Product product);

        void onFailure(Exception e);
    }

    interface GetSaleCallback {
        void onSuccess(Sale sale);

        void onFailure(Exception e);
    }

    void getSale(String saleId, GetSaleCallback callback);

    void saveSale(Sale sale, List<SaleItem> items, SaleCallback callback);

    void updateSale(Sale sale, List<SaleItem> items, SaleCallback callback);

    void getProductByBarcode(String barcode, ProductCallback callback);

    void getProduct(String productId, ProductCallback callback);

    interface CustomersCallback {
        void onSuccess(List<Customer> customers);

        void onFailure(Exception e);
    }

    void getCustomers(CustomersCallback callback);
}
