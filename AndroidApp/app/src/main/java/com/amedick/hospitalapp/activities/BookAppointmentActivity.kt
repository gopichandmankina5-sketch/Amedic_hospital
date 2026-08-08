package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.databinding.ActivityBookAppointmentBinding
import com.amedick.hospitalapp.utils.DatePickerFragment
import com.amedick.hospitalapp.utils.TimePickerFragment
import com.amedick.hospitalapp.viewmodel.AppointmentState
import com.amedick.hospitalapp.viewmodel.AppointmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAppointmentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DOCTOR_ID = "doctorId"
        const val EXTRA_DOCTOR_NAME = "doctorName"
        const val EXTRA_DOCTOR_SPEC = "doctorSpec"
    }

    private lateinit var binding: ActivityBookAppointmentBinding
    private val viewModel: AppointmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctorId = intent.getStringExtra(EXTRA_DOCTOR_ID).orEmpty()
        val doctorName = intent.getStringExtra(EXTRA_DOCTOR_NAME) ?: "Doctor"
        val doctorSpec = intent.getStringExtra(EXTRA_DOCTOR_SPEC).orEmpty()

        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.doctorName.text = doctorName
        binding.doctorSpecialization.text = doctorSpec

        // Date picker
        binding.dateInput.setOnClickListener {
            DatePickerFragment { year, month, day ->
                binding.dateInput.setText("$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}")
            }.show(supportFragmentManager, "datePicker")
        }
        binding.dateInputLayout.setEndIconOnClickListener {
            binding.dateInput.performClick()
        }

        // Time picker
        binding.timeInput.setOnClickListener {
            TimePickerFragment { hour, minute ->
                binding.timeInput.setText(String.format("%02d:%02d", hour, minute))
            }.show(supportFragmentManager, "timePicker")
        }
        binding.timeInputLayout.setEndIconOnClickListener {
            binding.timeInput.performClick()
        }

        binding.bookButton.setOnClickListener {
            if (validateInputs(doctorId)) {
                setLoading(true)
                viewModel.bookAppointment(
                    doctorId = doctorId,
                    doctorName = doctorName,
                    date = binding.dateInput.text.toString(),
                    time = binding.timeInput.text.toString(),
                    reason = binding.reasonInput.text.toString().trim()
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookingState.collect { state ->
                    when (state) {
                        is AppointmentState.Idle -> setLoading(false)
                        is AppointmentState.Loading -> setLoading(true)
                        is AppointmentState.Success -> {
                            setLoading(false)
                            showSuccessDialog()
                        }
                        is AppointmentState.Error -> {
                            setLoading(false)
                            Toast.makeText(this@BookAppointmentActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetBookingState()
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs(doctorId: String): Boolean {
        if (doctorId.isBlank()) {
            Toast.makeText(this, "Doctor information is missing.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.dateInput.text.isNullOrBlank()) {
            binding.dateInputLayout.error = "Please select a date"
            return false
        }
        binding.dateInputLayout.error = null

        if (binding.timeInput.text.isNullOrBlank()) {
            binding.timeInputLayout.error = "Please select a time"
            return false
        }
        binding.timeInputLayout.error = null

        return true
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Appointment Confirmed!")
            .setMessage("Your appointment has been successfully booked.\n\nYou will receive updates about your appointment status.")
            .setPositiveButton("View My Appointments") { _, _ ->
                val intent = android.content.Intent(this, MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigateTo", "appointments")
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Back to Home") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.bookButton.isEnabled = !loading
        binding.bookButton.alpha = if (loading) 0.7f else 1f
    }
}
