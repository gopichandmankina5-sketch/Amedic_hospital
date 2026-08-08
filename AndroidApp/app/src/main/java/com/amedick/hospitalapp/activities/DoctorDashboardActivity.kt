package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.amedick.hospitalapp.databinding.ActivityDoctorDashboardBinding
import com.amedick.hospitalapp.firebase.AuthRepository
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

        setupRecyclerView()
        loadDashboardData()

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = com.amedick.hospitalapp.adapters.DoctorAppointmentAdapter(
            appointments = emptyList(),
            onAcceptClick = { appt ->
                updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.ACCEPTED)
            },
            onRejectClick = { appt ->
                updateAppointmentStatus(appt, com.amedick.hospitalapp.models.AppointmentStatus.REJECTED)
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
