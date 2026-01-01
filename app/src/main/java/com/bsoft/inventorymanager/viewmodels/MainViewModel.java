package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Expense;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.repositories.MainRepository;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final MainRepository mainRepository;
    private final com.bsoft.inventorymanager.repositories.ExpenseRepository expenseRepository;
    private final com.bsoft.inventorymanager.repositories.ProductRepository productRepository;

    @Inject
    public MainViewModel(MainRepository mainRepository,
            com.bsoft.inventorymanager.repositories.ExpenseRepository expenseRepository,
            com.bsoft.inventorymanager.repositories.ProductRepository productRepository) {
        this.mainRepository = mainRepository;
        this.expenseRepository = expenseRepository;
        this.productRepository = productRepository;
    }

    public LiveData<List<Product>> getProducts() {
        return mainRepository.getProducts();
    }

    public LiveData<List<Sale>> getSales() {
        return mainRepository.getSales();
    }

    public LiveData<List<Purchase>> getPurchases() {
        return mainRepository.getPurchases();
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenseRepository.getExpenses();
    }

    public LiveData<Boolean> getIsLoading() {
        // Return a combined loading state if necessary, or just mainRepository's
        return mainRepository.getIsLoading();
    }

    public void preloadData() {
        mainRepository.preloadMainData();
        expenseRepository.preloadExpenses();
    }

    public void loadNextPageProducts() {
        mainRepository.loadNextPageProducts();
    }

    public void loadNextPageSales() {
        mainRepository.loadNextPageSales();
    }

    public void loadNextPagePurchases() {
        mainRepository.loadNextPagePurchases();
    }

    public void loadNextPageExpenses() {
        expenseRepository.loadNextPageExpenses();
    }

    public void refreshData() {
        mainRepository.resetPagination();
        expenseRepository.resetPagination();
        mainRepository.preloadMainData();
        expenseRepository.preloadExpenses();
    }

    public void saveExpense(Expense expense,
            com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback callback) {
        expenseRepository.saveExpense(expense, callback);
    }

    public void deleteExpense(String expenseId,
            com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback callback) {
        expenseRepository.deleteExpense(expenseId, callback);
    }

    public void saveProduct(Product product,
            com.bsoft.inventorymanager.repositories.ProductRepository.ProductCallback callback) {
        productRepository.saveProduct(product, callback);
    }

    public void deleteProduct(Product product,
            com.bsoft.inventorymanager.repositories.ProductRepository.ProductCallback callback) {
        productRepository.deleteProduct(product, callback);
    }

    public void fetchUniqueBrandsAndCategories(
            com.bsoft.inventorymanager.repositories.ProductRepository.UniqueFieldsCallback callback) {
        productRepository.fetchUniqueBrandsAndCategories(callback);
    }
}
