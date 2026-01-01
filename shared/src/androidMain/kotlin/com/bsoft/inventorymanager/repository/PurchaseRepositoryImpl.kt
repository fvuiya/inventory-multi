package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Product
import com.bsoft.inventorymanager.model.Purchase
import com.bsoft.inventorymanager.model.PurchaseItem
import com.bsoft.inventorymanager.model.Supplier
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.tasks.await
import java.util.Date

class PurchaseRepositoryImpl(private val db: FirebaseFirestore) : PurchaseRepository {

    override suspend fun getPurchase(purchaseId: String): Result<Purchase> {
        return try {
            val snapshot = db.collection("purchases").document(purchaseId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return Result.failure(Exception("No data"))
                val purchase = mapToPurchase(data, snapshot.id)
                Result.success(purchase)
            } else {
                Result.failure(Exception("Purchase not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePurchase(purchase: Purchase, items: List<PurchaseItem>): Result<String> {
        return try {
            val batch = db.batch()
            val purchaseRef = if (purchase.documentId.isNotEmpty()) {
                db.collection("purchases").document(purchase.documentId)
            } else {
                db.collection("purchases").document()
            }
            
            val finalPurchase = purchase.copy(documentId = purchaseRef.id, items = items)
            val purchaseMap = mapFromPurchase(finalPurchase)
            
            batch.set(purchaseRef, purchaseMap)

            // Update stock
            for (item in items) {
                if (item.productId.isNotEmpty()) {
                    val productRef = db.collection("products").document(item.productId)
                    batch.update(productRef, "quantity", FieldValue.increment(item.quantity.toLong()))
                }
            }
            
            batch.commit().await()
            Result.success(purchaseRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePurchase(purchase: Purchase, items: List<PurchaseItem>): Result<Unit> {
        return try {
            val batch = db.batch()
            val purchaseRef = db.collection("purchases").document(purchase.documentId)
            val purchaseMap = mapFromPurchase(purchase.copy(items = items))
            
            batch.set(purchaseRef, purchaseMap)
            // Stock logic omitted for simplicity as per original
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Result<Product?> {
        return try {
            val query = db.collection("products")
                .whereEqualTo("productCode", barcode)
                .limit(1)
                .get()
                .await()
            
            if (!query.isEmpty) {
                val doc = query.documents[0]
                val product = mapToProduct(doc.data!!, doc.id)
                Result.success(product)
            } else {
                Result.success(null)
            }
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

    override suspend fun getSuppliers(): Result<List<Supplier>> {
        return try {
            val query = db.collection("suppliers")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val suppliers = query.map { doc ->
                mapToSupplier(doc.data, doc.id)
            }
            Result.success(suppliers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Mappers ---

    private fun mapToPurchase(data: Map<String, Any>, id: String): Purchase {
        return Purchase(
            documentId = id,
            supplierId = data["supplierId"] as? String ?: "",
            supplierName = data["supplierName"] as? String ?: "",
            purchaseDate = (data["purchaseDate"] as? Timestamp)?.toDate()?.time ?: 0L,
            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
            items = (data["items"] as? List<Map<String, Any>>)?.map { mapToPurchaseItem(it) } ?: emptyList()
            // Add other fields mapping
        )
    }
    
    private fun mapFromPurchase(purchase: Purchase): Map<String, Any?> {
        return mapOf(
            "documentId" to purchase.documentId,
            "supplierId" to purchase.supplierId,
            "supplierName" to purchase.supplierName,
            "purchaseDate" to Timestamp(Date(purchase.purchaseDate)),
            "totalAmount" to purchase.totalAmount,
            "items" to purchase.items.map { mapFromPurchaseItem(it) }
            // Add other fields
        )
    }

    private fun mapToPurchaseItem(data: Map<String, Any>): PurchaseItem {
        return PurchaseItem(
            productId = data["productId"] as? String ?: "",
            productName = data["productName"] as? String ?: "",
            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
            pricePerItem = (data["pricePerItem"] as? Number)?.toDouble() ?: 0.0,
            expiryDate = (data["expiryDate"] as? Timestamp)?.toDate()?.time
            // Other fields
        )
    }

    private fun mapFromPurchaseItem(item: PurchaseItem): Map<String, Any?> {
        return mapOf(
            "productId" to item.productId,
            "productName" to item.productName,
            "quantity" to item.quantity,
            "pricePerItem" to item.pricePerItem,
            "expiryDate" to item.expiryDate?.let { Timestamp(Date(it)) }
        )
    }
    
    private fun mapToProduct(data: Map<String, Any>, id: String): Product {
        return Product(
            documentId = id,
            name = data["name"] as? String ?: "",
            barcode = data["barcode"] as? String ?: "",
            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
            productCode = data["productCode"] as? String ?: ""
            // Other fields
        )
    }

    private fun mapToSupplier(data: Map<String, Any>, id: String): Supplier {
        return Supplier(
            documentId = id,
            name = data["name"] as? String ?: ""
            // Other fields
        )
    }
}
