package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Purchase
import com.bsoft.inventorymanager.model.PurchaseItem
import com.bsoft.inventorymanager.model.Product
import com.bsoft.inventorymanager.model.Supplier

interface PurchaseRepository {
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
    suspend fun savePurchase(purchase: Purchase, items: List<PurchaseItem>): Result<String> // Returns ID
    suspend fun updatePurchase(purchase: Purchase, items: List<PurchaseItem>): Result<Unit>
    
    suspend fun getProductByBarcode(barcode: String): Result<Product?>
    suspend fun getProduct(productId: String): Result<Product>
    
    suspend fun getSuppliers(): Result<List<Supplier>>
}
