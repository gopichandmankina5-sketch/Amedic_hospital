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
import com.amedick.hospitalapp.databinding.ActivityDoctorVerificationBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.DoctorVerificationDocument
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
class DoctorVerificationActivity : AppCompatActivity() {

    companion object {
        const val DOC_MEDICAL_REGISTRATION = "MEDICAL_REGISTRATION"
        const val DOC_MEDICAL_DEGREE = "MEDICAL_DEGREE"
        const val DOC_GOVERNMENT_ID = "GOVERNMENT_ID"
        const val DOC_EXPERIENCE_CERTIFICATE = "EXPERIENCE_CERTIFICATE"
    }

    private lateinit var binding: ActivityDoctorVerificationBinding

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var firestoreRepository: FirestoreRepository

    private var doctorId: String = ""
    private var doctorName: String = ""
    private var pendingDocumentType: String = ""

    // Track uploaded documents by type
    private val uploadedDocs = mutableMapOf<String, DoctorVerificationDocument>()

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.w("DoctorVerification", "Could not take persistable permission", e)
            }
            processSelectedDocument(uri, pendingDocumentType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = authRepository.getCurrentUserId() ?: run {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        setupUploadButtons()
        loadExistingDocuments()
    }

    private fun setupUploadButtons() {
        binding.btnUploadMedReg.setOnClickListener {
            pendingDocumentType = DOC_MEDICAL_REGISTRATION
            pickDocument()
        }
        binding.btnUploadMedDegree.setOnClickListener {
            pendingDocumentType = DOC_MEDICAL_DEGREE
            pickDocument()
        }
        binding.btnUploadGovtId.setOnClickListener {
            pendingDocumentType = DOC_GOVERNMENT_ID
            pickDocument()
        }
        binding.btnUploadExpCert.setOnClickListener {
            pendingDocumentType = DOC_EXPERIENCE_CERTIFICATE
            pickDocument()
        }
        binding.btnSubmitVerification.setOnClickListener {
            submitVerificationRequest()
        }
    }

    private fun loadExistingDocuments() {
        lifecycleScope.launch {
            val result = firestoreRepository.getVerificationDocuments(doctorId)
            result.onSuccess { docs ->
                for (doc in docs) {
                    uploadedDocs[doc.documentType] = doc
                }
                updateDocumentStatusUI()
            }
            // Also load doctor name for notification
            val profile = firestoreRepository.getUserProfile(doctorId)
            profile.onSuccess { user -> doctorName = user.name.ifEmpty { user.email.substringBefore("@") } }
        }
    }

    private fun pickDocument() {
        val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
        documentPickerLauncher.launch(mimeTypes)
    }

    private fun processSelectedDocument(uri: Uri, documentType: String) {
        var fileName = "Document"
        var fileSize = 0L
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) fileSize = cursor.getLong(sizeIdx)
                }
            }
        } catch (e: Exception) {
            Log.e("DoctorVerification", "Error reading file metadata", e)
        }

        val maxSize = 10 * 1024 * 1024L
        if (fileSize > maxSize) {
            Toast.makeText(this, "File is too large. Maximum allowed size is 10 MB.", Toast.LENGTH_LONG).show()
            return
        }

        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val supportedTypes = listOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
        if (!supportedTypes.contains(mimeType.lowercase())) {
            Toast.makeText(this, "Unsupported file type. Please upload PDF, JPG, JPEG, or PNG.", Toast.LENGTH_LONG).show()
            return
        }

        uploadToCloudinary(uri, fileName, mimeType, fileSize, documentType)
    }

    private fun uploadToCloudinary(uri: Uri, fileName: String, mimeType: String, fileSize: Long, documentType: String) {
        setUploading(true, "Uploading ${documentTypeLabel(documentType)}...")

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
                    throw Exception("Upload failed with server error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty server response")
                val secureUrl = JSONObject(responseBody).getString("secure_url")

                val doc = DoctorVerificationDocument(
                    doctorId = doctorId,
                    documentType = documentType,
                    fileName = fileName,
                    fileUrl = secureUrl,
                    fileType = mimeType,
                    size = fileSize
                )

                // Delete previous document of the same type if exists
                uploadedDocs[documentType]?.let { existing ->
                    firestoreRepository.deleteVerificationDocument(existing.documentId)
                }

                val saveResult = firestoreRepository.saveVerificationDocument(doc)
                if (saveResult.isFailure) throw saveResult.exceptionOrNull() ?: Exception("Firestore save failed")

                // Reload saved doc (it will have the documentId now)
                val refreshResult = firestoreRepository.getVerificationDocuments(doctorId)
                refreshResult.onSuccess { docs ->
                    uploadedDocs.clear()
                    for (d in docs) uploadedDocs[d.documentType] = d
                }

                withContext(Dispatchers.Main) {
                    setUploading(false)
                    Toast.makeText(this@DoctorVerificationActivity, "${documentTypeLabel(documentType)} uploaded successfully.", Toast.LENGTH_SHORT).show()
                    updateDocumentStatusUI()
                }

            } catch (e: Exception) {
                Log.e("DoctorVerification", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    setUploading(false)
                    val msg = when {
                        e.message?.contains("10 MB") == true -> "File is larger than 10 MB."
                        e.message?.contains("internet") == true || e.message?.contains("network") == true ->
                            "Upload failed. Please check your internet connection and try again."
                        e.message?.contains("Unsupported") == true -> "Unsupported file type."
                        else -> "Upload failed. Please try again. (${e.message})"
                    }
                    Toast.makeText(this@DoctorVerificationActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateDocumentStatusUI() {
        updateSlot(DOC_MEDICAL_REGISTRATION, binding.tvMedRegStatus, binding.btnUploadMedReg)
        updateSlot(DOC_MEDICAL_DEGREE, binding.tvMedDegreeStatus, binding.btnUploadMedDegree)
        updateSlot(DOC_GOVERNMENT_ID, binding.tvGovtIdStatus, binding.btnUploadGovtId)
        updateSlot(DOC_EXPERIENCE_CERTIFICATE, binding.tvExpCertStatus, binding.btnUploadExpCert)

        // Submit button: enabled only if all 3 required docs are uploaded
        val allRequiredUploaded = uploadedDocs.containsKey(DOC_MEDICAL_REGISTRATION) &&
                uploadedDocs.containsKey(DOC_MEDICAL_DEGREE) &&
                uploadedDocs.containsKey(DOC_GOVERNMENT_ID)
        binding.btnSubmitVerification.isEnabled = allRequiredUploaded
    }

    private fun updateSlot(
        docType: String,
        statusView: android.widget.TextView,
        uploadBtn: android.widget.Button
    ) {
        val doc = uploadedDocs[docType]
        if (doc != null) {
            statusView.text = "✓ Uploaded: ${doc.fileName}"
            statusView.setTextColor(getColor(com.amedick.hospitalapp.R.color.color_success))
            uploadBtn.text = "Replace"
        } else {
            statusView.text = "Not uploaded"
            statusView.setTextColor(getColor(com.amedick.hospitalapp.R.color.color_text_secondary))
            uploadBtn.text = "Upload Document"
        }
    }

    private fun documentTypeLabel(type: String): String = when (type) {
        DOC_MEDICAL_REGISTRATION -> "Medical Registration Certificate"
        DOC_MEDICAL_DEGREE -> "Medical Degree"
        DOC_GOVERNMENT_ID -> "Government ID"
        DOC_EXPERIENCE_CERTIFICATE -> "Experience Certificate"
        else -> "Document"
    }

    private fun setUploading(uploading: Boolean, message: String = "") {
        binding.progressBar.visibility = if (uploading) View.VISIBLE else View.GONE
        binding.tvUploadStatus.visibility = if (uploading && message.isNotEmpty()) View.VISIBLE else View.GONE
        binding.tvUploadStatus.text = message
        binding.btnUploadMedReg.isEnabled = !uploading
        binding.btnUploadMedDegree.isEnabled = !uploading
        binding.btnUploadGovtId.isEnabled = !uploading
        binding.btnUploadExpCert.isEnabled = !uploading
        binding.btnSubmitVerification.isEnabled = !uploading
    }

    private fun submitVerificationRequest() {
        val required = listOf(DOC_MEDICAL_REGISTRATION, DOC_MEDICAL_DEGREE, DOC_GOVERNMENT_ID)
        if (!uploadedDocs.keys.containsAll(required)) {
            Toast.makeText(this, "Please upload all required documents before submitting.", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnSubmitVerification.isEnabled = false
        binding.btnSubmitVerification.text = "Submitting..."
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = firestoreRepository.submitVerificationRequest(doctorId, doctorName)
            binding.progressBar.visibility = View.GONE
            result.onSuccess {
                Toast.makeText(
                    this@DoctorVerificationActivity,
                    "Verification request submitted. The hospital administrator will review your documents.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }.onFailure { e ->
                binding.btnSubmitVerification.isEnabled = true
                binding.btnSubmitVerification.text = "Submit for Verification"
                Toast.makeText(this@DoctorVerificationActivity, "Submission failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
