package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.databinding.ActivityReviewBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Review
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var appointmentId: String = ""
    private var doctorId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appointmentId = intent.getStringExtra("appointmentId") ?: return finish()
        doctorId = intent.getStringExtra("doctorId") ?: return finish()

        binding.backButton.setOnClickListener { finish() }

        binding.btnSubmitReview.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val rating = binding.ratingBar.rating
        if (rating == 0f) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        val feedback = binding.etReviewComment.text.toString().trim()
        val patientId = authRepository.getCurrentUserId() ?: return

        val review = Review(
            appointmentId = appointmentId,
            patientId = patientId,
            doctorId = doctorId,
            rating = rating,
            feedback = feedback
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReview.isEnabled = false

        lifecycleScope.launch {
            val result = firestoreRepository.submitReview(review)
            
            result.onSuccess {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ReviewActivity, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                
                // Update appointment to mark as rated
                firestoreRepository.updateAppointmentRatedStatus(appointmentId, true)

                // Notify doctor
                firestoreRepository.createNotification(
                    userId = doctorId,
                    title = "New Review",
                    message = "A patient left you a ${rating}-star review.",
                    type = "review"
                )
                
                finish()
            }.onFailure { error ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReview.isEnabled = true
                Toast.makeText(this@ReviewActivity, error.message ?: "Failed to submit review", Toast.LENGTH_LONG).show()
            }
        }
    }
}
