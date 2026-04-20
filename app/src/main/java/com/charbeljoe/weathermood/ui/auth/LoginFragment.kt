package com.charbeljoe.weathermood.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.charbeljoe.weathermood.R
import com.charbeljoe.weathermood.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isSwitchingAccount = findNavController().previousBackStackEntry != null

        // ALWAYS force manual logout and sign-in when the app is opened fresh
        if (!isSwitchingAccount) {
            viewModel.logout()
        }
        
        setupUi(isSwitchingAccount)
    }

    private fun setupUi(isSwitchingAccount: Boolean) {
        if (_binding == null) return

        if (isSwitchingAccount) {
            binding.backButton.visibility = View.VISIBLE
            binding.backButton.setOnClickListener { findNavController().navigateUp() }
        } else {
            binding.backButton.visibility = View.GONE
        }

        binding.loginButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            viewModel.login(
                binding.usernameInput.text?.toString() ?: "",
                binding.passwordInput.text?.toString() ?: ""
            )
        }

        binding.forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        binding.goToRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                navigateToHome()
            } else {
                binding.errorText.text = error
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            R.id.navigation_home,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
