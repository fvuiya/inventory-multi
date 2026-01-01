package com.bsoft.inventorymanager.repositories;

import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.utils.PaginationHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProductRepositoryImpl implements ProductRepository {

    private final FirebaseFirestore db;

    @Inject
    public ProductRepositoryImpl(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void fetchPaginatedProducts(
            DocumentSnapshot lastVisible,
            String brand,
            String category,
            String searchQuery,
            int pageSize,
            PaginationCallback callback) {

        Query query = db.collection("products")
                .orderBy("name", Query.Direction.ASCENDING);

        if (brand != null && !brand.equals("All Brands")) {
            query = query.whereEqualTo("brand", brand);
        }
        if (category != null && !category.equals("All Categories")) {
            query = query.whereEqualTo("category", category);
        }

        // Note: Firestore doesn't support easy case-insensitive contains search.
        // For localized search query, we might still need some client-side or separate
        // logic.
        // For now, we apply brand/category filters at server side if possible.
        // If searchQuery is present, we might have to fetch more and filter locally or
        // use a trick.

        PaginationHelper.fetchPaginatedData(query, lastVisible, pageSize, new PaginationHelper.PaginationCallback() {
            @Override
            public void onSuccess(List<DocumentSnapshot> documents, boolean hasMore) {
                callback.onSuccess(documents, hasMore);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    @Override
    public void fetchUniqueBrandsAndCategories(UniqueFieldsCallback callback) {
        // Try to fetch from metadata document first
        db.collection("metadata").document("products").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> brands = (List<String>) documentSnapshot.get("brands");
                        List<String> categories = (List<String>) documentSnapshot.get("categories");

                        if (brands == null)
                            brands = new ArrayList<>();
                        if (categories == null)
                            categories = new ArrayList<>();

                        Collections.sort(brands);
                        Collections.sort(categories);
                        callback.onSuccess(brands, categories);
                    } else {
                        // Fallback to full fetch if metadata doesn't exist
                        fetchFromAllProducts(callback);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback on error
                    fetchFromAllProducts(callback);
                });
    }

    private void fetchFromAllProducts(UniqueFieldsCallback callback) {
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> brands = new HashSet<>();
            Set<String> categories = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String b = doc.getString("brand");
                String c = doc.getString("category");
                if (b != null && !b.isEmpty())
                    brands.add(b);
                if (c != null && !c.isEmpty())
                    categories.add(c);
            }
            List<String> sortedBrands = new ArrayList<>(brands);
            List<String> sortedCategories = new ArrayList<>(categories);
            Collections.sort(sortedBrands);
            Collections.sort(sortedCategories);
            callback.onSuccess(sortedBrands, sortedCategories);
        }).addOnFailureListener(callback::onError);
    }

    @Override
    public void saveProduct(Product product, ProductCallback callback) {
        if (product.getDocumentId() == null || product.getDocumentId().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID cannot be null"));
            return;
        }

        // Save product and update metadata atomically if possible,
        // or just sequentially for simplicity in this context.
        // Using a transaction would be best, but for now we'll do independent writes
        // to avoid complex transaction logic if metadata doc doesn't exist.

        db.collection("products").document(product.getDocumentId()).set(product)
                .addOnSuccessListener(aVoid -> {
                    // Update metadata
                    updateMetadata(product.getBrand(), product.getCategory());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onError);
    }

    private void updateMetadata(String brand, String category) {
        // We use arrayUnion to add only unique values.
        // We do not remove values on delete (soft management of metadata).
        if ((brand == null || brand.isEmpty()) && (category == null || category.isEmpty()))
            return;

        com.google.firebase.firestore.DocumentReference metaDoc = db.collection("metadata").document("products");

        // Use SetOptions.merge() in case doc doesn't exist?
        // ArrayUnion works with set(..., SetOptions.merge()) or update() if exists.
        // Safe way: set with merge.

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        if (brand != null && !brand.isEmpty()) {
            updates.put("brands", com.google.firebase.firestore.FieldValue.arrayUnion(brand));
        }
        if (category != null && !category.isEmpty()) {
            updates.put("categories", com.google.firebase.firestore.FieldValue.arrayUnion(category));
        }

        if (!updates.isEmpty()) {
            metaDoc.set(updates, com.google.firebase.firestore.SetOptions.merge());
        }
    }

    @Override
    public void deleteProduct(Product product, ProductCallback callback) {
        if (product.getDocumentId() == null || product.getDocumentId().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID cannot be null"));
            return;
        }
        db.collection("products").document(product.getDocumentId()).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
