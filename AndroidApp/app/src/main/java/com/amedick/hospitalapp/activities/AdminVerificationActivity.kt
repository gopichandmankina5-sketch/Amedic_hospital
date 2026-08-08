package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
    }

    override fun onResume() {
        super.onResume()
        val doctorId = intent.getStringExtra("EXTRA_DOCTOR_ID")
        loadPendingDoctors(doctorId)
    }

    private fun setupRecyclerView() {
        adapter = PendingDoctorAdapter(
            emptyList(),
            onItemClick = { doctor ->
                val intent = Intent(this, AdminVerificationDetailActivity::class.java).apply {
                    putExtra(AdminVerificationDetailActivity.EXTRA_DOCTOR_ID, doctor.doctorId)
                }
                startActivity(intent)
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
}
