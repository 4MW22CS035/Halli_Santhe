package com.hallisanthe.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.adapters.ProductAdapter
import com.hallisanthe.app.databinding.FragmentHomeBinding
import com.hallisanthe.app.ui.detail.ProductDetailFragment
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var adapter: ProductAdapter
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecycler()
        setupSearch()
        setupCategories()
        setupActions()
        observeState()
        viewModel.loadProducts()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.app_name)
    }

    private fun setupRecycler() {
        adapter = ProductAdapter { product ->
            val bundle = Bundle().apply {
                putString(ProductDetailFragment.ARG_PRODUCT_ID, product.id)
            }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment, bundle)
        }
        binding.recyclerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                viewModel.search(currentQuery)
                return true
            }
        })
    }

    private fun setupCategories() {
        val categories = listOf("All", "Textiles", "Pottery", "Food", "Decor", "Jewelry")
        categories.forEachIndexed { index, title ->
            val chip = Chip(requireContext()).apply {
                text = title
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
            }
            binding.chipGroupCategories.addView(chip)
            if (index == 0) chip.isChecked = true
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                viewModel.setCategory(selectedChip.text.toString(), currentQuery)
            }
        }
    }

    private fun setupActions() {
        binding.fabUpload.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_uploadProductFragment)
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadProducts() }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                    binding.progressBar.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }
                is Resource.Success -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    val products = state.data
                    adapter.submitList(products)
                    binding.recyclerProducts.visibility =
                        if (products.isEmpty()) View.GONE else View.VISIBLE
                    binding.layoutEmpty.root.visibility =
                        if (products.isEmpty()) View.VISIBLE else View.GONE
                    if (products.isEmpty()) {
                        binding.layoutEmpty.textEmptyMessage.text =
                            if (currentQuery.isNotBlank()) getString(R.string.no_products_search)
                            else getString(R.string.no_products_title)
                    }
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.layoutEmpty.root.visibility = View.VISIBLE
                    binding.recyclerProducts.visibility = View.GONE
                    binding.layoutEmpty.textEmptyMessage.text = state.message
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
