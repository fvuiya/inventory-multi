package com.bsoft.inventorymanager.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
// [KMP MIGRATION] Use shared models
import com.bsoft.inventorymanager.model.Product
import com.bsoft.inventorymanager.model.Sale
import com.bsoft.inventorymanager.models.Expense
import com.bsoft.inventorymanager.models.Purchase
import com.bsoft.inventorymanager.repositories.MainRepository
// [KMP MIGRATION] Use shared ProductRepository
import com.bsoft.inventorymanager.repository.ProductRepository
import com.bsoft.inventorymanager.repository.ProductPage
import com.bsoft.inventorymanager.repository.BrandsAndCategories
import com.bsoft.inventorymanager.utils.ModelMappers.toShared
import com.bsoft.inventorymanager.utils.ModelMappers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val expenseRepository: com.bsoft.inventorymanager.repositories.ExpenseRepository,
    // [KMP MIGRATION] Shared ProductRepository
    private val productRepository: ProductRepository
) : ViewModel() {

    // --- Products (Migrated to KMP Shared) ---
    private val _products = MutableLiveData<List<Product>>(ArrayList())
    val products: LiveData<List<Product>> = _products

    private var lastProductDocumentId: String? = null
    private var isLastPageProducts = false
    private var loadingProducts = false

    // --- Sales (Migrated to KMP Shared via mapping) ---
    val sales: LiveData<List<Sale>> = mainRepository.sales.map { legacySales ->
        legacySales.map { it.toShared() }
    }

    // --- Purchases (Migrated to KMP Shared via mapping) ---
    val purchases: LiveData<List<com.bsoft.inventorymanager.model.Purchase>> = mainRepository.purchases.map { legacyPurchases ->
        legacyPurchases.map { with(ModelMappers) { it.toShared() } }
    }

    // --- Expenses (Legacy) ---
    fun getExpenses(): LiveData<List<Expense>> = expenseRepository.expenses

    // --- Loading State ---
    private val _isLoading = MutableLiveData(false)
    
    @get:JvmName("getIsLoading")
    val isLoading: LiveData<Boolean> = _isLoading

    // --- Products Functions ---

    fun loadNextPageProducts() {
        if (loadingProducts || isLastPageProducts) return

        loadingProducts = true
        updateLoadingState()

        viewModelScope.launch {
            productRepository.fetchPaginatedProducts(
                lastDocumentId = lastProductDocumentId,
                brand = null,
                category = null,
                searchQuery = null,
                pageSize = 20
            ).onSuccess { page ->
                val currentList = _products.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(page.products)
                _products.value = currentList
                lastProductDocumentId = page.lastDocumentId
                isLastPageProducts = !page.hasMore
                loadingProducts = false
                updateLoadingState()
            }.onFailure {
                loadingProducts = false
                updateLoadingState()
            }
        }
    }

    fun saveProduct(product: Product, callback: ProductCallback) {
        viewModelScope.launch {
            productRepository.saveProduct(product)
                .onSuccess { callback.onSuccess() }
                .onFailure { callback.onError(it as Exception) }
        }
    }

    fun deleteProduct(product: Product, callback: ProductCallback) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
                .onSuccess { callback.onSuccess() }
                .onFailure { callback.onError(it as Exception) }
        }
    }

    fun fetchUniqueBrandsAndCategories(callback: UniqueFieldsCallback) {
        viewModelScope.launch {
            productRepository.fetchUniqueBrandsAndCategories()
                .onSuccess { callback.onSuccess(it.brands, it.categories) }
                .onFailure { callback.onError(it as Exception) }
        }
    }

    interface ProductCallback {
        fun onSuccess()
        fun onError(e: Exception)
    }

    interface UniqueFieldsCallback {
        fun onSuccess(brands: List<String>, categories: List<String>)
        fun onError(e: Exception)
    }

    // --- Sales & Purchases (Delegate to MainRepository) ---
    fun loadNextPageSales() {
        mainRepository.loadNextPageSales()
    }

    fun loadNextPagePurchases() {
        mainRepository.loadNextPagePurchases()
    }

    // --- Expenses (Delegate to ExpenseRepository) ---
    fun loadNextPageExpenses() {
        expenseRepository.loadNextPageExpenses()
    }

    // --- Preload & Refresh ---
    fun preloadData() {
        if (_products.value.isNullOrEmpty()) {
            loadNextPageProducts()
        }
        mainRepository.preloadMainData()
        expenseRepository.preloadExpenses()
    }

    fun refreshData() {
        // Reset products
        _products.value = ArrayList()
        lastProductDocumentId = null
        isLastPageProducts = false

        mainRepository.resetPagination()
        expenseRepository.resetPagination()

        loadNextPageProducts()
        mainRepository.preloadMainData()
        expenseRepository.preloadExpenses()
    }

    // --- Expenses (Delegate) ---
    fun saveExpense(expense: Expense, callback: com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback) {
        expenseRepository.saveExpense(expense, callback)
    }

    fun deleteExpense(expenseId: String, callback: com.bsoft.inventorymanager.repositories.ExpenseRepository.ExpenseCallback) {
        expenseRepository.deleteExpense(expenseId, callback)
    }

    // --- Loading State Helper ---
    private fun updateLoadingState() {
        _isLoading.value = loadingProducts
    }
}
