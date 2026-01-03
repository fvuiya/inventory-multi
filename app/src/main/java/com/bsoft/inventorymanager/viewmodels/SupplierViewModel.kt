package com.bsoft.inventorymanager.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsoft.inventorymanager.model.Supplier
import com.bsoft.inventorymanager.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _suppliers = MutableLiveData<List<Supplier>>()
    val suppliers: LiveData<List<Supplier>> = _suppliers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSuppliers() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            supplierRepository.getSuppliers()
                .onSuccess {
                    _suppliers.value = it
                    _loading.value = false
                }
                .onFailure {
                    _error.value = it.message
                    _loading.value = false
                }
        }
    }

    private val _lastSavedSupplierId = MutableLiveData<String?>()
    val lastSavedSupplierId: LiveData<String?> = _lastSavedSupplierId

    fun saveSupplier(supplier: Supplier) {
        _loading.value = true
        _error.value = null
        _lastSavedSupplierId.value = null
        viewModelScope.launch {
            val result = if (supplier.documentId.isNotEmpty()) {
                supplierRepository.updateSupplier(supplier)
                    .map { supplier.documentId }
            } else {
                supplierRepository.saveSupplier(supplier)
            }
            
            result
                .onSuccess { id ->
                    _lastSavedSupplierId.value = id
                    loadSuppliers() // Reload list
                }
                .onFailure {
                    _error.value = it.message
                    _loading.value = false
                }
        }
    }

    fun deleteSupplier(supplierId: String) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            supplierRepository.deleteSupplier(supplierId)
                .onSuccess {
                    loadSuppliers()
                }
                .onFailure {
                    _error.value = it.message
                    _loading.value = false
                }
        }
    }
}
