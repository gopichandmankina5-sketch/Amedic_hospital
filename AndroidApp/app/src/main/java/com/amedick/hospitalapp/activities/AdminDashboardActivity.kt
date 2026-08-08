package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.databinding.ActivityAdminDashboardBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: com.amedick.hospitalapp.firebase.FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        binding.btnDoctorVerification.setOnClickListener {
            startActivity(Intent(this, AdminVerificationActivity::class.java))
        }

        loadPlatformStats()
    }

    override fun onResume() {
        super.onResume()
        loadPlatformStats()
    }

    private fun loadPlatformStats() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            val result = firestoreRepository.getPlatformStats()
            binding.progressBar.visibility = android.view.View.GONE
            
            result.onSuccess { stats ->
                binding.tvTotalPatients.text = stats["patients"].toString()
                binding.tvTotalDoctors.text = stats["doctors"].toString()
                binding.tvVerifiedDoctors.text = stats["verifiedDoctors"].toString()
                binding.tvPendingDoctors.text = stats["pendingDoctors"].toString()
                binding.tvTotalAppointments.text = stats["totalAppointments"].toString()
            }.onFailure {
                android.widget.Toast.makeText(this@AdminDashboardActivity, "Failed to load stats", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
