package com.bsoft.inventorymanager.utils

import com.bsoft.inventorymanager.model.Purchase as SharedPurchase
import com.bsoft.inventorymanager.model.PurchaseItem as SharedPurchaseItem
import com.bsoft.inventorymanager.model.Supplier as SharedSupplier
import com.bsoft.inventorymanager.models.Purchase as JavaPurchase
import com.bsoft.inventorymanager.models.PurchaseItem as JavaPurchaseItem
import com.bsoft.inventorymanager.models.Supplier as JavaSupplier

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
}
