package com.amedick.hospitalapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.databinding.ActivityDoctorDetailsBinding
import com.amedick.hospitalapp.models.Doctor
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DoctorDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDetailsBinding
    private lateinit var reviewAdapter: com.amedick.hospitalapp.adapters.DoctorReviewAdapter

    @javax.inject.Inject
    lateinit var firestoreRepository: com.amedick.hospitalapp.firebase.FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctor = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DOCTOR, Doctor::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DOCTOR)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        doctor?.let { doc ->
            bindDoctor(doc)
            
            val isAdmin = intent.getBooleanExtra(EXTRA_IS_ADMIN, false)
            if (isAdmin) {
                binding.bookButton.visibility = View.GONE
            } else {
                binding.bookButton.setOnClickListener {
                    val intent = Intent(this, BookAppointmentActivity::class.java).apply {
                        putExtra(BookAppointmentActivity.EXTRA_DOCTOR_ID, doc.doctorId)
                        putExtra(BookAppointmentActivity.EXTRA_DOCTOR_NAME, doc.name)
                        putExtra(BookAppointmentActivity.EXTRA_DOCTOR_SPEC, doc.specialization)
                    }
                    startActivity(intent)
                }
            }
        } ?: run {
            Toast.makeText(this, "Doctor details unavailable", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupReviewsRecyclerView()
        doctor?.doctorId?.let { loadReviews(it) }
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = com.amedick.hospitalapp.adapters.DoctorReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter
    }

    private fun loadReviews(doctorId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.getDoctorReviews(doctorId)
            result.onSuccess { reviews ->
                if (reviews.isEmpty()) {
                    binding.tvNoReviews.visibility = View.VISIBLE
                    binding.rvReviews.visibility = View.GONE
                } else {
                    binding.tvNoReviews.visibility = View.GONE
                    binding.rvReviews.visibility = View.VISIBLE
                    reviewAdapter.updateData(reviews)
                }
            }.onFailure {
                binding.tvNoReviews.visibility = View.VISIBLE
                binding.tvNoReviews.text = "Failed to load reviews."
            }
        }
    }

    private fun bindDoctor(doctor: Doctor) {
        binding.doctorName.text = doctor.name
        binding.doctorSpecialization.text = doctor.specialization.ifEmpty { "General Practitioner" }
        binding.doctorRating.text = String.format("%.1f", doctor.rating)
        binding.doctorExperience.text = if (doctor.experience > 0) "${doctor.experience} yrs" else "N/A"
        binding.doctorHospital.text = doctor.hospital.ifEmpty { "Hospital not specified" }
        binding.doctorEmail.text = doctor.email.ifEmpty { "Email not available" }
        binding.doctorQualification.text = doctor.qualification.ifEmpty { "Qualification not specified" }
        binding.doctorAbout.text = doctor.about.ifEmpty { "No additional information available." }

        // Verification Badge
        if (doctor.isVerified) {
            binding.ivVerifiedBadge.visibility = View.VISIBLE
        } else {
            binding.ivVerifiedBadge.visibility = View.GONE
        }

        // Availability chip
        if (doctor.available) {
            binding.availabilityChip.text = "Available"
            binding.availabilityChip.setChipBackgroundColorResource(com.amedick.hospitalapp.R.color.status_confirmed_bg)
            binding.availabilityChip.setTextColor(getColor(com.amedick.hospitalapp.R.color.status_confirmed))
        } else {
            binding.availabilityChip.text = "Unavailable"
            binding.availabilityChip.setChipBackgroundColorResource(com.amedick.hospitalapp.R.color.status_cancelled_bg)
            binding.availabilityChip.setTextColor(getColor(com.amedick.hospitalapp.R.color.status_cancelled))
            binding.bookButton.isEnabled = false
            binding.bookButton.alpha = 0.5f
        }

        // Load image
        if (doctor.image.isNotEmpty()) {
            Glide.with(this)
                .load(doctor.image)
                .placeholder(com.amedick.hospitalapp.R.drawable.ic_doctor)
                .error(com.amedick.hospitalapp.R.drawable.ic_doctor)
                .centerCrop()
                .into(binding.doctorImage)
        }
    }

    companion object {
        const val EXTRA_DOCTOR = "extra_doctor"
        const val EXTRA_IS_ADMIN = "extra_is_admin"

        fun newIntent(context: Context, doctor: Doctor): Intent =
            Intent(context, DoctorDetailsActivity::class.java).apply {
                putExtra(EXTRA_DOCTOR, doctor)
            }
    }
}
