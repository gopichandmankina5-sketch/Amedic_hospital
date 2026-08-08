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
import com.amedick.hospitalapp.databinding.ActivityDoctorProfileBinding
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
class DoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorProfileBinding
    private var doctorId: String = ""
    private var currentDoctor: User? = null

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var firestoreRepository: FirestoreRepository

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.w("DoctorProfile", "Could not take persistable permission", e)
            }
            uploadProfilePhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = authRepository.getCurrentUserId() ?: run {
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

        loadDoctorProfile()
    }

    private fun loadDoctorProfile() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            firestoreRepository.getUserProfile(doctorId).onSuccess { user ->
                currentDoctor = user
                populateProfile(user)
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DoctorProfileActivity, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateProfile(user: User) {
        binding.tvDoctorName.text = "Dr. ${user.name.ifEmpty { "Doctor" }}"
        binding.tvSpecialization.text = user.specialization.ifEmpty { "Specialization not set" }
        binding.tvEmail.text = user.email.ifEmpty { "—" }
        binding.tvPhone.text = user.phone.ifEmpty { "Not set" }
        binding.tvQualification.text = user.qualification.ifEmpty { "Not set" }
        binding.tvExperience.text = if (user.experience > 0) "${user.experience} Years" else "Not set"
        binding.tvRegNumber.text = user.medicalRegistrationNumber.ifEmpty { "Not set" }

        // Verification Status
        when (user.verificationStatus) {
            "VERIFIED" -> {
                binding.tvVerificationStatus.text = "✓ VERIFIED"
                binding.tvVerificationStatus.setTextColor(getColor(com.amedick.hospitalapp.R.color.color_success))
            }
            "REJECTED" -> {
                binding.tvVerificationStatus.text = "✕ REJECTED"
                binding.tvVerificationStatus.setTextColor(getColor(com.amedick.hospitalapp.R.color.color_error))
            }
            else -> {
                binding.tvVerificationStatus.text = "⏳ PENDING"
                binding.tvVerificationStatus.setTextColor(getColor(com.amedick.hospitalapp.R.color.status_pending))
            }
        }

        if (user.profileImage.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImage)
                .placeholder(com.amedick.hospitalapp.R.drawable.ic_doctor)
                .circleCrop()
                .into(binding.profileImage)
            binding.profileImage.setPadding(0, 0, 0, 0)
        } else {
            binding.profileImage.setImageResource(com.amedick.hospitalapp.R.drawable.ic_doctor)
            binding.profileImage.setPadding(16, 16, 16, 16)
        }

        binding.progressBar.visibility = View.GONE
    }

    private fun showEditProfileDialog() {
        val user = currentDoctor ?: return

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

        val specLayout = TextInputLayout(this).apply {
            hint = "Specialization"
            setPadding(0, 8, 0, 0)
        }
        val specField = TextInputEditText(this).apply { setText(user.specialization) }
        specLayout.addView(specField)

        val qualLayout = TextInputLayout(this).apply {
            hint = "Qualification"
            setPadding(0, 8, 0, 0)
        }
        val qualField = TextInputEditText(this).apply { setText(user.qualification) }
        qualLayout.addView(qualField)

        val expLayout = TextInputLayout(this).apply {
            hint = "Experience (years)"
            setPadding(0, 8, 0, 0)
        }
        val expField = TextInputEditText(this).apply {
            setText(user.experience.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        expLayout.addView(expField)

        val regLayout = TextInputLayout(this).apply {
            hint = "Medical Registration Number"
            setPadding(0, 8, 0, 0)
        }
        val regField = TextInputEditText(this).apply { setText(user.medicalRegistrationNumber) }
        regLayout.addView(regField)

        container.addView(nameLayout)
        container.addView(phoneLayout)
        container.addView(specLayout)
        container.addView(qualLayout)
        container.addView(expLayout)
        container.addView(regLayout)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameField.text.toString().trim()
                val newPhone = phoneField.text.toString().trim()
                val newSpec = specField.text.toString().trim()
                val newQual = qualField.text.toString().trim()
                val newExp = expField.text.toString().toIntOrNull() ?: 0
                val newReg = regField.text.toString().trim()

                if (newName.isNotEmpty()) {
                    // Update only allowed fields, role and verificationStatus must NEVER change here
                    updateProfileData(
                        user.copy(
                            name = newName,
                            phone = newPhone,
                            specialization = newSpec,
                            qualification = newQual,
                            experience = newExp,
                            medicalRegistrationNumber = newReg
                        )
                    )
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
            
            // Because doctor profile has extra fields, we need to update those in Users too
            val extraFields = mapOf(
                "specialization" to updatedUser.specialization,
                "qualification" to updatedUser.qualification,
                "experience" to updatedUser.experience,
                "medicalRegistrationNumber" to updatedUser.medicalRegistrationNumber
            )
            val resultExtra = runCatching {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Users").document(doctorId).update(extraFields)
            }

            binding.progressBar.visibility = View.GONE
            if (result.isSuccess && resultExtra.isSuccess) {
                Toast.makeText(this@DoctorProfileActivity, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                loadDoctorProfile()
            } else {
                Toast.makeText(this@DoctorProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private fun uploadProfilePhoto(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        
        var fileName = "doctor_profile.jpg"
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

                val updateResult = firestoreRepository.updateUserProfileImage(doctorId, secureUrl)
                if (updateResult.isFailure) {
                    throw updateResult.exceptionOrNull() ?: Exception("Firestore profile update failed")
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@DoctorProfileActivity, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                    loadDoctorProfile()
                }
            } catch (e: Exception) {
                Log.e("DoctorProfile", "Failed to upload photo", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@DoctorProfileActivity, "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
