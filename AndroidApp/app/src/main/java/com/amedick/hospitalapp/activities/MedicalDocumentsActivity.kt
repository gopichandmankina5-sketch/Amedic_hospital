package com.amedick.hospitalapp.activities

import android.app.Activity
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.MedicalDocumentAdapter
import com.amedick.hospitalapp.config.CloudinaryConfig
import com.amedick.hospitalapp.databinding.ActivityMedicalDocumentsBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.MedicalDocument
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
class MedicalDocumentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalDocumentsBinding
    private lateinit var adapter: MedicalDocumentAdapter

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var patientId: String = ""
    private var isUploading = false

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("MedicalDocuments", "Could not take persistable permission", e)
            }
            processSelectedDocument(uri)
        } else {
            Toast.makeText(this, "Please select a file.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        patientId = intent.getStringExtra("EXTRA_PATIENT_ID") ?: authRepository.getCurrentUserId() ?: ""
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Please log in again to access documents.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // If doctor is viewing, disable upload
        if (patientId != authRepository.getCurrentUserId()) {
            binding.btnUploadDocument.visibility = View.GONE
        } else {
            binding.btnUploadDocument.setOnClickListener {
                if (!isUploading) {
                    pickDocument()
                }
            }
        }

        setupRecyclerView()
        loadDocuments()
    }

    private fun setupRecyclerView() {
        adapter = MedicalDocumentAdapter(
            emptyList(),
            onViewClick = { document -> viewDocument(document) },
            onDeleteClick = { document -> deleteDocument(document) }
        )
        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        binding.rvDocuments.adapter = adapter
    }

    private fun loadDocuments() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvDocuments.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

        lifecycleScope.launch {
            val result = firestoreRepository.getMedicalDocuments(patientId)
            binding.progressBar.visibility = View.GONE

            result.onSuccess { documents ->
                if (documents.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.rvDocuments.visibility = View.VISIBLE
                    adapter.updateData(documents)
                }
            }.onFailure { e ->
                Log.e("MedicalDocuments", "Unable to load documents", e)
                binding.emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@MedicalDocumentsActivity, "Unable to load documents.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickDocument() {
        val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
        documentPickerLauncher.launch(mimeTypes)
    }

    private fun processSelectedDocument(uri: Uri) {
        var fileName = "Medical Document"
        var fileSize = 0L

        // Query ContentResolver for name and size
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MedicalDocuments", "Error querying document metadata", e)
        }

        // Validate File Size (Limit: 10 MB)
        val maxSizeBytes = 10 * 1024 * 1024
        if (fileSize > maxSizeBytes) {
            Toast.makeText(this, "File is too large. Maximum size is 10 MB.", Toast.LENGTH_LONG).show()
            return
        }

        // Extract MIME type
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val supportedTypes = listOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
        
        // Convert mimeType to simpler UI type
        val uiType = when {
            mimeType.contains("pdf", ignoreCase = true) -> "PDF Document"
            mimeType.contains("image", ignoreCase = true) -> "Image"
            else -> "Document"
        }

        if (uiType == "Document" && !supportedTypes.contains(mimeType.lowercase())) {
            Toast.makeText(this, "Unsupported file type.", Toast.LENGTH_SHORT).show()
            return
        }

        uploadDocumentToCloudinary(uri, fileName, uiType, fileSize, mimeType)
    }

    private fun uploadDocumentToCloudinary(uri: Uri, name: String, type: String, size: Long, mimeType: String) {
        isUploading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUploadDocument.isEnabled = false
        binding.btnUploadDocument.text = "Uploading..."
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Failed to read file bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart(
                        "file",
                        name,
                        bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/auto/upload")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Cloudinary API error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                val jsonObject = JSONObject(responseBody)
                val secureUrl = jsonObject.getString("secure_url")

                val document = MedicalDocument(
                    patientId = patientId,
                    name = name,
                    type = type,
                    url = secureUrl,
                    size = size,
                    storageProvider = "cloudinary",
                    storagePath = "" // Cloudinary uses URL directly, we don't need a custom storage path here
                )

                val firestoreResult = firestoreRepository.saveMedicalDocumentMetadata(document)
                if (firestoreResult.isFailure) {
                    throw firestoreResult.exceptionOrNull() ?: Exception("Firestore metadata save failed")
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUploadDocument.isEnabled = true
                    binding.btnUploadDocument.text = "+ Upload Document"
                    isUploading = false
                    Toast.makeText(this@MedicalDocumentsActivity, "Document uploaded successfully", Toast.LENGTH_SHORT).show()
                    loadDocuments()
                }

            } catch (e: Exception) {
                Log.e("MedicalDocuments", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUploadDocument.isEnabled = true
                    binding.btnUploadDocument.text = "+ Upload Document"
                    isUploading = false
                    Toast.makeText(this@MedicalDocumentsActivity, "Upload failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun viewDocument(document: MedicalDocument) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(document.url), "*/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MedicalDocuments", "No application available to view file", e)
            Toast.makeText(this, "No application is available to open this file.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteDocument(document: MedicalDocument) {
        // Simple confirmation could be added here, omitting for speed
        if (patientId != authRepository.getCurrentUserId()) {
            Toast.makeText(this, "You cannot delete this document.", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = firestoreRepository.deleteMedicalDocument(document)
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess {
                Toast.makeText(this@MedicalDocumentsActivity, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                loadDocuments()
            }.onFailure { e ->
                Log.e("MedicalDocuments", "Failed to delete document", e)
                Toast.makeText(this@MedicalDocumentsActivity, "Failed to delete document.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
