package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Supplier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SupplierRepositoryImpl(private val db: FirebaseFirestore) : SupplierRepository {

    override suspend fun getSupplier(supplierId: String): Result<Supplier> {
        return try {
            val snapshot = db.collection("suppliers").document(supplierId).get().await()
            if (snapshot.exists()) {
                val supplier = mapToSupplier(snapshot.data!!, snapshot.id)
                Result.success(supplier)
            } else {
                Result.failure(Exception("Supplier not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuppliers(): Result<List<Supplier>> {
        return try {
            val snapshot = db.collection("suppliers")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val suppliers = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { mapToSupplier(it, doc.id) }
            }
            Result.success(suppliers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSupplier(supplier: Supplier): Result<String> {
        return try {
            val supplierRef = if (supplier.documentId.isNotEmpty()) {
                db.collection("suppliers").document(supplier.documentId)
            } else {
                db.collection("suppliers").document()
            }
            
            val supplierMap = mapFromSupplier(supplier.copy(documentId = supplierRef.id))
            supplierRef.set(supplierMap).await()
            
            Result.success(supplierRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Unit> {
        return try {
            if (supplier.documentId.isEmpty()) {
                return Result.failure(Exception("Supplier ID is required for update"))
            }
            val supplierMap = mapFromSupplier(supplier)
            db.collection("suppliers").document(supplier.documentId).set(supplierMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSupplier(supplierId: String): Result<Unit> {
        return try {
             // Soft delete by setting isActive to false
            db.collection("suppliers").document(supplierId).update("isActive", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToSupplier(data: Map<String, Any>, id: String): Supplier {
        return Supplier(
            documentId = id,
            name = data["name"] as? String ?: "",
            contactNumber = data["contactNumber"] as? String ?: "",
            address = data["address"] as? String ?: "",
            age = (data["age"] as? Number)?.toInt() ?: 0,
            photo = data["photo"] as? String,
            isActive = data["isActive"] as? Boolean ?: true,
            rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
            paymentTerms = data["paymentTerms"] as? String,
            leadTime = (data["leadTime"] as? Number)?.toInt() ?: 0,
            performanceScore = (data["performanceScore"] as? Number)?.toDouble() ?: 0.0,
            preferredSupplier = data["preferredSupplier"] as? Boolean ?: false,
            outstandingPayment = (data["outstandingPayment"] as? Number)?.toDouble() ?: 0.0,
            contractDetails = data["contractDetails"] as? String,
            productsSupplied = data["productsSupplied"] as? String,
            lastDeliveryDate = (data["lastDeliveryDate"] as? com.google.firebase.Timestamp)?.toDate()?.time
                ?: (data["lastDeliveryDate"] as? Number)?.toLong() ?: 0L,
            bankAccount = data["bankAccount"] as? String,
            taxId = data["taxId"] as? String,
            creationDate = (data["creationDate"] as? com.google.firebase.Timestamp)?.toDate()?.time
                ?: (data["creationDate"] as? Number)?.toLong() ?: 0L,
            totalSupplyAmount = (data["totalSupplyAmount"] as? Number)?.toDouble() ?: 0.0,
            supplyFrequency = (data["supplyFrequency"] as? Number)?.toInt() ?: 0,
            supplierType = data["supplierType"] as? String,
            supplierTier = data["supplierTier"] as? String
        )
    }

    private fun mapFromSupplier(supplier: Supplier): Map<String, Any?> {
        return mapOf(
            "documentId" to supplier.documentId,
            "name" to supplier.name,
            "contactNumber" to supplier.contactNumber,
            "address" to supplier.address,
            "age" to supplier.age,
            "photo" to supplier.photo,
            "isActive" to supplier.isActive,
            "rating" to supplier.rating,
            "paymentTerms" to supplier.paymentTerms,
            "leadTime" to supplier.leadTime,
            "performanceScore" to supplier.performanceScore,
            "preferredSupplier" to supplier.preferredSupplier,
            "outstandingPayment" to supplier.outstandingPayment,
            "contractDetails" to supplier.contractDetails,
            "productsSupplied" to supplier.productsSupplied,
            "lastDeliveryDate" to supplier.lastDeliveryDate,
            "bankAccount" to supplier.bankAccount,
            "taxId" to supplier.taxId,
            "creationDate" to supplier.creationDate,
            "totalSupplyAmount" to supplier.totalSupplyAmount,
            "supplyFrequency" to supplier.supplyFrequency,
            "supplierType" to supplier.supplierType,
            "supplierTier" to supplier.supplierTier
        )
    }
}
