package com.charbeljoe.weathermood.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.charbeljoe.weathermood.R
import com.charbeljoe.weathermood.databinding.FragmentSettingsBinding
import com.charbeljoe.weathermood.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = authViewModel.getUsername()
        binding.usernameText.text = username
        binding.avatarText.text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        binding.changePasswordToggle.setOnClickListener {
            val isVisible = binding.changePasswordForm.visibility == View.VISIBLE
            binding.changePasswordForm.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.changePasswordError.visibility = View.GONE
        }

        binding.cancelPasswordButton.setOnClickListener {
            binding.changePasswordForm.visibility = View.GONE
            binding.changePasswordError.visibility = View.GONE
            binding.currentPasswordInput.text?.clear()
            binding.newPasswordInput.text?.clear()
            binding.confirmNewPasswordInput.text?.clear()
        }

        binding.updatePasswordButton.setOnClickListener {
            binding.changePasswordError.visibility = View.GONE
            authViewModel.changePassword(
                binding.currentPasswordInput.text?.toString() ?: "",
                binding.newPasswordInput.text?.toString() ?: "",
                binding.confirmNewPasswordInput.text?.toString() ?: ""
            )
        }

        authViewModel.changePasswordResult.observe(viewLifecycleOwner) { error ->
            if (error == null) {
                binding.changePasswordForm.visibility = View.GONE
                binding.currentPasswordInput.text?.clear()
                binding.newPasswordInput.text?.clear()
                binding.confirmNewPasswordInput.text?.clear()
                Snackbar.make(binding.root, "Password updated successfully!", Snackbar.LENGTH_SHORT).show()
            } else {
                binding.changePasswordError.text = error
                binding.changePasswordError.visibility = View.VISIBLE
            }
        }

        binding.switchAccountButton.setOnClickListener {
            // Do NOT logout yet — only log out if the user actually signs in with a new account.
            // Keeping the session alive lets Settings restore correctly if the user presses Back.
            findNavController().navigate(R.id.navigation_login)
        }

        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
