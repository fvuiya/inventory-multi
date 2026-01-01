package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Supplier

interface SupplierRepository {
    suspend fun getSupplier(supplierId: String): Result<Supplier>
    suspend fun getSuppliers(): Result<List<Supplier>>
    suspend fun saveSupplier(supplier: Supplier): Result<String> // Returns supplier ID
    suspend fun updateSupplier(supplier: Supplier): Result<Unit>
    suspend fun deleteSupplier(supplierId: String): Result<Unit>
}
