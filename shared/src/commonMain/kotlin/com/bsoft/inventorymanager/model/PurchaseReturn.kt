package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseReturn(
    var documentId: String = "",
    var originalPurchaseId: String = "",
    var supplierId: String = "",
    var supplierName: String = "",
    var returnDate: Long = 0, // Timestamp in millis
    var items: List<PurchaseReturnItem> = emptyList(),
    var totalCreditAmount: Double = 0.0,
    var userId: String = ""
)
