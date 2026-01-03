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
class CustomerProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _customer = MutableLiveData<Customer?>()
    val customer: LiveData<Customer?> = _customer

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCustomer(customerId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            customerRepository.getCustomer(customerId)
                .onSuccess {
                    _customer.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Failed to load customer: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
}
