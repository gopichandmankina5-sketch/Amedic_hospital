package com.amedick.hospitalapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.activities.LoginActivity
import com.amedick.hospitalapp.config.CloudinaryConfig
import com.amedick.hospitalapp.databinding.FragmentProfileBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.viewmodel.ProfileState
import com.amedick.hospitalapp.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("ProfileFragment", "Could not take persistable permission", e)
            }
            uploadProfilePhoto(uri)
        }
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

        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.logoutButton.setOnClickListener {
            com.amedick.hospitalapp.utils.LogoutHelper.showLogoutConfirmation(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        }

        binding.profileImageCard.setOnClickListener {
            pickImage()
        }
        binding.profileImage.setOnClickListener {
            pickImage()
        }

        observeViewModel()
    }

    private fun pickImage() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val uid = viewModel.getCurrentUserId() ?: return
        binding.progressBar.visibility = View.VISIBLE

        var fileName = "profile_image.jpg"
        try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                }
            }
        } catch (_: Exception) {}

        val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Failed to read file bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart("file", fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/auto/upload")
                    .post(requestBody)
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Cloudinary server error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                val secureUrl = JSONObject(responseBody).getString("secure_url")

                val updateResult = firestoreRepository.updateUserProfileImage(uid, secureUrl)
                if (updateResult.isFailure) {
                    throw updateResult.exceptionOrNull() ?: Exception("Firestore profile update failed")
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.loadProfile()
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Failed to upload photo", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

                            // Load profile image using Glide
                            if (state.user.profileImage.isNotEmpty()) {
                                Glide.with(this@ProfileFragment)
                                    .load(state.user.profileImage)
                                    .placeholder(com.amedick.hospitalapp.R.drawable.ic_user)
                                    .circleCrop()
                                    .into(binding.profileImage)
                                binding.profileImage.setPadding(0, 0, 0, 0)
                            } else {
                                binding.profileImage.setImageResource(com.amedick.hospitalapp.R.drawable.ic_user)
                                binding.profileImage.setPadding(16, 16, 16, 16)
                            }
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
