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
import com.charbeljoe.weathermood.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            viewModel.register(
                binding.usernameInput.text?.toString() ?: "",
                binding.emailInput.text?.toString() ?: "",
                binding.passwordInput.text?.toString() ?: "",
                binding.confirmPasswordInput.text?.toString() ?: ""
            )
        }

        binding.goToLoginButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                findNavController().navigate(
                    R.id.action_register_to_home,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_login, true)
                        .build()
                )
            } else {
                binding.errorText.text = error
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
