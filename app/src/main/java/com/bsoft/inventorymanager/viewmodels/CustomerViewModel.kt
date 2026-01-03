package com.bsoft.inventorymanager.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsoft.inventorymanager.model.Customer
import com.bsoft.inventorymanager.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> = _customers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationSuccess = MutableLiveData<Boolean?>()
    val operationSuccess: LiveData<Boolean?> = _operationSuccess

    fun loadCustomers() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            customerRepository.getCustomers()
                .onSuccess {
                    _customers.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message ?: "Failed to load customers"
                    _isLoading.value = false
                }
        }
    }

    fun saveCustomer(customer: Customer) {
        _isLoading.value = true
        _error.value = null
        _operationSuccess.value = null
        viewModelScope.launch {
            if (customer.documentId.isEmpty()) {
                customerRepository.saveCustomer(customer)
                    .onSuccess {
                        _operationSuccess.value = true
                        _isLoading.value = false
                        loadCustomers() // Refresh list
                    }
                    .onFailure {
                        _operationSuccess.value = false
                        _error.value = it.message ?: "Failed to save customer"
                        _isLoading.value = false
                    }
            } else {
                customerRepository.updateCustomer(customer)
                    .onSuccess {
                        _operationSuccess.value = true
                        _isLoading.value = false
                        loadCustomers() // Refresh list
                    }
                    .onFailure {
                        _operationSuccess.value = false
                        _error.value = it.message ?: "Failed to update customer"
                        _isLoading.value = false
                    }
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        _isLoading.value = true
        _error.value = null
        _operationSuccess.value = null
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerId)
                .onSuccess {
                    _operationSuccess.value = true
                    _isLoading.value = false
                    loadCustomers() // Refresh list
                }
                .onFailure {
                    _operationSuccess.value = false
                    _error.value = it.message ?: "Failed to delete customer"
                    _isLoading.value = false
                }
        }
    }
    
    fun resetOperationStatus() {
        _operationSuccess.value = null
    }
}

