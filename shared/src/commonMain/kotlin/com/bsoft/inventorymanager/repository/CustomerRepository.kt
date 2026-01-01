package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Customer

interface CustomerRepository {
    suspend fun getCustomer(customerId: String): Result<Customer>
    suspend fun getCustomers(): Result<List<Customer>>
    suspend fun saveCustomer(customer: Customer): Result<String> // Returns customer ID
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    suspend fun deleteCustomer(customerId: String): Result<Unit>
}
