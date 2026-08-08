package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.amedick.hospitalapp.databinding.ActivityDoctorDashboardBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.utils.AppointmentUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDashboardBinding
    private lateinit var adapter: com.amedick.hospitalapp.adapters.DoctorAppointmentAdapter

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: com.amedick.hospitalapp.firebase.FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAvailability.setOnClickListener {
            startActivity(Intent(this, DoctorAvailabilityActivity::class.java))
        }
        
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, DoctorProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            com.amedick.hospitalapp.utils.LogoutHelper.showLogoutConfirmation(this)
        }

        setupRecyclerView()
        observeDoctorVerificationStatus()  // Realtime listener — fixes the verification status bug
        loadDashboardData()
    }

    private fun observeDoctorVerificationStatus() {
        val doctorId = authRepository.getCurrentUserId() ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getDoctorProfileRealtime(doctorId).collect { result ->
                    result.onSuccess { user ->
                        binding.tvWelcomeMessage.text = "Welcome, Dr. ${user.name.ifEmpty { user.email.substringBefore("@") }}"
                        applyVerificationState(user.verificationStatus, user.verificationRejectedReason)
                    }.onFailure {
                        // Silently ignore — profile might not be loaded yet
                    }
                }
            }
        }
    }

    private fun applyVerificationState(status: String, rejectionReason: String) {
        // Hide all banners first
        binding.pendingBanner.visibility = View.GONE
        binding.verifiedBanner.visibility = View.GONE
        binding.rejectedBanner.visibility = View.GONE

        when (status) {
            "VERIFIED" -> {
                binding.verifiedBanner.visibility = View.VISIBLE
            }
            "REJECTED" -> {
                binding.rejectedBanner.visibility = View.VISIBLE
                val reason = if (rejectionReason.isNotEmpty()) "Reason: $rejectionReason" else "Reason: Please contact the administrator."
                binding.tvRejectionReason.text = reason
                binding.btnUpdateDocs.setOnClickListener {
                    startActivity(Intent(this, DoctorVerificationActivity::class.java))
                }
            }
            else -> { // PENDING or empty
                binding.pendingBanner.visibility = View.VISIBLE
                binding.btnUploadDocs.setOnClickListener {
                    startActivity(Intent(this, DoctorVerificationActivity::class.java))
                }
            }
        }
    }

    @Deprecated("Replaced by observeDoctorVerificationStatus() for realtime updates")
    private fun loadDoctorDetails() {
        // This is now a no-op — kept for reference only
    }

    private fun setupRecyclerView() {
        adapter = com.amedick.hospitalapp.adapters.DoctorAppointmentAdapter(
            appointments = emptyList(),
            onAcceptClick = { appt ->
                updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.ACCEPTED)
            },
            onRejectClick = { appt ->
                updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.REJECTED)
            },
            onMarkCompletedClick = { appt ->
                if (!AppointmentUtils.isAppointmentStarted(appt)) {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Meeting Not Started")
                        .setMessage("This appointment has not started yet. You can mark it as completed after the scheduled time.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Mark as Completed?")
                        .setMessage("Mark this appointment as completed?")
                        .setPositiveButton("Confirm") { _, _ ->
                            updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.COMPLETED)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onOpenChatClick = { appt ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_APPOINTMENT_ID, appt.appointmentId)
                    putExtra(ChatActivity.EXTRA_OTHER_USER_ID, appt.patientId)
                    putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, appt.patientName)
                }
                startActivity(intent)
            }
        )
        binding.rvAppointments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter
    }

    private fun loadDashboardData() {
        val uid = authRepository.getCurrentUserId() ?: return
        val userEmail = authRepository.getCurrentUserEmail() ?: "Doctor"
        
        binding.tvWelcomeMessage.text = "Welcome, Dr. ${userEmail.split("@").first().capitalize()}"
        binding.tvDate.text = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            val result = firestoreRepository.getAppointmentsForDoctor(uid)
            binding.progressBar.visibility = android.view.View.GONE
            
            result.onSuccess { appointments ->
                val sorted = appointments.sortedByDescending { it.createdAt }
                adapter.updateData(sorted)
                
                binding.tvEmptyState.visibility = if (sorted.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                
                // Calculate stats
                val total = sorted.size
                val pending = sorted.count { it.status == com.amedick.hospitalapp.models.AppointmentStatus.PENDING }
                val accepted = sorted.count { it.status == com.amedick.hospitalapp.models.AppointmentStatus.ACCEPTED }
                
                binding.tvTotalAppointments.text = total.toString()
                binding.tvPendingAppointments.text = pending.toString()
                binding.tvAcceptedAppointments.text = accepted.toString()
            }.onFailure {
                android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to load appointments", android.widget.Toast.LENGTH_SHORT).show()
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun updateAppointmentStatus(appointment: com.amedick.hospitalapp.models.Appointment, status: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.updateAppointmentStatus(appointment.appointmentId, status)
            result.onSuccess {
                android.widget.Toast.makeText(this@DoctorDashboardActivity, "Appointment updated", android.widget.Toast.LENGTH_SHORT).show()
                loadDashboardData() // Reload
            }.onFailure {
                android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
