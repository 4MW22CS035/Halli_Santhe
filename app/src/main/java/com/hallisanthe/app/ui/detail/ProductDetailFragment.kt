package com.hallisanthe.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.databinding.FragmentProductDetailBinding
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.DetailViewModel

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private var currentProductId: String = ""
    private var isWishlisted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentProductId = arguments?.getString(ARG_PRODUCT_ID).orEmpty()
        if (currentProductId.isNotBlank()) viewModel.loadProduct(currentProductId)
        observeState()
        setupActions()
    }

    private fun observeState() {
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val item = state.data
                    Glide.with(binding.imageProduct)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.bg_card_placeholder)
                        .error(R.drawable.bg_card_placeholder)
                        .centerCrop()
                        .into(binding.imageProduct)
                    binding.textName.text = item.name
                    binding.textPrice.text = getString(R.string.price_format, item.price)
                    binding.textCategory.text = item.category
                    binding.textSeller.text = item.sellerName
                    binding.textDescription.text = item.description
                    binding.textStock.text =
                        if (item.inStock) getString(R.string.in_stock) else getString(R.string.out_of_stock)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textDescription.text = state.message
                }
            }
        }
        viewModel.wishlistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    isWishlisted = state.data
                    binding.buttonWishlist.text = if (isWishlisted) {
                        getString(R.string.remove_from_wishlist)
                    } else {
                        getString(R.string.add_to_wishlist)
                    }
                }
                is Resource.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                Resource.Loading -> Unit
            }
        }
    }

    private fun setupActions() = with(binding) {
        buttonCheckStock.setOnClickListener {
            Snackbar.make(root, getString(R.string.stock_checked_msg), Snackbar.LENGTH_SHORT).show()
        }
        buttonContactSeller.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.contact_seller))
                .setMessage(getString(R.string.contact_seller_msg))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
        buttonWishlist.setOnClickListener {
            if (currentProductId.isNotBlank()) {
                viewModel.toggleWishlist(currentProductId, isWishlisted)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_PRODUCT_ID = "productId"
    }
}
