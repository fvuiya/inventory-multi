package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItem(
    override var productId: String = "",
    override var productName: String = "",
    var productCode: String? = null,
    override var quantity: Int = 0,
    override var pricePerItem: Double = 0.0,
    var totalPrice: Double = 0.0,
    var returnedQuantity: Int = 0,
    var taxRate: Double = 0.0,
    var discountRate: Double = 0.0,
    var batchNumber: String? = null,
    var expiryDate: Long? = null, // Timestamp in millis
    var notes: String? = null
) : ReturnableItem {
    // Custom logic from Java class
    fun updateTotalPrice() {
        totalPrice = quantity * pricePerItem
    }
}
