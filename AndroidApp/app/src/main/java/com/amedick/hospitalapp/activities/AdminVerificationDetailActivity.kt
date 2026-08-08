package com.amedick.hospitalapp.activities

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.VerificationDocumentAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminVerificationDetailBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.DoctorVerificationDocument
import com.amedick.hospitalapp.models.User
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminVerificationDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DOCTOR_ID = "EXTRA_DOCTOR_ID"
        private const val DOC_MEDICAL_REGISTRATION = "MEDICAL_REGISTRATION"
        private const val DOC_MEDICAL_DEGREE = "MEDICAL_DEGREE"
        private const val DOC_GOVERNMENT_ID = "GOVERNMENT_ID"
        private const val DOC_EXPERIENCE_CERTIFICATE = "EXPERIENCE_CERTIFICATE"
    }

    private lateinit var binding: ActivityAdminVerificationDetailBinding
    private lateinit var documentAdapter: VerificationDocumentAdapter

    @Inject lateinit var firestoreRepository: FirestoreRepository

    private var doctorId: String = ""
    private var doctorName: String = ""
    private var currentDocuments: List<DoctorVerificationDocument> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = intent.getStringExtra(EXTRA_DOCTOR_ID) ?: run {
            Toast.makeText(this, "Doctor not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeDoctorProfile()
        observeDocuments()
        setupActionButtons()
    }

    private fun setupRecyclerView() {
        documentAdapter = VerificationDocumentAdapter(
            documents = emptyList(),
            onViewClick = { doc -> viewDocument(doc) },
            onDownloadClick = { doc -> downloadDocument(doc) }
        )
        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        binding.rvDocuments.adapter = documentAdapter
    }

    // ── Realtime Doctor Profile ───────────────────────────────────────────────

    private fun observeDoctorProfile() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getDoctorProfileRealtime(doctorId).collect { result ->
                    result.onSuccess { user ->
                        populateDoctorProfile(user)
                    }.onFailure {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@AdminVerificationDetailActivity,
                            "Unable to load doctor profile.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun populateDoctorProfile(user: User) {
        doctorName = user.name.ifEmpty { user.email.substringBefore("@") }

        binding.tvDoctorName.text = "Dr. ${user.name.ifEmpty { "—" }}"
        binding.tvSpecialization.text = user.specialization.ifEmpty { "Specialization not set" }
        binding.tvEmail.text = user.email.ifEmpty { "—" }
        binding.tvPhone.text = user.phone.ifEmpty { "Not provided" }
        binding.tvQualification.text = "${user.qualification.ifEmpty { "—" }}, ${user.experience} yrs experience"
        binding.tvExperience.text = "${user.experience} years experience"
        binding.tvRegNumber.text = "Reg No: ${user.medicalRegistrationNumber.ifEmpty { "Not provided" }}"

        // Verification status chip
        when (user.verificationStatus) {
            "VERIFIED" -> {
                binding.chipVerificationStatus.text = "✓ VERIFIED"
                binding.chipVerificationStatus.chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9"))
                binding.chipVerificationStatus.setTextColor(android.graphics.Color.parseColor("#1B5E20"))
            }
            "REJECTED" -> {
                binding.chipVerificationStatus.text = "✕ REJECTED"
                binding.chipVerificationStatus.chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE"))
                binding.chipVerificationStatus.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            }
            else -> {
                binding.chipVerificationStatus.text = "⏳ PENDING"
                binding.chipVerificationStatus.chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0"))
                binding.chipVerificationStatus.setTextColor(android.graphics.Color.parseColor("#E65100"))
            }
        }

        // Load profile photo
        if (user.profileImage.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImage)
                .placeholder(com.amedick.hospitalapp.R.drawable.ic_doctor)
                .circleCrop()
                .into(binding.ivDoctorPhoto)
        }

        binding.cardDoctorProfile.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    // ── Realtime Verification Documents ──────────────────────────────────────

    private fun observeDocuments() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getVerificationDocumentsRealtime(doctorId).collect { result ->
                    result.onSuccess { docs ->
                        currentDocuments = docs
                        updateDocumentsUI(docs)
                    }.onFailure {
                        Toast.makeText(
                            this@AdminVerificationDetailActivity,
                            "Unable to load documents. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun updateDocumentsUI(docs: List<DoctorVerificationDocument>) {
        val docMap = docs.associateBy { it.documentType }

        // Update checklist
        updateCheckmark(binding.ivCheckMedReg, docMap.containsKey(DOC_MEDICAL_REGISTRATION))
        updateCheckmark(binding.ivCheckMedDegree, docMap.containsKey(DOC_MEDICAL_DEGREE))
        updateCheckmark(binding.ivCheckGovtId, docMap.containsKey(DOC_GOVERNMENT_ID))
        updateCheckmark(binding.ivCheckExpCert, docMap.containsKey(DOC_EXPERIENCE_CERTIFICATE))

        val uploadedCount = docs.size
        binding.tvDocumentProgress.text = "$uploadedCount of 4 documents uploaded"

        // Update RecyclerView
        documentAdapter.updateData(docs)
        binding.tvNoDocuments.visibility = if (docs.isEmpty()) View.VISIBLE else View.GONE

        // Check required docs
        val hasRequiredDocs = docMap.containsKey(DOC_MEDICAL_REGISTRATION) &&
                docMap.containsKey(DOC_MEDICAL_DEGREE) &&
                docMap.containsKey(DOC_GOVERNMENT_ID)

        binding.btnVerifyDoctor.isEnabled = hasRequiredDocs
        binding.cardMissingDocsWarning.visibility = if (!hasRequiredDocs) View.VISIBLE else View.GONE

        // Show cards
        binding.cardChecklist.visibility = View.VISIBLE
        binding.cardDocuments.visibility = View.VISIBLE
        binding.actionButtons.visibility = View.VISIBLE
    }

    private fun updateCheckmark(imageView: android.widget.ImageView, isPresent: Boolean) {
        if (isPresent) {
            imageView.setImageResource(com.amedick.hospitalapp.R.drawable.ic_verified)
            imageView.imageTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2E7D32")
            )
        } else {
            imageView.setImageResource(com.amedick.hospitalapp.R.drawable.ic_close)
            imageView.imageTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#F57C00")
            )
        }
    }

    // ── Action Buttons ────────────────────────────────────────────────────────

    private fun setupActionButtons() {
        binding.btnVerifyDoctor.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Verify Doctor?")
                .setMessage("Have you reviewed the submitted documents and confirmed that they are valid?\n\nDr. $doctorName will be verified and become visible to patients.")
                .setPositiveButton("Verify") { _, _ ->
                    performVerification(true, "VERIFIED", "")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnReject.setOnClickListener {
            val reasonInput = EditText(this).apply {
                hint = "Enter rejection reason (required)"
                setPadding(48, 32, 48, 16)
                maxLines = 4
            }
            AlertDialog.Builder(this)
                .setTitle("Reject Verification")
                .setMessage("Please provide a clear reason for rejecting Dr. $doctorName's verification request.")
                .setView(reasonInput)
                .setPositiveButton("Reject") { _, _ ->
                    val reason = reasonInput.text.toString().trim()
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Please enter a rejection reason.", Toast.LENGTH_SHORT).show()
                    } else {
                        performVerification(false, "REJECTED", reason)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performVerification(isVerified: Boolean, status: String, rejectionReason: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerifyDoctor.isEnabled = false
        binding.btnReject.isEnabled = false

        lifecycleScope.launch {
            val result = firestoreRepository.verifyDoctor(doctorId, isVerified, status, rejectionReason)
            binding.progressBar.visibility = View.GONE

            result.onSuccess {
                val notifTitle = if (isVerified) "Account Verified ✓" else "Verification Rejected"
                val notifMessage = if (isVerified)
                    "Congratulations! Your account has been verified. You can now accept patient appointments."
                else
                    "Your verification request was rejected. Reason: $rejectionReason"

                firestoreRepository.createNotification(
                    userId = doctorId,
                    title = notifTitle,
                    message = notifMessage,
                    type = if (isVerified) "doctor_verification_approved" else "doctor_verification_rejected"
                )

                val toastMsg = if (isVerified) "Dr. $doctorName has been verified." else "Verification rejected."
                Toast.makeText(this@AdminVerificationDetailActivity, toastMsg, Toast.LENGTH_LONG).show()
                finish()
            }.onFailure { e ->
                binding.btnVerifyDoctor.isEnabled = true
                binding.btnReject.isEnabled = true
                Toast.makeText(
                    this@AdminVerificationDetailActivity,
                    "Failed to update status: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ── Document Actions ──────────────────────────────────────────────────────

    private fun viewDocument(doc: DoctorVerificationDocument) {
        if (doc.fileUrl.isEmpty()) {
            Toast.makeText(this, "Document URL is not available.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val mimeType = resolveMimeType(doc)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(doc.fileUrl), mimeType)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: open in browser
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.fileUrl))
                startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(this, "No application available to open this document.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadDocument(doc: DoctorVerificationDocument) {
        if (doc.fileUrl.isEmpty()) {
            Toast.makeText(this, "Document URL is not available.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = doc.fileName.ifEmpty {
                "${doc.documentType}_${doc.doctorId}.${extensionFromMime(resolveMimeType(doc))}"
            }
            val mimeType = resolveMimeType(doc)

            val request = DownloadManager.Request(Uri.parse(doc.fileUrl)).apply {
                setTitle("Downloading: ${documentTypeLabel(doc.documentType)}")
                setDescription("AmedicK — Saving $fileName")
                setMimeType(mimeType)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AmedicK/$fileName")
                addRequestHeader("Accept", "*/*")
            }

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Downloading $fileName to Downloads/AmedicK/", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resolveMimeType(doc: DoctorVerificationDocument): String {
        if (doc.fileType.isNotEmpty() && doc.fileType != "application/octet-stream") return doc.fileType
        return when {
            doc.fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            doc.fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            doc.fileName.endsWith(".jpg", ignoreCase = true) || doc.fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            doc.fileUrl.contains(".pdf", ignoreCase = true) -> "application/pdf"
            doc.fileUrl.contains(".png", ignoreCase = true) -> "image/png"
            else -> "image/jpeg"
        }
    }

    private fun extensionFromMime(mime: String): String = when (mime) {
        "application/pdf" -> "pdf"
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        else -> "bin"
    }

    private fun documentTypeLabel(type: String): String = when (type) {
        "MEDICAL_REGISTRATION" -> "Medical Registration Certificate"
        "MEDICAL_DEGREE" -> "Medical Degree Certificate"
        "GOVERNMENT_ID" -> "Government ID"
        "EXPERIENCE_CERTIFICATE" -> "Experience Certificate"
        else -> type.replace("_", " ")
    }
}
