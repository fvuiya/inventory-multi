package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Sale(
    var documentId: String = "",
    var customerId: String = "",
    var customerName: String = "",
    var customerPhoneNumber: String = "",
    var salesPerson: String? = null,
    var commissionRate: Double = 0.0,
    var saleDate: Long = 0, // Timestamp in millis
    var invoiceNumber: String? = null,
    var totalAmount: Double = 0.0,
    var amountPaid: Double = 0.0,
    var amountDue: Double = 0.0,
    var taxAmount: Double = 0.0,
    var discountAmount: Double = 0.0,
    var paymentMethod: String? = null,
    var deliveryStatus: String? = null,
    var deliveryAddress: String? = null,
    var deliveryCharge: Double = 0.0,
    var loyaltyPointsEarned: String? = null,
    var salesChannel: String? = null,
    var notes: String? = null,
    var items: List<SaleItem> = emptyList(),
    var productIds: List<String> = emptyList(),
    var userId: String = "",
    var status: String = "",
    var subtotal: Double = 0.0,
    var totalCost: Double? = null,
    var totalProfit: Double? = null
)
