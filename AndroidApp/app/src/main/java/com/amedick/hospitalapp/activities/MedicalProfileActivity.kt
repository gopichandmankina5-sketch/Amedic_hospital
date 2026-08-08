package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.databinding.ActivityMedicalProfileBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.MedicalProfile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicalProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalProfileBinding

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var patientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        patientId = intent.getStringExtra("EXTRA_PATIENT_ID") ?: authRepository.getCurrentUserId() ?: ""
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // If a doctor is viewing, disable saving
        if (patientId != authRepository.getCurrentUserId()) {
            binding.btnSaveProfile.visibility = View.GONE
            disableEditing()
        }

        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        loadProfile()
    }

    private fun loadProfile() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = firestoreRepository.getMedicalProfile(patientId)
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess { profile ->
                binding.etBloodGroup.setText(profile.bloodGroup)
                binding.etAllergies.setText(profile.allergies)
                binding.etMedications.setText(profile.currentMedications)
                binding.etMedicalHistory.setText(profile.medicalHistory)
                binding.etEmergencyName.setText(profile.emergencyContactName)
                binding.etEmergencyPhone.setText(profile.emergencyContactPhone)
            }.onFailure {
                Toast.makeText(this@MedicalProfileActivity, "Failed to load medical profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val profile = MedicalProfile(
            patientId = patientId,
            bloodGroup = binding.etBloodGroup.text.toString().trim(),
            allergies = binding.etAllergies.text.toString().trim(),
            currentMedications = binding.etMedications.text.toString().trim(),
            medicalHistory = binding.etMedicalHistory.text.toString().trim(),
            emergencyContactName = binding.etEmergencyName.text.toString().trim(),
            emergencyContactPhone = binding.etEmergencyPhone.text.toString().trim()
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false

        lifecycleScope.launch {
            val result = firestoreRepository.saveMedicalProfile(profile)
            binding.progressBar.visibility = View.GONE
            binding.btnSaveProfile.isEnabled = true
            
            result.onSuccess {
                Toast.makeText(this@MedicalProfileActivity, "Profile saved successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@MedicalProfileActivity, "Failed to save profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableEditing() {
        binding.etBloodGroup.isEnabled = false
        binding.etAllergies.isEnabled = false
        binding.etMedications.isEnabled = false
        binding.etMedicalHistory.isEnabled = false
        binding.etEmergencyName.isEnabled = false
        binding.etEmergencyPhone.isEnabled = false
    }
}
