package com.bsoft.inventorymanager.repository

import com.bsoft.inventorymanager.model.Sale
import com.bsoft.inventorymanager.model.SaleItem
import com.bsoft.inventorymanager.model.Product
import com.bsoft.inventorymanager.model.Customer

interface SaleRepository {
    suspend fun getSale(saleId: String): Result<Sale>
    suspend fun saveSale(sale: Sale, items: List<SaleItem>): Result<String> // Returns sale ID
    suspend fun updateSale(sale: Sale, items: List<SaleItem>): Result<Unit>
    
    suspend fun getProductByBarcode(barcode: String): Result<Product?>
    suspend fun getProduct(productId: String): Result<Product>
    
    suspend fun getCustomers(): Result<List<Customer>>
}
