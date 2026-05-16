package com.hallisanthe.app.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hallisanthe.app.R
import com.hallisanthe.app.databinding.FragmentSplashBinding
import com.hallisanthe.app.utils.FirebaseAuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logoImage.alpha = 0f
        binding.titleText.alpha = 0f
        binding.logoImage.animate().alpha(1f).setDuration(900).start()
        binding.titleText.animate().alpha(1f).setDuration(1400).start()

        lifecycleScope.launch {
            delay(2000)
            if (!isAdded) return@launch
            val destination = if (FirebaseAuthManager.isLoggedIn()) {
                R.id.action_splashFragment_to_homeFragment
            } else {
                R.id.action_splashFragment_to_loginFragment
            }
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.splashFragment) {
                navController.navigate(destination)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
