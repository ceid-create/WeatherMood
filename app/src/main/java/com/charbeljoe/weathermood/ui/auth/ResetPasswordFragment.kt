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
import com.charbeljoe.weathermood.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString("email") ?: ""

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.resetButton.setOnClickListener {
            binding.errorText.visibility = View.GONE
            binding.resetButton.isEnabled = false
            viewModel.verifyAndResetPassword(
                email,
                binding.codeInput.text?.toString() ?: "",
                binding.newPasswordInput.text?.toString() ?: "",
                binding.confirmPasswordInput.text?.toString() ?: ""
            )
        }

        viewModel.verifyCodeResult.observe(viewLifecycleOwner) { error ->
            binding.resetButton.isEnabled = true
            if (error == null) {
                // Navigate back to login, clearing the forgot-password back stack
                findNavController().navigate(
                    R.id.navigation_login,
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
