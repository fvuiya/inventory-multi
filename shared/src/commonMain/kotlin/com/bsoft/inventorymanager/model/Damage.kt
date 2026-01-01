package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class Damage(
    var documentId: String = "",
    var productId: String = "",
    var productName: String = "",
    var quantity: Int = 0,
    var reason: String? = null,
    var userId: String = "",
    var date: Long = 0 // Timestamp in millis
)
