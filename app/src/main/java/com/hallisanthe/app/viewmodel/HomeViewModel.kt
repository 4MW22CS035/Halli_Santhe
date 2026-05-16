package com.hallisanthe.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {

    private val _state = MutableLiveData<Resource<List<Product>>>()
    val state: LiveData<Resource<List<Product>>> = _state

    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String = CATEGORY_ALL

    fun loadProducts() {
        _state.value = Resource.Loading
        viewModelScope.launch {
            when (val result = repository.fetchProducts()) {
                is Resource.Success -> {
                    allProducts = result.data
                    _state.value = Resource.Success(filterProducts("", selectedCategory))
                }
                is Resource.Error -> _state.value = result
                Resource.Loading -> Unit
            }
        }
    }

    fun search(query: String) {
        _state.value = Resource.Success(filterProducts(query, selectedCategory))
    }

    fun setCategory(category: String, query: String) {
        selectedCategory = category
        _state.value = Resource.Success(filterProducts(query, selectedCategory))
    }

    private fun filterProducts(query: String, category: String): List<Product> {
        return allProducts.filter { product ->
            val categoryMatch = category == CATEGORY_ALL || product.category.equals(category, true)
            val queryMatch = product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            categoryMatch && queryMatch
        }
    }

    companion object {
        const val CATEGORY_ALL = "All"
    }
}
