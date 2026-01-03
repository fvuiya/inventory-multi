package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Customer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CustomerRepositoryImpl(private val db: FirebaseFirestore) : CustomerRepository {

    override suspend fun getCustomer(customerId: String): Result<Customer> {
        return try {
            val snapshot = db.collection("customers").document(customerId).get().await()
            if (snapshot.exists()) {
                val customer = mapToCustomer(snapshot.data!!, snapshot.id)
                Result.success(customer)
            } else {
                Result.failure(Exception("Customer not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomers(): Result<List<Customer>> {
        return try {
            val snapshot = db.collection("customers")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val customers = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { mapToCustomer(it, doc.id) }
            }
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCustomer(customer: Customer): Result<String> {
        return try {
            val customerRef = if (customer.documentId.isNotEmpty()) {
                db.collection("customers").document(customer.documentId)
            } else {
                db.collection("customers").document()
            }
            
            val customerMap = mapFromCustomer(customer.copy(documentId = customerRef.id))
            customerRef.set(customerMap).await()
            
            Result.success(customerRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            if (customer.documentId.isEmpty()) {
                return Result.failure(Exception("Customer ID is required for update"))
            }
            val customerMap = mapFromCustomer(customer)
            db.collection("customers").document(customer.documentId).set(customerMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(customerId: String): Result<Unit> {
        return try {
             // Soft delete by setting isActive to false
            db.collection("customers").document(customerId).update("isActive", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToCustomer(data: Map<String, Any>, id: String): Customer {
        return Customer(
            documentId = id,
            name = data["name"] as? String ?: "",
            contactNumber = data["contactNumber"] as? String ?: "",
            address = data["address"] as? String ?: "",
            age = (data["age"] as? Number)?.toInt() ?: 0,
            photo = data["photo"] as? String,
            isActive = data["isActive"] as? Boolean ?: true,
            creationDate = (data["creationDate"] as? com.google.firebase.Timestamp)?.toDate()?.time
                ?: (data["creationDate"] as? Number)?.toLong() ?: 0L,
            creditLimit = (data["creditLimit"] as? Number)?.toDouble() ?: 0.0,
            outstandingBalance = (data["outstandingBalance"] as? Number)?.toDouble() ?: 0.0,
            customerType = data["customerType"] as? String,
            customerTier = data["customerTier"] as? String,
            lastPurchaseDate = (data["lastPurchaseDate"] as? com.google.firebase.Timestamp)?.toDate()?.time
                ?: (data["lastPurchaseDate"] as? Number)?.toLong() ?: 0L,
            totalPurchaseAmount = (data["totalPurchaseAmount"] as? Number)?.toDouble() ?: 0.0,
            purchaseFrequency = (data["purchaseFrequency"] as? Number)?.toInt() ?: 0,
            paymentTerms = data["paymentTerms"] as? String,
            discountRate = (data["discountRate"] as? Number)?.toDouble() ?: 0.0,
            rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
            leadTime = (data["leadTime"] as? Number)?.toInt() ?: 0,
            performanceScore = (data["performanceScore"] as? Number)?.toDouble() ?: 0.0,
            preferredCustomer = data["preferredCustomer"] as? Boolean ?: false,
            contractDetails = data["contractDetails"] as? String,
            productsPurchased = data["productsPurchased"] as? String,
            bankAccount = data["bankAccount"] as? String,
            taxId = data["taxId"] as? String
        )
    }

    private fun mapFromCustomer(customer: Customer): Map<String, Any?> {
        return mapOf(
            "documentId" to customer.documentId,
            "name" to customer.name,
            "contactNumber" to customer.contactNumber,
            "address" to customer.address,
            "age" to customer.age,
            "photo" to customer.photo,
            "isActive" to customer.isActive,
            "creationDate" to customer.creationDate,
            "creditLimit" to customer.creditLimit,
            "outstandingBalance" to customer.outstandingBalance,
            "customerType" to customer.customerType,
            "customerTier" to customer.customerTier,
            "lastPurchaseDate" to customer.lastPurchaseDate,
            "totalPurchaseAmount" to customer.totalPurchaseAmount,
            "purchaseFrequency" to customer.purchaseFrequency,
            "paymentTerms" to customer.paymentTerms,
            "discountRate" to customer.discountRate,
            "rating" to customer.rating,
            "leadTime" to customer.leadTime,
            "performanceScore" to customer.performanceScore,
            "preferredCustomer" to customer.preferredCustomer,
            "contractDetails" to customer.contractDetails,
            "productsPurchased" to customer.productsPurchased,
            "bankAccount" to customer.bankAccount,
            "taxId" to customer.taxId
        )
    }
}
