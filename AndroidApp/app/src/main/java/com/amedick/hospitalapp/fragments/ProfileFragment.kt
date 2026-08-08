package com.amedick.hospitalapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.activities.LoginActivity
import com.amedick.hospitalapp.databinding.FragmentProfileBinding
import com.amedick.hospitalapp.viewmodel.ProfileState
import com.amedick.hospitalapp.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

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

        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    viewModel.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        observeViewModel()
    }

    private fun showEditProfileDialog() {
        val currentState = viewModel.profileState.value
        val user = (currentState as? ProfileState.Loaded)?.user ?: return

        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val nameLayout = TextInputLayout(requireContext()).apply { hint = "Full Name" }
        val nameField = TextInputEditText(requireContext()).apply { setText(user.name) }
        nameLayout.addView(nameField)

        val phoneLayout = TextInputLayout(requireContext()).apply {
            hint = "Phone Number"
            setPadding(0, 8, 0, 0)
        }
        val phoneField = TextInputEditText(requireContext()).apply {
            setText(user.phone)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        phoneLayout.addView(phoneField)

        container.addView(nameLayout)
        container.addView(phoneLayout)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameField.text.toString().trim()
                val newPhone = phoneField.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateProfile(user.copy(name = newName, phone = newPhone))
                    Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show()
                    viewModel.loadProfile()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val newPassLayout = TextInputLayout(requireContext()).apply {
            hint = "New Password"
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
        val newPassField = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or android.text.InputType.TYPE_CLASS_TEXT
        }
        newPassLayout.addView(newPassField)

        container.addView(newPassLayout)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setMessage("Enter your new password (at least 6 characters).")
            .setView(container)
            .setPositiveButton("Change") { _, _ ->
                val newPass = newPassField.text.toString()
                if (newPass.length >= 6) {
                    viewModel.updatePassword(newPass)
                    Toast.makeText(requireContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    when (state) {
                        is ProfileState.Idle -> binding.progressBar.visibility = View.GONE
                        is ProfileState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is ProfileState.Loaded -> {
                            binding.progressBar.visibility = View.GONE
                            binding.nameText.text = state.user.name.ifEmpty { "Your Name" }
                            binding.emailText.text = state.user.email
                            binding.phoneText.text = state.user.phone.ifEmpty { "Not set" }
                        }
                        is ProfileState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            // Silently handle — user may not have a Firestore profile yet
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
