package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(private val db: FirebaseFirestore) : ProductRepository {

    // Cache for cursor-based pagination
    private var lastVisibleSnapshot: DocumentSnapshot? = null

    override suspend fun fetchPaginatedProducts(
        lastDocumentId: String?,
        brand: String?,
        category: String?,
        searchQuery: String?,
        pageSize: Int
    ): Result<ProductPage> {
        return try {
            var query: Query = db.collection("products")
                .orderBy("name")
                .limit(pageSize.toLong() + 1) // Fetch one extra to check hasMore

            // Apply filters
            if (!brand.isNullOrEmpty()) {
                query = query.whereEqualTo("brand", brand)
            }
            if (!category.isNullOrEmpty()) {
                query = query.whereEqualTo("category", category)
            }

            // Handle cursor-based pagination
            if (lastDocumentId != null && lastVisibleSnapshot != null && 
                lastVisibleSnapshot?.id == lastDocumentId) {
                query = query.startAfter(lastVisibleSnapshot!!)
            } else if (lastDocumentId != null) {
                // Fetch the document to use as cursor
                val cursorDoc = db.collection("products").document(lastDocumentId).get().await()
                if (cursorDoc.exists()) {
                    query = query.startAfter(cursorDoc)
                }
            }

            val querySnapshot = query.get().await()
            val documents = querySnapshot.documents

            // Check if there are more pages
            val hasMore = documents.size > pageSize
            val productsToReturn = if (hasMore) documents.dropLast(1) else documents

            // Cache the last visible for next page
            if (productsToReturn.isNotEmpty()) {
                lastVisibleSnapshot = productsToReturn.last()
            }

            val products = productsToReturn.mapNotNull { doc ->
                doc.data?.let { mapToProduct(it, doc.id) }
            }.filter { product ->
                // Client-side search filter if searchQuery provided
                if (searchQuery.isNullOrEmpty()) true
                else product.name.contains(searchQuery, ignoreCase = true) ||
                     (product.productCode?.contains(searchQuery, ignoreCase = true) == true)
            }

            Result.success(ProductPage(
                products = products,
                hasMore = hasMore,
                lastDocumentId = lastVisibleSnapshot?.id
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUniqueBrandsAndCategories(): Result<BrandsAndCategories> {
        return try {
            val snapshot = db.collection("products").get().await()
            
            val brands = snapshot.documents
                .mapNotNull { it.getString("brand") }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
            
            val categories = snapshot.documents
                .mapNotNull { it.getString("category") }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()

            Result.success(BrandsAndCategories(brands, categories))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProduct(product: Product): Result<Unit> {
        return try {
            val productRef = if (product.documentId.isNotEmpty()) {
                db.collection("products").document(product.documentId)
            } else {
                db.collection("products").document()
            }
            
            val productMap = mapFromProduct(product.copy(documentId = productRef.id))
            productRef.set(productMap).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            if (product.documentId.isEmpty()) {
                return Result.failure(Exception("Product ID is required for deletion"))
            }
            db.collection("products").document(product.documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProduct(productId: String): Result<Product> {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            if (snapshot.exists()) {
                val product = mapToProduct(snapshot.data!!, snapshot.id)
                Result.success(product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reset pagination cache
    fun resetPagination() {
        lastVisibleSnapshot = null
    }

    // --- Mappers ---

    private fun mapToProduct(data: Map<String, Any>, id: String): Product {
        return Product(
            documentId = id,
            name = data["name"] as? String ?: "",
            imageUrl = data["imageUrl"] as? String,
            brand = data["brand"] as? String,
            category = data["category"] as? String,
            productCode = data["productCode"] as? String,
            barcode = data["barcode"] as? String,
            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
            minStockLevel = (data["minStockLevel"] as? Number)?.toInt() ?: 0,
            unit = data["unit"] as? String,
            costPrice = (data["costPrice"] as? Number)?.toDouble() ?: 0.0,
            purchasePrice = (data["purchasePrice"] as? Number)?.toDouble() ?: 0.0,
            mrp = (data["mrp"] as? Number)?.toDouble() ?: 0.0,
            wholesalePrice = (data["wholesalePrice"] as? Number)?.toDouble() ?: 0.0,
            dealerPrice = (data["dealerPrice"] as? Number)?.toDouble() ?: 0.0,
            supplierId = data["supplierId"] as? String,
            supplierName = data["supplierName"] as? String
        )
    }

    private fun mapFromProduct(product: Product): Map<String, Any?> {
        return mapOf(
            "documentId" to product.documentId,
            "name" to product.name,
            "imageUrl" to product.imageUrl,
            "brand" to product.brand,
            "category" to product.category,
            "productCode" to product.productCode,
            "barcode" to product.barcode,
            "quantity" to product.quantity,
            "minStockLevel" to product.minStockLevel,
            "unit" to product.unit,
            "costPrice" to product.costPrice,
            "purchasePrice" to product.purchasePrice,
            "mrp" to product.mrp,
            "wholesalePrice" to product.wholesalePrice,
            "dealerPrice" to product.dealerPrice,
            "supplierId" to product.supplierId,
            "supplierName" to product.supplierName
        )
    }
}
