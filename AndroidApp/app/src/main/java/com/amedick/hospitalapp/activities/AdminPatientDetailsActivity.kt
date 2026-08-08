package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.databinding.ActivityAdminPatientDetailsBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AdminPatientDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPatientDetailsBinding
    
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val patientId = intent.getStringExtra("PATIENT_ID")
        if (patientId == null) {
            Toast.makeText(this, "Invalid Patient ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPatientDetails(patientId)
    }

    private fun loadPatientDetails(patientId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        lifecycleScope.launch {
            val result = firestoreRepository.getUserProfile(patientId)
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess { patient ->
                binding.contentLayout.visibility = View.VISIBLE
                
                binding.tvName.text = patient.name.ifEmpty { "Unknown" }
                binding.tvEmail.text = patient.email.ifEmpty { "Not Provided" }
                binding.tvPhone.text = patient.phone.ifEmpty { "Not Provided" }
                
                if (patient.createdAt != null) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    binding.tvRegistered.text = sdf.format(patient.createdAt)
                } else {
                    binding.tvRegistered.text = "Unknown"
                }

                if (patient.profileImage.isNotEmpty()) {
                    Glide.with(this@AdminPatientDetailsActivity)
                        .load(patient.profileImage)
                        .placeholder(com.amedick.hospitalapp.R.drawable.ic_hospital)
                        .error(com.amedick.hospitalapp.R.drawable.ic_hospital)
                        .centerCrop()
                        .into(binding.ivProfile)
                }
            }.onFailure {
                Toast.makeText(this@AdminPatientDetailsActivity, "Failed to load patient information", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
