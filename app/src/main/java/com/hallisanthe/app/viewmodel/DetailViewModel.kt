package com.hallisanthe.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {

    private val _productState = MutableLiveData<Resource<Product>>()
    val productState: LiveData<Resource<Product>> = _productState
    private val _wishlistState = MutableLiveData<Resource<Boolean>>()
    val wishlistState: LiveData<Resource<Boolean>> = _wishlistState

    fun loadProduct(productId: String) {
        _productState.value = Resource.Loading
        viewModelScope.launch {
            _productState.value = repository.fetchProductById(productId)
            _wishlistState.value = repository.isWishlisted(productId)
        }
    }

    fun toggleWishlist(productId: String, currentlyWishlisted: Boolean) {
        viewModelScope.launch {
            val result = if (currentlyWishlisted) {
                repository.removeFromWishlist(productId)
            } else {
                repository.addToWishlist(productId)
            }
            _wishlistState.value = when (result) {
                is Resource.Success -> Resource.Success(!currentlyWishlisted)
                is Resource.Error -> Resource.Error(result.message)
                Resource.Loading -> Resource.Loading
            }
        }
    }
}
