package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amedick.hospitalapp.databinding.ActivityBookAppointmentBinding
import com.amedick.hospitalapp.utils.DatePickerFragment
import com.amedick.hospitalapp.utils.TimePickerFragment

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctorName = intent.getStringExtra("doctorName") ?: "Doctor"
        binding.doctorName.text = doctorName

        binding.dateInput.setOnClickListener {
            DatePickerFragment { year, month, day ->
                binding.dateInput.setText("$year-${month + 1}-$day")
            }.show(supportFragmentManager, "datePicker")
        }

        binding.timeInput.setOnClickListener {
            TimePickerFragment { hour, minute ->
                binding.timeInput.setText(String.format("%02d:%02d", hour, minute))
            }.show(supportFragmentManager, "timePicker")
        }

        binding.bookButton.setOnClickListener {
            Toast.makeText(this, "Appointment booked successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
