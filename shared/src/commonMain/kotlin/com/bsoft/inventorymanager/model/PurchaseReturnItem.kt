package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseReturnItem(
    var productId: String = "",
    var productName: String = "",
    var quantity: Int = 0,
    var pricePerItem: Double = 0.0
)
