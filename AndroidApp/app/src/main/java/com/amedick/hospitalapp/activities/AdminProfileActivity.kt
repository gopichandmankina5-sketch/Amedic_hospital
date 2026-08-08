package com.amedick.hospitalapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.config.CloudinaryConfig
import com.amedick.hospitalapp.databinding.ActivityAdminProfileBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.User
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
class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProfileBinding
    private var adminId: String = ""
    private var currentAdmin: User? = null

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var firestoreRepository: FirestoreRepository

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.w("AdminProfile", "Could not take persistable permission", e)
            }
            uploadProfilePhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = authRepository.getCurrentUserId() ?: run {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnEditPhoto.setOnClickListener {
            pickImage()
        }

        loadAdminProfile()
    }

    private fun loadAdminProfile() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            firestoreRepository.getUserProfile(adminId).onSuccess { user ->
                currentAdmin = user
                populateProfile(user)
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminProfileActivity, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateProfile(user: User) {
        binding.tvAdminName.text = user.name.ifEmpty { "Administrator" }
        binding.tvEmail.text = user.email.ifEmpty { "—" }
        binding.tvPhone.text = user.phone.ifEmpty { "Not set" }
        
        // Account Status
        binding.tvAccountStatus.text = "● Active"
        binding.tvAccountStatus.setTextColor(getColor(com.amedick.hospitalapp.R.color.color_success))

        if (user.profileImage.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImage)
                .placeholder(com.amedick.hospitalapp.R.drawable.ic_user)
                .circleCrop()
                .into(binding.profileImage)
            // Remove icon padding if image loaded
            binding.profileImage.setPadding(0, 0, 0, 0)
        } else {
            binding.profileImage.setImageResource(com.amedick.hospitalapp.R.drawable.ic_user)
            binding.profileImage.setPadding(16, 16, 16, 16)
        }

        binding.progressBar.visibility = View.GONE
    }

    private fun showEditProfileDialog() {
        val user = currentAdmin ?: return

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val nameLayout = TextInputLayout(this).apply { hint = "Full Name" }
        val nameField = TextInputEditText(this).apply { setText(user.name) }
        nameLayout.addView(nameField)

        val phoneLayout = TextInputLayout(this).apply {
            hint = "Phone Number"
            setPadding(0, 8, 0, 0)
        }
        val phoneField = TextInputEditText(this).apply {
            setText(user.phone)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        phoneLayout.addView(phoneField)

        container.addView(nameLayout)
        container.addView(phoneLayout)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameField.text.toString().trim()
                val newPhone = phoneField.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateProfileData(user.copy(name = newName, phone = newPhone))
                } else {
                    Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfileData(updatedUser: User) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = firestoreRepository.updateUserProfile(updatedUser)
            binding.progressBar.visibility = View.GONE
            result.onSuccess {
                Toast.makeText(this@AdminProfileActivity, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                loadAdminProfile()
            }.onFailure { e ->
                Toast.makeText(this@AdminProfileActivity, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private fun uploadProfilePhoto(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        
        var fileName = "profile_image.jpg"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                }
            }
        } catch (_: Exception) {}

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
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

                val updateResult = firestoreRepository.updateUserProfileImage(adminId, secureUrl)
                if (updateResult.isFailure) {
                    throw updateResult.exceptionOrNull() ?: Exception("Firestore profile update failed")
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminProfileActivity, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                    loadAdminProfile()
                }
            } catch (e: Exception) {
                Log.e("AdminProfile", "Failed to upload photo", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminProfileActivity, "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
