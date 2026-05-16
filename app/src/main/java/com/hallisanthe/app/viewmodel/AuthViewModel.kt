package com.hallisanthe.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {
    private val _authState = MutableLiveData<Resource<Unit>>()
    val authState: LiveData<Resource<Unit>> = _authState

    fun login(email: String, password: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = repository.register(name, email, password, role)
        }
    }
}
