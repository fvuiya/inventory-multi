package com.bsoft.inventorymanager.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.Sale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.bsoft.inventorymanager.utils.PaginationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * MainRepository handles Sales and Purchases pagination.
 * Products are now managed by the shared ProductRepository (KMP).
 */
@Singleton
public class MainRepository {
    private final FirebaseFirestore db;

    // [KMP MIGRATION] Products removed - now handled by shared ProductRepository
    private final MutableLiveData<List<Sale>> sales = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Purchase>> purchases = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public MainRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public LiveData<List<Sale>> getSales() {
        return sales;
    }

    public LiveData<List<Purchase>> getPurchases() {
        return purchases;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Pagination State
    private DocumentSnapshot lastVisibleSale;
    private boolean isLastPageSales = false;

    private DocumentSnapshot lastVisiblePurchase;
    private boolean isLastPagePurchases = false;

    // Internal loading states to allow concurrent fetching
    private boolean loadingSales = false;
    private boolean loadingPurchases = false;

    private void updateGlobalLoadingState() {
        isLoading.postValue(loadingSales || loadingPurchases);
    }

    public void loadNextPageSales() {
        if (loadingSales || isLastPageSales)
            return;

        loadingSales = true;
        updateGlobalLoadingState();

        PaginationHelper.fetchPaginatedData("sales", lastVisibleSale, 20, "saleDate",
                com.google.firebase.firestore.Query.Direction.DESCENDING, new PaginationHelper.PaginationCallback() {
                    @Override
                    public void onSuccess(List<DocumentSnapshot> documents, boolean hasMore) {
                        if (!documents.isEmpty()) {
                            lastVisibleSale = documents.get(documents.size() - 1);
                            List<Sale> newItems = new ArrayList<>();
                            for (DocumentSnapshot doc : documents) {
                                Sale item = doc.toObject(Sale.class);
                                if (item != null) {
                                    item.setDocumentId(doc.getId());
                                    newItems.add(item);
                                }
                            }
                            appendToList(sales, newItems);
                        }
                        isLastPageSales = !hasMore;
                        loadingSales = false;
                        updateGlobalLoadingState();
                    }

                    @Override
                    public void onError(Exception e) {
                        loadingSales = false;
                        updateGlobalLoadingState();
                    }
                });
    }

    public void loadNextPagePurchases() {
        if (loadingPurchases || isLastPagePurchases)
            return;

        loadingPurchases = true;
        updateGlobalLoadingState();

        PaginationHelper.fetchPaginatedData("purchases", lastVisiblePurchase, 20, "purchaseDate",
                com.google.firebase.firestore.Query.Direction.DESCENDING, new PaginationHelper.PaginationCallback() {
                    @Override
                    public void onSuccess(List<DocumentSnapshot> documents, boolean hasMore) {
                        if (!documents.isEmpty()) {
                            lastVisiblePurchase = documents.get(documents.size() - 1);
                            List<Purchase> newItems = new ArrayList<>();
                            for (DocumentSnapshot doc : documents) {
                                Purchase item = doc.toObject(Purchase.class);
                                if (item != null) {
                                    item.setDocumentId(doc.getId());
                                    newItems.add(item);
                                }
                            }
                            appendToList(purchases, newItems);
                        }
                        isLastPagePurchases = !hasMore;
                        loadingPurchases = false;
                        updateGlobalLoadingState();
                    }

                    @Override
                    public void onError(Exception e) {
                        loadingPurchases = false;
                        updateGlobalLoadingState();
                    }
                });
    }

    /**
     * Preloads Sales and Purchases data.
     * Products are loaded by MainViewModel via shared ProductRepository.
     */
    public void preloadMainData() {
        // Only preload Sales and Purchases
        // Products are loaded by MainViewModel.loadNextPageProducts()
        if (sales.getValue() == null || sales.getValue().isEmpty()) {
            loadNextPageSales();
        }
        if (purchases.getValue() == null || purchases.getValue().isEmpty()) {
            loadNextPagePurchases();
        }
    }

    public void resetPagination() {
        // Clear Lists
        sales.setValue(new ArrayList<>());
        purchases.setValue(new ArrayList<>());

        // Reset state
        lastVisibleSale = null;
        isLastPageSales = false;
        lastVisiblePurchase = null;
        isLastPagePurchases = false;
    }

    private <T> void appendToList(MutableLiveData<List<T>> liveData, List<T> newItems) {
        List<T> currentList = liveData.getValue();
        if (currentList == null)
            currentList = new ArrayList<>();
        currentList.addAll(newItems);
        liveData.setValue(new ArrayList<>(currentList));
    }
}
