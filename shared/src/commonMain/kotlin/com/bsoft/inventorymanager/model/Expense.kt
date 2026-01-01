package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    var documentId: String = "",
    var description: String = "",
    var amount: Double = 0.0,
    var date: Long = 0, // Timestamp in millis
    var category: String? = null,
    var userId: String = ""
)
