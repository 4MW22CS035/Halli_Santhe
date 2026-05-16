package com.hallisanthe.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {
    private val _wishlistState = MutableLiveData<Resource<List<Product>>>()
    val wishlistState: LiveData<Resource<List<Product>>> = _wishlistState

    fun loadWishlist() {
        _wishlistState.value = Resource.Loading
        viewModelScope.launch {
            _wishlistState.value = repository.fetchWishlistProducts()
        }
    }
}
