package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Customer(
    override var documentId: String = "",
    override var name: String = "",
    override var contactNumber: String = "",
    override var address: String = "",
    override var age: Int = 0,
    override var photo: String? = null,
    @SerialName("isActive")
    override var isActive: Boolean = true,
    var creationDate: Long = 0,
    var creditLimit: Double = 0.0,
    var outstandingBalance: Double = 0.0,
    var customerType: String? = null, // retail, wholesale, VIP
    var customerTier: String? = null, // gold, silver, bronze
    var lastPurchaseDate: Long = 0,
    var totalPurchaseAmount: Double = 0.0,
    var purchaseFrequency: Int = 0,
    var paymentTerms: String? = null,
    var discountRate: Double = 0.0,
    var rating: Double = 0.0,
    var leadTime: Int = 0,
    var performanceScore: Double = 0.0,
    var preferredCustomer: Boolean = false,
    var contractDetails: String? = null,
    var productsPurchased: String? = null,
    var bankAccount: String? = null,
    var taxId: String? = null
) : Person
