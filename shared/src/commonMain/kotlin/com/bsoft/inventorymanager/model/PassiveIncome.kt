package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class PassiveIncome(
    var documentId: String = "",
    var source: String = "",
    var note: String? = null,
    var amount: Double = 0.0,
    var date: Long = 0 // Timestamp in millis
)
