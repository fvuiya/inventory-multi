package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleReturn(
    var documentId: String = "",
    var originalSaleId: String = "",
    var customerId: String = "",
    var customerName: String = "",
    var returnDate: Long = 0, // Timestamp in millis
    var items: List<SaleReturnItem> = emptyList(),
    var totalRefundAmount: Double = 0.0,
    var userId: String = ""
)
