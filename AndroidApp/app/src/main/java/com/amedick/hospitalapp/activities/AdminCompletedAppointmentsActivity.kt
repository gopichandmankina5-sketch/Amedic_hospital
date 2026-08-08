package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.AdminCompletedAppointmentAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminCompletedAppointmentsBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Review
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminCompletedAppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCompletedAppointmentsBinding
    private lateinit var adapter: AdminCompletedAppointmentAdapter

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCompletedAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeAppointmentsAndReviews()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdminCompletedAppointmentAdapter(
            appointments = emptyList(),
            reviewsMap = emptyMap()
        )
        binding.rvCompletedAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvCompletedAppointments.adapter = adapter
    }

    private fun observeAppointmentsAndReviews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvCompletedAppointments.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getAppointmentsRealtime("COMPLETED").collect { result ->
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { appointments ->
                        if (appointments.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvCompletedAppointments.visibility = View.GONE
                            adapter.updateData(emptyList(), emptyMap())
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvCompletedAppointments.visibility = View.VISIBLE
                            
                            // Fetch reviews for each appointment
                            val reviewsMap = mutableMapOf<String, Review?>()
                            appointments.forEach { appt ->
                                val reviewResult = firestoreRepository.getReviewForAppointment(appt.appointmentId)
                                reviewsMap[appt.appointmentId] = reviewResult.getOrNull()
                            }
                            
                            adapter.updateData(appointments, reviewsMap)
                        }
                    }.onFailure {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.tvEmptyTitle.text = "Unable to load appointments"
                        binding.tvEmptySubtitle.text = "Please try again later."
                        Toast.makeText(this@AdminCompletedAppointmentsActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
