package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.repositories.ProductRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SelectProductViewModel extends ViewModel {
    private final ProductRepository productRepository;

    private final MutableLiveData<List<Product>> productsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> brandsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> categoriesList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);

    private DocumentSnapshot lastVisible;
    private String currentBrand = "All Brands";
    private String currentCategory = "All Categories";
    private String currentSearchQuery = "";

    @Inject
    public SelectProductViewModel(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public LiveData<List<Product>> getProductsList() {
        return productsList;
    }

    public LiveData<List<String>> getBrandsList() {
        return brandsList;
    }

    public LiveData<List<String>> getCategoriesList() {
        return categoriesList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsLastPage() {
        return isLastPage;
    }

    public void initMetadata() {
        productRepository.fetchUniqueBrandsAndCategories(new ProductRepository.UniqueFieldsCallback() {
            @Override
            public void onSuccess(List<String> brands, List<String> categories) {
                List<String> b = new ArrayList<>();
                b.add("All Brands");
                b.addAll(brands);
                brandsList.setValue(b);

                List<String> c = new ArrayList<>();
                c.add("All Categories");
                c.addAll(categories);
                categoriesList.setValue(c);
            }

            @Override
            public void onError(Exception e) {
                // handle error
            }
        });
    }

    public void setFilters(String brand, String category, String searchQuery) {
        boolean changed = !currentBrand.equals(brand) || !currentCategory.equals(category)
                || !currentSearchQuery.equals(searchQuery);
        if (changed) {
            currentBrand = brand;
            currentCategory = category;
            currentSearchQuery = searchQuery;
            resetPaginationAndLoad();
        }
    }

    public void resetPaginationAndLoad() {
        lastVisible = null;
        isLastPage.setValue(false);
        productsList.setValue(new ArrayList<>());
        loadNextPage();
    }

    public void loadNextPage() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || Boolean.TRUE.equals(isLastPage.getValue()))
            return;

        isLoading.setValue(true);
        productRepository.fetchPaginatedProducts(lastVisible, currentBrand, currentCategory, currentSearchQuery, 20,
                new ProductRepository.PaginationCallback() {
                    @Override
                    public void onSuccess(List<DocumentSnapshot> documents, boolean hasMore) {
                        if (!documents.isEmpty()) {
                            lastVisible = documents.get(documents.size() - 1);
                            List<Product> newProducts = new ArrayList<>();
                            for (DocumentSnapshot doc : documents) {
                                Product p = doc.toObject(Product.class);
                                if (p != null) {
                                    p.setDocumentId(doc.getId());
                                    // Local filtering for search query if needed (since Firestore doesn't do
                                    // "contains" well)
                                    if (currentSearchQuery.isEmpty() || matchesSearch(p, currentSearchQuery)) {
                                        newProducts.add(p);
                                    }
                                }
                            }
                            List<Product> current = productsList.getValue();
                            if (current == null)
                                current = new ArrayList<>();
                            current.addAll(newProducts);
                            productsList.setValue(current);
                        }
                        isLastPage.setValue(!hasMore);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        isLoading.setValue(false);
                    }
                });
    }

    private boolean matchesSearch(Product p, String query) {
        String q = query.toLowerCase();
        return (p.getName() != null && p.getName().toLowerCase().contains(q)) ||
                (p.getProductCode() != null && p.getProductCode().toLowerCase().contains(q)) ||
                (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(q));
    }
}
