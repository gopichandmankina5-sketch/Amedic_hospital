package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.PendingDoctorAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminVerificationBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminVerificationBinding
    private lateinit var adapter: PendingDoctorAdapter

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        setupRecyclerView()

        val doctorId = intent.getStringExtra("EXTRA_DOCTOR_ID")
        loadPendingDoctors(doctorId)
    }

    private fun setupRecyclerView() {
        adapter = PendingDoctorAdapter(
            emptyList(),
            onVerifyClick = { doctor ->
                // Show confirmation dialog before verifying
                AlertDialog.Builder(this)
                    .setTitle("Verify Doctor?")
                    .setMessage("Are you sure you want to verify Dr. ${doctor.name}'s professional credentials?")
                    .setPositiveButton("Verify") { _, _ ->
                        verifyDoctor(doctor.doctorId, doctor.name, true, "VERIFIED", "")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onRejectClick = { doctor ->
                // Show rejection reason dialog
                val reasonInput = EditText(this).apply {
                    hint = "Enter rejection reason (required)"
                    setPadding(48, 32, 48, 16)
                }
                AlertDialog.Builder(this)
                    .setTitle("Reject Verification")
                    .setMessage("Please provide a reason for rejecting Dr. ${doctor.name}'s verification request.")
                    .setView(reasonInput)
                    .setPositiveButton("Reject") { _, _ ->
                        val reason = reasonInput.text.toString().trim()
                        if (reason.isEmpty()) {
                            Toast.makeText(this, "Please enter a rejection reason.", Toast.LENGTH_SHORT).show()
                        } else {
                            verifyDoctor(doctor.doctorId, doctor.name, false, "REJECTED", reason)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvPendingDoctors.layoutManager = LinearLayoutManager(this)
        binding.rvPendingDoctors.adapter = adapter
    }

    private fun loadPendingDoctors(doctorId: String? = null) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvPendingDoctors.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

        lifecycleScope.launch {
            val result = firestoreRepository.getPendingDoctors(doctorId)
            binding.progressBar.visibility = View.GONE

            result.onSuccess { doctors ->
                if (doctors.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.rvPendingDoctors.visibility = View.VISIBLE
                    adapter.updateData(doctors)
                }
            }.onFailure {
                binding.emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@AdminVerificationActivity, "Failed to load pending doctors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyDoctor(doctorId: String, doctorName: String, isVerified: Boolean, status: String, rejectionReason: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = firestoreRepository.verifyDoctor(doctorId, isVerified, status, rejectionReason)
            result.onSuccess {
                // Notify the doctor
                val message = if (isVerified)
                    "Congratulations! Your account has been verified. You can now accept patient appointments."
                else
                    "Your verification request was rejected. Reason: $rejectionReason"

                firestoreRepository.createNotification(
                    userId = doctorId,
                    title = if (isVerified) "Account Verified ✓" else "Verification Rejected",
                    message = message,
                    type = if (isVerified) "doctor_verification_approved" else "doctor_verification_rejected"
                )

                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminVerificationActivity, "Doctor $status", Toast.LENGTH_SHORT).show()
                val doctorIdFilter = intent.getStringExtra("EXTRA_DOCTOR_ID")
                loadPendingDoctors(doctorIdFilter)
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminVerificationActivity, "Failed to update status: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
