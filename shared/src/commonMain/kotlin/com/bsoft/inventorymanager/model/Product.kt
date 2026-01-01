package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(
    var documentId: String = "",
    var name: String = "",
    var imageUrl: String? = null,
    var brand: String? = null,
    var category: String? = null,
    var productCode: String? = null,
    var barcode: String? = null,
    var quantity: Int = 0,
    var minStockLevel: Int = 0,
    var unit: String? = null,
    var costPrice: Double = 0.0,
    var purchasePrice: Double = 0.0,
    var mrp: Double = 0.0,
    var wholesalePrice: Double = 0.0,
    var dealerPrice: Double = 0.0,
    var supplierId: String? = null,
    var supplierName: String? = null,
    var expiryDate: Long? = null,
    var batchNumber: String? = null,
    var quantityToSell: Int = 0, // Scope: Local transaction
    var sellingPrice: Double = 0.0 // Scope: Local transaction
) {
    companion object {
        const val FIELD_DOCUMENT_ID = "documentId"
    }
}
