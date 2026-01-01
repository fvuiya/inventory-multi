package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    var documentId: String = "",
    var customerId: String = "",
    var orderDate: Long = 0, // Date in millis
    var status: String = "",
    var totalAmount: Double = 0.0
)
