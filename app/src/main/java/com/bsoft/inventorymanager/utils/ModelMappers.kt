package com.bsoft.inventorymanager.utils

import com.bsoft.inventorymanager.model.Purchase as SharedPurchase
import com.bsoft.inventorymanager.model.PurchaseItem as SharedPurchaseItem
import com.bsoft.inventorymanager.models.Purchase as JavaPurchase
import com.bsoft.inventorymanager.models.PurchaseItem as JavaPurchaseItem

/**
 * Mapper utilities to convert between Java models (in :app) and Kotlin shared models (in :shared).
 * Use these during gradual migration to allow mixed usage.
 */
object ModelMappers {

    // ==================== Purchase ====================
    
    fun JavaPurchase.toShared(): SharedPurchase {
        return SharedPurchase(
            documentId = this.documentId ?: "",
            supplierId = this.supplierId ?: "",
            supplierName = this.supplierName ?: "",
            supplierContactNumber = this.supplierContactNumber ?: "",
            purchaseDate = this.purchaseDate?.toDate()?.time ?: 0L,
            purchaseOrderNumber = this.purchaseOrderNumber,
            expectedDeliveryDate = this.expectedDeliveryDate?.toDate()?.time,
            actualDeliveryDate = this.actualDeliveryDate?.toDate()?.time,
            deliveryStatus = this.deliveryStatus ?: "",
            purchaseStatus = this.purchaseStatus ?: "",
            totalAmount = this.totalAmount,
            amountPaid = this.amountPaid,
            amountDue = this.amountDue,
            taxAmount = this.taxAmount,
            discountAmount = this.discountAmount,
            paymentStatus = this.paymentStatus ?: "",
            paymentMethod = this.paymentMethod ?: "",
            notes = this.notes,
            items = this.items?.map { it.toShared() } ?: emptyList(),
            productIds = this.productIds ?: emptyList(),
            userId = this.userId ?: "",
            status = this.status ?: "",
            deliveryAddress = this.deliveryAddress,
            invoiceNumber = this.invoiceNumber
        )
    }
    
    fun SharedPurchase.toJava(): JavaPurchase {
        val purchase = JavaPurchase()
        purchase.documentId = this.documentId
        purchase.supplierId = this.supplierId
        purchase.supplierName = this.supplierName
        purchase.supplierContactNumber = this.supplierContactNumber
        if (this.purchaseDate > 0) {
            purchase.purchaseDate = com.google.firebase.Timestamp(java.util.Date(this.purchaseDate))
        }
        purchase.purchaseOrderNumber = this.purchaseOrderNumber
        this.expectedDeliveryDate?.let { 
            purchase.expectedDeliveryDate = com.google.firebase.Timestamp(java.util.Date(it))
        }
        this.actualDeliveryDate?.let {
            purchase.actualDeliveryDate = com.google.firebase.Timestamp(java.util.Date(it))
        }
        purchase.deliveryStatus = this.deliveryStatus
        purchase.purchaseStatus = this.purchaseStatus
        purchase.totalAmount = this.totalAmount
        purchase.amountPaid = this.amountPaid
        purchase.amountDue = this.amountDue
        purchase.taxAmount = this.taxAmount
        purchase.discountAmount = this.discountAmount
        purchase.paymentStatus = this.paymentStatus
        purchase.paymentMethod = this.paymentMethod
        purchase.notes = this.notes
        purchase.items = this.items.map { it.toJava() }
        purchase.productIds = this.productIds
        purchase.userId = this.userId
        purchase.status = this.status
        purchase.deliveryAddress = this.deliveryAddress
        purchase.invoiceNumber = this.invoiceNumber
        return purchase
    }

    // ==================== PurchaseItem ====================
    
    fun JavaPurchaseItem.toShared(): SharedPurchaseItem {
        return SharedPurchaseItem(
            productId = this.productId ?: "",
            productName = this.productName ?: "",
            productCode = null, // Private in Java
            quantity = this.quantity,
            pricePerItem = this.pricePerItem,
            totalPrice = this.totalPrice,
            returnedQuantity = this.returnedQuantity,
            expiryDate = null // Private in Java
        )
    }
    
    fun SharedPurchaseItem.toJava(): JavaPurchaseItem {
        val item = JavaPurchaseItem()
        item.productId = this.productId
        item.productName = this.productName
        item.quantity = this.quantity
        item.pricePerItem = this.pricePerItem
        item.totalPrice = this.totalPrice
        item.returnedQuantity = this.returnedQuantity
        return item
    }
    
    // ==================== Extension for Lists ====================
    
    fun List<JavaPurchase>.toSharedList(): List<SharedPurchase> = this.map { it.toShared() }
    fun List<SharedPurchase>.toJavaList(): List<JavaPurchase> = this.map { it.toJava() }
    
    // ==================== Sale ====================
    
    fun com.bsoft.inventorymanager.models.Sale.toShared(): com.bsoft.inventorymanager.model.Sale {
        return com.bsoft.inventorymanager.model.Sale(
            documentId = this.documentId ?: "",
            customerId = this.customerId ?: "",
            customerName = this.customerName ?: "",
            customerPhoneNumber = this.customerPhoneNumber ?: "",
            salesPerson = this.salesPerson,
            commissionRate = this.commissionRate,
            saleDate = this.saleDate?.toDate()?.time ?: 0L,
            invoiceNumber = this.invoiceNumber,
            totalAmount = this.totalAmount,
            amountPaid = this.amountPaid,
            amountDue = this.amountDue,
            taxAmount = this.taxAmount,
            discountAmount = this.discountAmount,
            paymentMethod = this.paymentMethod,
            deliveryStatus = this.deliveryStatus,
            deliveryAddress = this.deliveryAddress,
            deliveryCharge = this.deliveryCharge,
            loyaltyPointsEarned = this.loyaltyPointsEarned,
            salesChannel = this.salesChannel,
            notes = this.notes,
            items = this.items?.map { it.toShared() } ?: emptyList(),
            productIds = this.productIds ?: emptyList(),
            userId = this.userId ?: "",
            status = this.status ?: "",
            subtotal = this.subtotal,
            totalCost = this.totalCost,
            totalProfit = this.totalProfit
        )
    }
    
    fun com.bsoft.inventorymanager.model.Sale.toJava(): com.bsoft.inventorymanager.models.Sale {
        val sale = com.bsoft.inventorymanager.models.Sale()
        sale.documentId = this.documentId
        sale.customerId = this.customerId
        sale.customerName = this.customerName
        sale.customerPhoneNumber = this.customerPhoneNumber
        sale.salesPerson = this.salesPerson
        sale.commissionRate = this.commissionRate
        if (this.saleDate > 0) {
            sale.saleDate = com.google.firebase.Timestamp(java.util.Date(this.saleDate))
        }
        sale.invoiceNumber = this.invoiceNumber
        sale.totalAmount = this.totalAmount
        sale.amountPaid = this.amountPaid
        sale.amountDue = this.amountDue
        sale.taxAmount = this.taxAmount
        sale.discountAmount = this.discountAmount
        sale.paymentMethod = this.paymentMethod
        sale.deliveryStatus = this.deliveryStatus
        sale.deliveryAddress = this.deliveryAddress
        sale.deliveryCharge = this.deliveryCharge
        sale.loyaltyPointsEarned = this.loyaltyPointsEarned
        sale.salesChannel = this.salesChannel
        sale.notes = this.notes
        sale.items = this.items.map { it.toJava() }
        sale.productIds = this.productIds
        sale.userId = this.userId
        sale.status = this.status
        sale.subtotal = this.subtotal
        sale.totalCost = this.totalCost
        sale.totalProfit = this.totalProfit
        return sale
    }
    
    // ==================== SaleItem ====================
    
    fun com.bsoft.inventorymanager.models.SaleItem.toShared(): com.bsoft.inventorymanager.model.SaleItem {
        return com.bsoft.inventorymanager.model.SaleItem(
            productId = this.productId ?: "",
            productName = this.productName ?: "",
            quantity = this.quantity,
            pricePerItem = this.pricePerItem,
            totalPrice = this.totalPrice,
            returnedQuantity = this.returnedQuantity,
            category = this.category,
            brand = this.brand,
            costPrice = this.costPrice
        )
    }
    
    fun com.bsoft.inventorymanager.model.SaleItem.toJava(): com.bsoft.inventorymanager.models.SaleItem {
        val item = com.bsoft.inventorymanager.models.SaleItem()
        item.productId = this.productId
        item.productName = this.productName
        item.quantity = this.quantity
        item.pricePerItem = this.pricePerItem
        item.totalPrice = this.totalPrice
        item.returnedQuantity = this.returnedQuantity
        item.category = this.category
        item.brand = this.brand
        item.costPrice = this.costPrice
        return item
    }
    
    // Sale list extensions
    @JvmName("salesToSharedList")
    fun List<com.bsoft.inventorymanager.models.Sale>.toSharedSaleList(): List<com.bsoft.inventorymanager.model.Sale> = this.map { it.toShared() }
    
    @JvmName("salesToJavaList")
    fun List<com.bsoft.inventorymanager.model.Sale>.toJavaSaleList(): List<com.bsoft.inventorymanager.models.Sale> = this.map { it.toJava() }
}
