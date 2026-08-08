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
            onRateClick = {}
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
}
