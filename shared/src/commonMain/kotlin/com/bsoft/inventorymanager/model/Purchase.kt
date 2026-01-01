package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    var documentId: String = "",
    var supplierId: String = "",
    var supplierName: String = "",
    var supplierContactNumber: String = "",
    var purchaseDate: Long = 0, // Timestamp millis
    var purchaseOrderNumber: String? = null,
    var expectedDeliveryDate: Long? = null, // Timestamp millis
    var actualDeliveryDate: Long? = null, // Timestamp millis
    var deliveryStatus: String = "",
    var purchaseStatus: String = "",
    var totalAmount: Double = 0.0,
    var amountPaid: Double = 0.0,
    var amountDue: Double = 0.0,
    var taxAmount: Double = 0.0,
    var discountAmount: Double = 0.0,
    var paymentStatus: String = "",
    var paymentMethod: String = "",
    var notes: String? = null,
    var items: List<PurchaseItem> = emptyList(),
    var productIds: List<String> = emptyList(),
    var userId: String = "",
    var status: String = "",
    var deliveryAddress: String? = null,
    var invoiceNumber: String? = null
)
