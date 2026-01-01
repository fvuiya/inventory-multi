package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Customer
import com.bsoft.inventorymanager.model.Product
import com.bsoft.inventorymanager.model.Sale
import com.bsoft.inventorymanager.model.SaleItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class SaleRepositoryImpl(private val db: FirebaseFirestore) : SaleRepository {

    override suspend fun getSale(saleId: String): Result<Sale> {
        return try {
            val snapshot = db.collection("sales").document(saleId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return Result.failure(Exception("No data"))
                val sale = mapToSale(data, snapshot.id)
                Result.success(sale)
            } else {
                Result.failure(Exception("Sale not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSale(sale: Sale, items: List<SaleItem>): Result<String> {
        return try {
            val batch = db.batch()
            val saleRef = if (sale.documentId.isNotEmpty()) {
                db.collection("sales").document(sale.documentId)
            } else {
                db.collection("sales").document()
            }
            
            val finalSale = sale.copy(documentId = saleRef.id, items = items)
            val saleMap = mapFromSale(finalSale)
            
            batch.set(saleRef, saleMap)

            // Update stock (decrease for sales)
            for (item in items) {
                if (item.productId.isNotEmpty()) {
                    val productRef = db.collection("products").document(item.productId)
                    batch.update(productRef, "quantity", FieldValue.increment(-item.quantity.toLong()))
                }
            }
            
            batch.commit().await()
            Result.success(saleRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSale(sale: Sale, items: List<SaleItem>): Result<Unit> {
        return try {
            val batch = db.batch()
            val saleRef = db.collection("sales").document(sale.documentId)
            val saleMap = mapFromSale(sale.copy(items = items))
            
            batch.set(saleRef, saleMap)
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Result<Product?> {
        return try {
            val query = db.collection("products")
                .whereEqualTo("productCode", barcode)
                .limit(1)
                .get()
                .await()
            
            if (!query.isEmpty) {
                val doc = query.documents[0]
                val product = mapToProduct(doc.data!!, doc.id)
                Result.success(product)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProduct(productId: String): Result<Product> {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            if (snapshot.exists()) {
                val product = mapToProduct(snapshot.data!!, snapshot.id)
                Result.success(product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomers(): Result<List<Customer>> {
        return try {
            val query = db.collection("customers")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val customers = query.map { doc ->
                mapToCustomer(doc.data, doc.id)
            }
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Mappers ---

    private fun mapToSale(data: Map<String, Any>, id: String): Sale {
        return Sale(
            documentId = id,
            customerId = data["customerId"] as? String ?: "",
            customerName = data["customerName"] as? String ?: "",
            customerPhoneNumber = data["customerPhoneNumber"] as? String ?: "",
            salesPerson = data["salesPerson"] as? String,
            commissionRate = (data["commissionRate"] as? Number)?.toDouble() ?: 0.0,
            saleDate = (data["saleDate"] as? Timestamp)?.toDate()?.time ?: 0L,
            invoiceNumber = data["invoiceNumber"] as? String,
            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
            amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
            amountDue = (data["amountDue"] as? Number)?.toDouble() ?: 0.0,
            taxAmount = (data["taxAmount"] as? Number)?.toDouble() ?: 0.0,
            discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
            paymentMethod = data["paymentMethod"] as? String,
            deliveryStatus = data["deliveryStatus"] as? String,
            deliveryAddress = data["deliveryAddress"] as? String,
            deliveryCharge = (data["deliveryCharge"] as? Number)?.toDouble() ?: 0.0,
            notes = data["notes"] as? String,
            items = (data["items"] as? List<Map<String, Any>>)?.map { mapToSaleItem(it) } ?: emptyList(),
            productIds = (data["productIds"] as? List<String>) ?: emptyList(),
            userId = data["userId"] as? String ?: "",
            status = data["status"] as? String ?: ""
        )
    }
    
    private fun mapFromSale(sale: Sale): Map<String, Any?> {
        return mapOf(
            "documentId" to sale.documentId,
            "customerId" to sale.customerId,
            "customerName" to sale.customerName,
            "customerPhoneNumber" to sale.customerPhoneNumber,
            "salesPerson" to sale.salesPerson,
            "commissionRate" to sale.commissionRate,
            "saleDate" to Timestamp(Date(sale.saleDate)),
            "invoiceNumber" to sale.invoiceNumber,
            "totalAmount" to sale.totalAmount,
            "amountPaid" to sale.amountPaid,
            "amountDue" to sale.amountDue,
            "taxAmount" to sale.taxAmount,
            "discountAmount" to sale.discountAmount,
            "paymentMethod" to sale.paymentMethod,
            "deliveryStatus" to sale.deliveryStatus,
            "deliveryAddress" to sale.deliveryAddress,
            "deliveryCharge" to sale.deliveryCharge,
            "notes" to sale.notes,
            "items" to sale.items.map { mapFromSaleItem(it) },
            "productIds" to sale.productIds,
            "userId" to sale.userId,
            "status" to sale.status
        )
    }

    private fun mapToSaleItem(data: Map<String, Any>): SaleItem {
        return SaleItem(
            productId = data["productId"] as? String ?: "",
            productName = data["productName"] as? String ?: "",
            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
            pricePerItem = (data["pricePerItem"] as? Number)?.toDouble() ?: 0.0,
            totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0,
            returnedQuantity = (data["returnedQuantity"] as? Number)?.toInt() ?: 0
        )
    }

    private fun mapFromSaleItem(item: SaleItem): Map<String, Any?> {
        return mapOf(
            "productId" to item.productId,
            "productName" to item.productName,
            "quantity" to item.quantity,
            "pricePerItem" to item.pricePerItem,
            "totalPrice" to item.totalPrice,
            "returnedQuantity" to item.returnedQuantity
        )
    }
    
    private fun mapToProduct(data: Map<String, Any>, id: String): Product {
        return Product(
            documentId = id,
            name = data["name"] as? String ?: "",
            barcode = data["barcode"] as? String ?: "",
            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
            productCode = data["productCode"] as? String ?: ""
        )
    }

    private fun mapToCustomer(data: Map<String, Any>, id: String): Customer {
        return Customer(
            documentId = id,
            name = data["name"] as? String ?: "",
            contactNumber = data["contactNumber"] as? String ?: "",
            address = data["address"] as? String ?: ""
        )
    }
}
