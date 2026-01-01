package com.bsoft.inventorymanager.repositories;

import androidx.lifecycle.LiveData;
import com.bsoft.inventorymanager.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public interface ProductRepository {
    interface PaginationCallback {
        void onSuccess(List<DocumentSnapshot> documents, boolean hasMore);

        void onError(Exception e);
    }

    void fetchPaginatedProducts(
            DocumentSnapshot lastVisible,
            String brand,
            String category,
            String searchQuery,
            int pageSize,
            PaginationCallback callback);

    void fetchUniqueBrandsAndCategories(UniqueFieldsCallback callback);

    interface UniqueFieldsCallback {
        void onSuccess(List<String> brands, List<String> categories);

        void onError(Exception e);
    }

    void saveProduct(Product product, ProductCallback callback);

    void deleteProduct(Product product, ProductCallback callback);

    interface ProductCallback {
        void onSuccess();

        void onError(Exception e);
    }
}
