package com.hallisanthe.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.databinding.FragmentProfileBinding
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.updateProfileImage(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
        }
        binding.imageProfile.setOnClickListener { pickImage.launch("image/*") }
        observeState()
        viewModel.loadProfile()
    }

    private fun observeState() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    binding.textName.text = state.data.name
                    binding.textEmail.text = state.data.email
                    binding.textRole.text = state.data.role
                    Glide.with(binding.imageProfile)
                        .load(state.data.profileImageUrl)
                        .placeholder(R.drawable.ic_market_logo)
                        .into(binding.imageProfile)
                }
                is Resource.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                Resource.Loading -> Unit
            }
        }
        viewModel.countState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                binding.textUploads.text = getString(R.string.uploaded_products_count, state.data)
            }
        }
        viewModel.imageUpdateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> Snackbar.make(binding.root, getString(R.string.profile_image_updated), Snackbar.LENGTH_SHORT).show()
                is Resource.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                Resource.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
