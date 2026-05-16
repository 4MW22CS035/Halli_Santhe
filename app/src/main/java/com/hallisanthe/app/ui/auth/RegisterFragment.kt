package com.hallisanthe.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.util.PatternsCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hallisanthe.app.R
import com.hallisanthe.app.databinding.FragmentRegisterBinding
import com.hallisanthe.app.utils.Resource
import com.hallisanthe.app.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf("Buyer", "Artisan")
        )
        binding.inputRole.setAdapter(roleAdapter)
        binding.buttonRegister.setOnClickListener { register() }
        binding.textLogin.setOnClickListener {
            findNavController().navigateUp()
        }
        observeState()
    }

    private fun register() {
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val role = binding.inputRole.text?.toString()?.trim()?.ifEmpty { "Buyer" } ?: "Buyer"

        when {
            name.isBlank() -> {
                Snackbar.make(binding.root, "Please enter your name", Snackbar.LENGTH_SHORT).show()
                return
            }
            name.length < 3 -> {
                Snackbar.make(binding.root, "Name must be at least 3 characters", Snackbar.LENGTH_SHORT).show()
                return
            }
            email.isBlank() -> {
                Snackbar.make(binding.root, "Please enter your email", Snackbar.LENGTH_SHORT).show()
                return
            }
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> {
                Snackbar.make(binding.root, "Please enter a valid email", Snackbar.LENGTH_SHORT).show()
                return
            }
            password.isBlank() -> {
                Snackbar.make(binding.root, "Please enter your password", Snackbar.LENGTH_SHORT).show()
                return
            }
            password.length < 6 -> {
                Snackbar.make(binding.root, "Password must be at least 6 characters", Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        if (binding.buttonRegister.isEnabled.not()) {
            return
        }
        viewModel.register(name, email, password, role)
    }

    private fun observeState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state is Resource.Loading) View.VISIBLE else View.GONE
            binding.buttonRegister.isEnabled = state !is Resource.Loading
            when (state) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, getString(R.string.register_success), Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
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
