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
    private var appointmentsJob: kotlinx.coroutines.Job? = null

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

        binding.btnPaymentSetup.setOnClickListener {
            startActivity(Intent(this, DoctorPaymentSetupActivity::class.java))
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
                    val input = android.widget.EditText(this)
                    input.hint = "Reason for early completion"
                    val lp = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    input.layoutParams = lp

                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Early Completion")
                        .setMessage("This appointment hasn't started yet. Please provide a reason for early completion to request patient verification.")
                        .setView(input)
                        .setPositiveButton("Request Verification") { _, _ ->
                            val reason = input.text.toString().trim()
                            lifecycleScope.launch {
                                val result = firestoreRepository.requestCompletionVerification(appt.appointmentId, reason)
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(this@DoctorDashboardActivity, "Completion request sent to patient", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to request verification", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Complete Consultation?")
                        .setMessage("Please confirm that the consultation has actually been completed. We will ask the patient to verify.")
                        .setPositiveButton("Request Patient Verification") { _, _ ->
                            lifecycleScope.launch {
                                val result = firestoreRepository.requestCompletionVerification(appt.appointmentId, "")
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(this@DoctorDashboardActivity, "Completion request sent to patient", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to request verification", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
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
            },
            onJoinMeetClick = { appt ->
                joinGoogleMeet(appt.doctorId)
            },
            onCancelMeetClick = { appt ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want to cancel this appointment?")
                    .setPositiveButton("Yes") { _, _ ->
                        updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.CANCELLED)
                    }
                    .setNegativeButton("No", null)
                    .show()
            },
            onRescheduleMeetClick = { appt ->
                updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.RESCHEDULE_REQUESTED)
            },
            onItemClick = { appt ->
                val intent = Intent(this, com.amedick.hospitalapp.activities.MedicalProfileActivity::class.java).apply {
                    putExtra("EXTRA_PATIENT_ID", appt.patientId)
                }
                startActivity(intent)
            },
            onViewPaymentProofClick = { appt ->
                if (appt.paymentProofUrl.isNotEmpty()) {
                    val dialogView = layoutInflater.inflate(com.amedick.hospitalapp.R.layout.dialog_image_viewer, null)
                    val imageView = dialogView.findViewById<android.widget.ImageView>(com.amedick.hospitalapp.R.id.ivFullscreenImage)
                    com.bumptech.glide.Glide.with(this).load(appt.paymentProofUrl).into(imageView)
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("Close", null)
                        .show()
                }
            }
        )
        binding.rvAppointments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter
    }

    private fun joinGoogleMeet(doctorId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.getUserProfile(doctorId)
            result.onSuccess { doctorProfile ->
                val link = doctorProfile.googleMeetLink
                if (link.isNotEmpty() && link.startsWith("https://meet.google.com/")) {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link))
                    startActivity(intent)
                } else {
                    android.widget.Toast.makeText(this@DoctorDashboardActivity, "Please set your Google Meet link in your Profile first.", android.widget.Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to load profile. Try again.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDashboardData() {
        val uid = authRepository.getCurrentUserId() ?: return
        val userEmail = authRepository.getCurrentUserEmail() ?: "Doctor"
        
        binding.tvWelcomeMessage.text = "Welcome, Dr. ${userEmail.split("@").first().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}"
        binding.tvDate.text = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        binding.progressBar.visibility = android.view.View.VISIBLE
        observeAppointmentsRealtime(uid)
    }

    private fun observeAppointmentsRealtime(uid: String) {
        appointmentsJob?.cancel()
        appointmentsJob = lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                firestoreRepository.getAppointmentsForDoctorRealtime(uid).collect { result ->
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
        }
    }

    private fun updateAppointmentStatus(appointment: com.amedick.hospitalapp.models.Appointment, status: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.updateAppointmentStatus(appointment.appointmentId, status)
            result.onSuccess {
                val successMessage = when (status) {
                    com.amedick.hospitalapp.models.AppointmentStatus.ACCEPTED -> "Appointment accepted"
                    com.amedick.hospitalapp.models.AppointmentStatus.REJECTED,
                    com.amedick.hospitalapp.models.AppointmentStatus.CANCELLED -> "Appointment cancelled"
                    else -> "Appointment updated"
                }
                android.widget.Toast.makeText(this@DoctorDashboardActivity, successMessage, android.widget.Toast.LENGTH_SHORT).show()
                
                if (status == com.amedick.hospitalapp.models.AppointmentStatus.ACCEPTED) {
                    val notifMessage = if (appointment.consultationType == "ONLINE") {
                        "Your video consultation with Dr. ${appointment.doctorName} is scheduled on ${appointment.date} at ${appointment.time}."
                    } else {
                        "Your offline appointment with Dr. ${appointment.doctorName} is confirmed on ${appointment.date} at ${appointment.time}."
                    }
                    firestoreRepository.createNotification(
                        userId = appointment.patientId,
                        title = "Appointment Accepted",
                        message = notifMessage,
                        type = "appointment_accepted",
                        relatedId = appointment.appointmentId
                    )
                }
            }.onFailure {
                android.widget.Toast.makeText(this@DoctorDashboardActivity, "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
