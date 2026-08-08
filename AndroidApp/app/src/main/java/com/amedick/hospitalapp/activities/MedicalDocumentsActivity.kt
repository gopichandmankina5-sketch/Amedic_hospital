package com.amedick.hospitalapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.MedicalDocumentAdapter
import com.amedick.hospitalapp.databinding.ActivityMedicalDocumentsBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                uploadDocument(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        patientId = intent.getStringExtra("EXTRA_PATIENT_ID") ?: authRepository.getCurrentUserId() ?: ""
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // If doctor is viewing, disable upload
        if (patientId != authRepository.getCurrentUserId()) {
            binding.btnUploadDocument.visibility = View.GONE
        } else {
            binding.btnUploadDocument.setOnClickListener {
                pickDocument()
            }
        }

        setupRecyclerView()
        loadDocuments()
    }

    private fun setupRecyclerView() {
        adapter = MedicalDocumentAdapter(emptyList()) { document ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(document.url)
            startActivity(intent)
        }
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
            }.onFailure {
                binding.emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@MedicalDocumentsActivity, "Failed to load documents.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickDocument() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Allow all types, or specific like application/pdf
            val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        documentPickerLauncher.launch(intent)
    }

    private fun uploadDocument(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUploadDocument.isEnabled = false
        
        lifecycleScope.launch {
            // Provide a default name/type for simplicity in this demo, real app would ask user
            val name = "Medical Document"
            val type = "Uploaded File"
            
            val result = firestoreRepository.uploadMedicalDocument(patientId, name, type, uri)
            binding.progressBar.visibility = View.GONE
            binding.btnUploadDocument.isEnabled = true
            
            result.onSuccess {
                Toast.makeText(this@MedicalDocumentsActivity, "Document uploaded successfully", Toast.LENGTH_SHORT).show()
                loadDocuments()
            }.onFailure {
                Toast.makeText(this@MedicalDocumentsActivity, "Failed to upload document", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
