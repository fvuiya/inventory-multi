package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Product

interface ProductRepository {
    /**
     * Fetch paginated products with optional filters.
     * @param lastDocumentId ID of the last document for cursor-based pagination, null for first page
     * @param brand Optional brand filter
     * @param category Optional category filter
     * @param searchQuery Optional search query
     * @param pageSize Number of products per page
     * @return Result with list of products and whether more pages exist
     */
    suspend fun fetchPaginatedProducts(
        lastDocumentId: String?,
        brand: String?,
        category: String?,
        searchQuery: String?,
        pageSize: Int
    ): Result<ProductPage>
    
    suspend fun fetchUniqueBrandsAndCategories(): Result<BrandsAndCategories>
    
    suspend fun saveProduct(product: Product): Result<Unit>
    
    suspend fun deleteProduct(product: Product): Result<Unit>
    
    suspend fun getProduct(productId: String): Result<Product>
}

data class ProductPage(
    val products: List<Product>,
    val hasMore: Boolean,
    val lastDocumentId: String?
)

data class BrandsAndCategories(
    val brands: List<String>,
    val categories: List<String>
)
