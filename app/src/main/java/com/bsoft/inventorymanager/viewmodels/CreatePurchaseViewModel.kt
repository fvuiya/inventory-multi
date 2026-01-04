package com.bsoft.inventorymanager.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bsoft.inventorymanager.models.Product
import com.bsoft.inventorymanager.models.ProductSelection
import com.bsoft.inventorymanager.models.Purchase
import com.bsoft.inventorymanager.models.PurchaseItem
import com.bsoft.inventorymanager.model.Supplier
import com.bsoft.inventorymanager.repositories.PurchaseRepository
import com.bsoft.inventorymanager.utils.FinancialCalculator
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreatePurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _productSelections = MutableLiveData<List<ProductSelection>>(ArrayList())
    val productSelections: LiveData<List<ProductSelection>> = _productSelections

    private val _subtotal = MutableLiveData(0.0)
    val subtotal: LiveData<Double> = _subtotal

    private val _isLoading = MutableLiveData(false)
    
    @get:JvmName("getIsLoading")
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _purchaseSuccess = MutableLiveData<String?>()
    val purchaseSuccess: LiveData<String?> = _purchaseSuccess

    private val _suppliers = MutableLiveData<List<Supplier>>()
    val suppliers: LiveData<List<Supplier>> = _suppliers

    private val _selectedSupplier = MutableLiveData<Supplier?>()
    val selectedSupplier: LiveData<Supplier?> = _selectedSupplier

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        _isLoading.value = true
        purchaseRepository.getSuppliers(object : PurchaseRepository.SuppliersCallback {
            override fun onSuccess(supplierList: List<Supplier>) {
                _isLoading.value = false
                _suppliers.value = supplierList
            }

            override fun onFailure(e: Exception) {
                _isLoading.value = false
                _error.value = "Failed to load suppliers: ${e.message}"
            }
        })
    }

    fun setSelectedSupplier(supplier: Supplier?) {
        _selectedSupplier.value = supplier
    }

    fun addProduct(product: Product?) {
        if (product == null) return

        val currentItems = _productSelections.value?.toMutableList() ?: mutableListOf()

        var found = false
        for (item in currentItems) {
            if (item.product.documentId == product.documentId) {
                item.quantityInSale = item.quantityInSale + 1
                found = true
                break
            }
        }

        if (!found) {
            currentItems.add(ProductSelection(product, 1))
        }

        _productSelections.value = currentItems
        recalculateSubtotal()
    }

    fun loadProductByBarcode(barcode: String) {
        _isLoading.value = true
        purchaseRepository.getProductByBarcode(barcode, object : PurchaseRepository.ProductCallback {
            override fun onSuccess(product: Product) {
                _isLoading.value = false
                addProduct(product)
            }

            override fun onFailure(e: Exception) {
                _isLoading.value = false
                _error.value = "Product not found or error: ${e.message}"
            }
        })
    }

    fun removeProduct(position: Int) {
        val currentItems = _productSelections.value?.toMutableList()
        if (currentItems != null && position >= 0 && position < currentItems.size) {
            currentItems.removeAt(position)
            _productSelections.value = currentItems
            recalculateSubtotal()
        }
    }

    fun updateItemQuantity(position: Int, quantity: Int) {
        val currentItems = _productSelections.value
        if (currentItems != null && position >= 0 && position < currentItems.size) {
            val item = currentItems[position]
            item.quantityInSale = quantity
            // CRITICAL FIX: Do NOT call productSelections.setValue(currentItems) here.
            // This prevents the Adapter from refreshing/losing focus while typing.
            recalculateSubtotal()
        }
    }

    fun updateItemPrice(position: Int, price: Double) {
        val currentItems = _productSelections.value
        if (currentItems != null && position >= 0 && position < currentItems.size) {
            val item = currentItems[position]
            item.product.purchasePrice = price
            // CRITICAL FIX: Do NOT call productSelections.setValue(currentItems) here.
            recalculateSubtotal()
        }
    }

    private fun recalculateSubtotal() {
        var sub = 0.0
        val items = _productSelections.value
        if (items != null) {
            for (item in items) {
                sub += FinancialCalculator.calculateLineItemTotal(
                    item.product.purchasePrice,
                    item.quantityInSale
                )
            }
        }
        _subtotal.value = sub
    }

    fun savePurchase(
        purchaseDate: Date?,
        taxAmount: Double,
        discountAmount: Double,
        paymentMethod: String,
        amountPaid: Double,
        notes: String
    ) {
        if (_isLoading.value == true) return

        val selections = _productSelections.value
        if (selections == null || selections.isEmpty()) {
            _error.value = "No products selected"
            return
        }

        if (_selectedSupplier.value == null) {
            _error.value = "No supplier selected"
            return
        }

        _isLoading.value = true

        val purchase = Purchase()
        purchase.supplierId = _selectedSupplier.value!!.documentId
        purchase.supplierName = _selectedSupplier.value!!.name
        purchase.supplierContactNumber = _selectedSupplier.value!!.contactNumber

        val dateParam = purchaseDate ?: Date()
        purchase.purchaseDate = Timestamp(dateParam)

        val items = mutableListOf<PurchaseItem>()
        val productIds = mutableListOf<String>()
        for (sel in selections) {
            val item = PurchaseItem()
            item.productId = sel.product.documentId
            item.productName = sel.product.name
            item.pricePerItem = sel.product.purchasePrice
            item.quantity = sel.quantityInSale
            items.add(item)
            productIds.add(sel.product.documentId)
        }
        purchase.items = items
        purchase.productIds = productIds

        val sub = _subtotal.value ?: 0.0

        purchase.taxAmount = taxAmount
        purchase.discountAmount = discountAmount
        purchase.totalAmount = FinancialCalculator.calculateTotalAmount(sub, taxAmount, discountAmount)
        purchase.paymentMethod = paymentMethod
        purchase.amountPaid = amountPaid
        purchase.notes = notes
        purchase.amountDue = purchase.totalAmount - amountPaid
        purchase.userId = "CURRENT_USER_ID" // TODO: Real user
        purchase.status = "COMPLETED"

        purchaseRepository.savePurchase(purchase, items, object : PurchaseRepository.PurchaseCallback {
            override fun onSuccess(purchaseId: String) {
                _isLoading.postValue(false)
                _purchaseSuccess.postValue(purchaseId)
            }

            override fun onFailure(e: Exception) {
                _isLoading.postValue(false)
                _error.postValue("Failed to save purchase: ${e.message}")
            }
        })
    }

    fun loadPurchaseForEditing(purchaseId: String) {
        _isLoading.value = true
        purchaseRepository.getPurchase(purchaseId, object : PurchaseRepository.GetPurchaseCallback {
            override fun onSuccess(purchase: Purchase) {
                val items = purchase.items
                reconstructSelections(items)
                loadSupplierById(purchase.supplierId)
                _isLoading.value = false
            }

            override fun onFailure(e: Exception) {
                _isLoading.value = false
                _error.value = "Failed to load purchase: ${e.message}"
            }
        })
    }

    private fun loadSupplierById(supplierId: String) {
        val currentList = _suppliers.value
        if (currentList != null) {
            for (s in currentList) {
                if (s.documentId == supplierId) {
                    _selectedSupplier.value = s
                    return
                }
            }
        }
        // If not in list (e.g. pagination or inactive), we might need direct fetch.
        // For now rely on list.
    }

    private fun reconstructSelections(items: List<PurchaseItem>) {
        val list = mutableListOf<ProductSelection>()
        for (item in items) {
            val p = Product()
            p.documentId = item.productId
            p.name = item.productName
            p.purchasePrice = item.pricePerItem

            val sel = ProductSelection(p, item.quantity)
            list.add(sel)
        }
        _productSelections.value = list
        recalculateSubtotal()
    }
}
