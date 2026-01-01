package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleItem(
    override var productId: String = "",
    override var productName: String = "",
    override var quantity: Int = 0,
    override var pricePerItem: Double = 0.0,
    var totalPrice: Double = 0.0,
    var returnedQuantity: Int = 0,
    var category: String? = null,
    var brand: String? = null,
    var costPrice: Double = 0.0
) : ReturnableItem {
    fun updateTotalPrice() {
        totalPrice = quantity * pricePerItem
    }
}
