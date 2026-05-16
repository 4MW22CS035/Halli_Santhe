package com.hallisanthe.app.ui.upload

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.databinding.FragmentUploadProductBinding
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.UploadViewModel

class UploadProductFragment : Fragment() {

    private var _binding: FragmentUploadProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val safeBinding = _binding ?: return@registerForActivityResult
            selectedImageUri = uri
            Glide.with(safeBinding.imagePreview)
                .load(uri)
                .placeholder(R.drawable.bg_card_placeholder)
                .centerCrop()
                .into(safeBinding.imagePreview)
        }
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchPicker()
        else Snackbar.make(binding.root, getString(R.string.permission_required), Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryDropdown()
        setupActions()
        observeUpload()
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.product_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupActions() = with(binding) {
        buttonPickImage.setOnClickListener {
            checkAndPickImage()
        }
        buttonUpload.setOnClickListener { validateAndUpload() }
    }

    private fun checkAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) launchPicker() else requestPermission.launch(permission)
    }

    private fun launchPicker() {
        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun validateAndUpload() {
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val priceRaw = binding.inputPrice.text?.toString()?.trim().orEmpty()
        val category = binding.spinnerCategory.text?.toString()?.trim().orEmpty()
        val description = binding.inputDescription.text?.toString()?.trim().orEmpty()
        val imageUri = selectedImageUri

        if (name.isBlank() || priceRaw.isBlank() || category.isBlank() || description.isBlank()) {
            Snackbar.make(binding.root, getString(R.string.fill_all_fields), Snackbar.LENGTH_SHORT).show()
            return
        }
        if (imageUri == null) {
            Snackbar.make(binding.root, getString(R.string.select_image_msg), Snackbar.LENGTH_SHORT).show()
            return
        }

        val price = priceRaw.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            Snackbar.make(binding.root, getString(R.string.invalid_price_msg), Snackbar.LENGTH_SHORT).show()
            return
        }

        val product = Product(
            name = name,
            price = price,
            category = category,
            description = description,
            sellerName = "Local Artisan",
            inStock = true
        )
        viewModel.uploadProduct(requireContext(), product, imageUri)
    }

    private fun observeUpload() {
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textUploadProgress.visibility = View.VISIBLE
                    binding.progressBar.progress = 0
                    binding.buttonUpload.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textUploadProgress.visibility = View.GONE
                    binding.buttonUpload.isEnabled = true
                    Snackbar.make(binding.root, getString(R.string.upload_success), Snackbar.LENGTH_LONG).show()
                    clearForm()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textUploadProgress.visibility = View.GONE
                    binding.buttonUpload.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        viewModel.uploadProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.textUploadProgress.text = getString(R.string.upload_progress_format, progress)
        }
    }

    private fun clearForm() = with(binding) {
        inputName.text?.clear()
        inputPrice.text?.clear()
        inputDescription.text?.clear()
        spinnerCategory.text?.clear()
        selectedImageUri = null
        imagePreview.setImageResource(R.drawable.bg_card_placeholder)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
