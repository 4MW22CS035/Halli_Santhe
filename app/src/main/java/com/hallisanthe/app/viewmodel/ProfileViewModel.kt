package com.hallisanthe.app.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.model.UserProfile
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {
    private val _profileState = MutableLiveData<Resource<UserProfile>>()
    val profileState: LiveData<Resource<UserProfile>> = _profileState
    private val _countState = MutableLiveData<Resource<Int>>()
    val countState: LiveData<Resource<Int>> = _countState
    private val _imageUpdateState = MutableLiveData<Resource<String>>()
    val imageUpdateState: LiveData<Resource<String>> = _imageUpdateState

    fun loadProfile() {
        _profileState.value = Resource.Loading
        _countState.value = Resource.Loading
        viewModelScope.launch {
            _profileState.value = repository.fetchUserProfile()
            _countState.value = repository.fetchUploadedProductsCount()
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _imageUpdateState.value = Resource.Loading
            _imageUpdateState.value = repository.updateProfileImage(imageUri)
            loadProfile()
        }
    }

    fun logout() {
        repository.logout()
    }
}
