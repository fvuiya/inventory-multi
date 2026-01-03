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
class SupplierProfileViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _supplier = MutableLiveData<Supplier>()
    val supplier: LiveData<Supplier> = _supplier

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadSupplier(supplierId: String) {
        _loading.value = true
        viewModelScope.launch {
            supplierRepository.getSupplier(supplierId)
                .onSuccess {
                    _supplier.value = it
                    _loading.value = false
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                    _loading.value = false
                }
        }
    }
}
