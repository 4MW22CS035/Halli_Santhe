package com.hallisanthe.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.data.repository.FirebaseMarketplaceRepository
import com.hallisanthe.app.utils.ImageCompressor
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.launch

class UploadViewModel(
    private val repository: FirebaseMarketplaceRepository = FirebaseMarketplaceRepository()
) : ViewModel() {

    private val _uploadState = MutableLiveData<Resource<Unit>>()
    val uploadState: LiveData<Resource<Unit>> = _uploadState
    private val _uploadProgress = MutableLiveData(0)
    val uploadProgress: LiveData<Int> = _uploadProgress

    fun uploadProduct(context: Context, product: Product, imageUri: Uri) {
        _uploadState.value = Resource.Loading
        viewModelScope.launch {
            val compressedUri = ImageCompressor.compress(context, imageUri)
            if (compressedUri == null) {
                _uploadState.value = Resource.Error("Image compression failed")
                return@launch
            }
            _uploadState.value = repository.uploadProduct(product, compressedUri) { progress ->
                _uploadProgress.postValue(progress)
            }
        }
    }
}
