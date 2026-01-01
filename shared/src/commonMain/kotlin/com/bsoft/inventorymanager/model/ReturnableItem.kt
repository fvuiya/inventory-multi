package com.bsoft.inventorymanager.model

interface ReturnableItem {
    val productId: String
    val productName: String
    val quantity: Int
    val pricePerItem: Double
}
