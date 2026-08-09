package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.AppointmentAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminAppointmentListBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminAppointmentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAppointmentListBinding
    private lateinit var adapter: AppointmentAdapter
    private var filterType = "ALL" // ALL or COMPLETED

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filterType = intent.getStringExtra("FILTER_TYPE") ?: "ALL"

        setupToolbar()
        setupRecyclerView()
        observeAppointments()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (filterType == "COMPLETED") "Completed Appointments" else "All Appointments"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // Since Admin doesn't cancel or chat with patients directly via this list, we pass empty handlers
        adapter = AppointmentAdapter(
            appointments = emptyList(),
            onCancelClick = {},
            onOpenChatClick = {},
            onRateClick = {},
            onJoinMeetClick = {},
            onRescheduleClick = {},
            onVerifyCompletionClick = { _, _ -> },
            onItemClick = { appointment ->
                showAppointmentDetailDialog(appointment)
            }
        )
        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter
    }

    private fun observeAppointments() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvAppointments.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getAppointmentsRealtime(filterType).collect { result ->
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { appointments ->
                        adapter.updateData(appointments)
                        
                        if (appointments.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvAppointments.visibility = View.GONE
                            binding.tvEmptyMessage.text = if (filterType == "COMPLETED") "No completed appointments yet." else "No appointments found."
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvAppointments.visibility = View.VISIBLE
                        }
                    }.onFailure {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = "Unable to load information. Please try again."
                        Toast.makeText(this@AdminAppointmentListActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAppointmentDetailDialog(appointment: Appointment) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_appointment_details, null)
        val tvDoctor = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailDoctor)!!
        val tvPatient = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailPatient)!!
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailDate)!!
        val tvTime = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailTime)!!
        val tvReason = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailReason)!!
        val tvStatus = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailStatus)!!
        val tvConsultationType = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailConsultationType)!!
        val layoutOnline = dialogView.findViewById<android.view.View>(R.id.layoutOnlineDetails)!!
        val tvMeetingProvider = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailMeetingProvider)!!
        val tvMeetingStatus = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailMeetingStatus)!!
        val tvMeetingLink = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailMeetingLink)!!

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView as android.view.View)
            .setPositiveButton("Close", null)
            .create()

        val job = lifecycleScope.launch {
            firestoreRepository.getAppointmentDetailsRealtime(appointment.appointmentId).collect { result ->
                result.onSuccess { appt ->
                    tvDoctor.text = "Dr. ${appt.doctorName}"
                    tvPatient.text = appt.patientName
                    tvDate.text = appt.date
                    tvTime.text = appt.time
                    tvReason.text = appt.reason.ifEmpty { "—" }
                    tvStatus.text = appt.status.uppercase()
                    tvStatus.setTextColor(when (appt.status) {
                        "accepted" -> getColor(R.color.color_success)
                        "rejected" -> getColor(R.color.color_error)
                        "completed" -> getColor(R.color.color_primary)
                        else -> getColor(R.color.status_pending)
                    })

                    tvConsultationType.text = appt.consultationType
                    if (appt.consultationType == "ONLINE") {
                        tvConsultationType.setTextColor(getColor(R.color.color_primary))
                        layoutOnline.visibility = android.view.View.VISIBLE
                        
                        tvMeetingProvider.text = "Provider: ${if (appt.meetingProvider.isNotEmpty()) appt.meetingProvider else "Google Meet"}"
                        tvMeetingStatus.text = "Meeting Status: ${appt.meetingStatus}"
                        tvMeetingLink.text = "Link: ${if (appt.meetingUri.isNotEmpty()) appt.meetingUri else "Not available yet"}"
                    } else {
                        tvConsultationType.setTextColor(getColor(R.color.color_text_secondary))
                        layoutOnline.visibility = android.view.View.GONE
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            job.cancel()
        }

        dialog.show()
    }
}
