package com.charbeljoe.weathermood.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.charbeljoe.weathermood.R
import com.charbeljoe.weathermood.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.sendCodeButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            binding.sendCodeButton.isEnabled = false
            viewModel.sendResetCode(binding.emailInput.text?.toString() ?: "")
        }

        viewModel.sendResetCodeResult.observe(viewLifecycleOwner) { error ->
            binding.sendCodeButton.isEnabled = true
            if (error == null) {
                val email = binding.emailInput.text?.toString()?.trim()?.lowercase() ?: ""
                findNavController().navigate(
                    R.id.action_forgot_password_to_reset_password,
                    bundleOf("email" to email)
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
