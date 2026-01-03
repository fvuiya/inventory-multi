package com.bsoft.inventorymanager.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Supplier @JvmOverloads constructor(
    override var documentId: String = "",
    override var name: String = "",
    override var contactNumber: String = "",
    override var address: String = "",
    override var age: Int = 0,
    override var photo: String? = null,
    @SerialName("isActive")
    override var isActive: Boolean = true,
    var rating: Double = 0.0,
    var paymentTerms: String? = null,
    var leadTime: Int = 0,
    var performanceScore: Double = 0.0,
    var preferredSupplier: Boolean = false,
    var outstandingPayment: Double = 0.0,
    var contractDetails: String? = null,
    var productsSupplied: String? = null,
    var lastDeliveryDate: Long = 0,
    var bankAccount: String? = null,
    var taxId: String? = null,
    var creationDate: Long = 0,
    var totalSupplyAmount: Double = 0.0,
    var supplyFrequency: Int = 0,
    var supplierType: String? = null,
    var supplierTier: String? = null
) : Person
