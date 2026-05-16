package com.hallisanthe.app.ui.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.adapters.ProductAdapter
import com.hallisanthe.app.databinding.FragmentWishlistBinding
import com.hallisanthe.app.ui.detail.ProductDetailFragment
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.WishlistViewModel

class WishlistFragment : Fragment() {
    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WishlistViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProductAdapter { product ->
            val bundle = Bundle().apply {
                putString(ProductDetailFragment.ARG_PRODUCT_ID, product.id)
            }
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }
        binding.recyclerWishlist.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerWishlist.adapter = adapter
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadWishlist()
    }

    private fun observeState() {
        viewModel.wishlistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.emptyText.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
