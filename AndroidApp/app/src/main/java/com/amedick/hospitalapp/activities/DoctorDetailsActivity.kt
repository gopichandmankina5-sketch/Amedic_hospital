package com.amedick.hospitalapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amedick.hospitalapp.databinding.ActivityDoctorDetailsBinding
import com.amedick.hospitalapp.models.Doctor

class DoctorDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctor = intent.getParcelableExtra<Doctor>(EXTRA_DOCTOR)
        doctor?.let { doctorObj ->
            binding.doctorName.text = doctorObj.name
            binding.doctorSpecialization.text = doctorObj.specialization ?: "General"
            binding.doctorEmail.text = doctorObj.email ?: "Unavailable"
            binding.bookButton.setOnClickListener {
                val intent = Intent(this, BookAppointmentActivity::class.java)
                intent.putExtra("doctorName", doctorObj.name)
                startActivity(intent)
            }
        }
    }

    companion object {
        const val EXTRA_DOCTOR = "extra_doctor"

        fun newIntent(context: Context, doctor: Doctor): Intent {
            return Intent(context, DoctorDetailsActivity::class.java).apply {
                putExtra(EXTRA_DOCTOR, doctor)
            }
        }
    }
}
