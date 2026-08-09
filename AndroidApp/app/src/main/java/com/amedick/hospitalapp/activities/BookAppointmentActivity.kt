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
import com.amedick.hospitalapp.databinding.ItemTimeSlotBinding
import com.amedick.hospitalapp.utils.DatePickerFragment
import com.amedick.hospitalapp.viewmodel.AppointmentState
import com.amedick.hospitalapp.viewmodel.AppointmentViewModel
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Availability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup

@AndroidEntryPoint
class BookAppointmentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DOCTOR_ID = "doctorId"
        const val EXTRA_DOCTOR_NAME = "doctorName"
        const val EXTRA_DOCTOR_SPEC = "doctorSpec"
    }

    private lateinit var binding: ActivityBookAppointmentBinding
    private val viewModel: AppointmentViewModel by viewModels()
    
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var doctorAvailability: Availability? = null
    private var selectedDateStr: String = ""
    private var selectedTimeStr: String = ""
    private var bookedSlotsForDate: List<String> = emptyList()
    private var selectedConsultationType: String? = null
    private var patientName: String = ""

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

        binding.rvTimeSlots.layoutManager = GridLayoutManager(this, 3)

        // Date picker
        binding.dateInput.setOnClickListener {
            DatePickerFragment { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                // Prevent past dates
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                
                if (calendar.before(today)) {
                    Toast.makeText(this, "Cannot book past dates", Toast.LENGTH_SHORT).show()
                    return@DatePickerFragment
                }

                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDateStr = format.format(calendar.time)
                binding.dateInput.setText(selectedDateStr)
                
                checkAvailabilityForDate(selectedDateStr, doctorId)
            }.show(supportFragmentManager, "datePicker")
        }
        binding.dateInputLayout.setEndIconOnClickListener {
            binding.dateInput.performClick()
        }

        loadDoctorAvailability(doctorId)

        // Fetch patient profile for name
        lifecycleScope.launch {
            val uid = firestoreRepository.getCurrentUserId() ?: return@launch
            firestoreRepository.getUserProfile(uid).onSuccess { user ->
                patientName = user.name
            }
        }

        // Setup consultation card selection
        binding.cardOffline.setOnClickListener {
            selectedConsultationType = "OFFLINE"
            binding.cardOffline.setStrokeColor(android.content.res.ColorStateList.valueOf(getColor(com.amedick.hospitalapp.R.color.color_primary)))
            binding.cardOffline.setCardBackgroundColor(getColor(com.amedick.hospitalapp.R.color.color_primary_light))
            binding.cardOnline.setStrokeColor(android.content.res.ColorStateList.valueOf(getColor(com.amedick.hospitalapp.R.color.color_outline)))
            binding.cardOnline.setCardBackgroundColor(getColor(com.amedick.hospitalapp.R.color.color_surface))
        }

        binding.cardOnline.setOnClickListener {
            selectedConsultationType = "ONLINE"
            binding.cardOnline.setStrokeColor(android.content.res.ColorStateList.valueOf(getColor(com.amedick.hospitalapp.R.color.color_primary)))
            binding.cardOnline.setCardBackgroundColor(getColor(com.amedick.hospitalapp.R.color.color_primary_light))
            binding.cardOffline.setStrokeColor(android.content.res.ColorStateList.valueOf(getColor(com.amedick.hospitalapp.R.color.color_outline)))
            binding.cardOffline.setCardBackgroundColor(getColor(com.amedick.hospitalapp.R.color.color_surface))
        }

        // Remove the time picker block entirely since we use dynamic slots

        val rescheduleAppointmentId = intent.getStringExtra("EXTRA_RESCHEDULE_APPOINTMENT_ID")

        binding.bookButton.setOnClickListener {
            if (validateInputs(doctorId)) {
                setLoading(true)
                if (rescheduleAppointmentId != null) {
                    viewModel.rescheduleAppointment(
                        appointmentId = rescheduleAppointmentId,
                        date = selectedDateStr,
                        time = selectedTimeStr,
                        consultationType = selectedConsultationType!!
                    )
                } else {
                    viewModel.bookAppointment(
                        doctorId = doctorId,
                        doctorName = doctorName,
                        date = selectedDateStr,
                        time = selectedTimeStr,
                        reason = binding.reasonInput.text.toString().trim(),
                        patientName = patientName,
                        consultationType = selectedConsultationType!!
                    )
                }
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

    private fun loadDoctorAvailability(doctorId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.getDoctorAvailability(doctorId)
            result.onSuccess {
                doctorAvailability = it
            }
        }
    }

    private fun checkAvailabilityForDate(dateStr: String, doctorId: String) {
        val availability = doctorAvailability ?: return
        
        binding.tvNoSlots.visibility = View.VISIBLE
        binding.rvTimeSlots.visibility = View.GONE
        selectedTimeStr = ""

        if (availability.blockedDates.contains(dateStr)) {
            binding.tvNoSlots.text = "Doctor is unavailable on this date."
            return
        }

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr) ?: return
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeek = dayFormat.format(date)

        val slotsForDay = availability.schedule[dayOfWeek]
        if (slotsForDay.isNullOrEmpty()) {
            binding.tvNoSlots.text = "No availability for $dayOfWeek."
            return
        }

        binding.tvNoSlots.text = "Loading slots..."
        
        lifecycleScope.launch {
            val result = firestoreRepository.getAppointmentsForDoctor(doctorId)
            result.onSuccess { appointments ->
                // Filter booked ones for this date
                val booked = appointments.filter { 
                    it.date == dateStr && 
                    it.status != com.amedick.hospitalapp.models.AppointmentStatus.CANCELLED && 
                    it.status != com.amedick.hospitalapp.models.AppointmentStatus.REJECTED 
                }.map { it.time }
                
                bookedSlotsForDate = booked
                generateTimeSlotUI(slotsForDay, dateStr)
            }.onFailure {
                binding.tvNoSlots.text = "Failed to check slot availability."
            }
        }
    }

    private fun generateTimeSlotUI(slots: List<String>, dateStr: String) {
        binding.tvNoSlots.visibility = View.GONE
        binding.rvTimeSlots.visibility = View.VISIBLE
        
        val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) == dateStr
        val currentFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTimeStr = currentFormat.format(Date())
        val currentTimeParsed = currentFormat.parse(currentTimeStr)
        
        // Disable past slots if today
        val validSlots = slots.map { slot ->
            val isBooked = bookedSlotsForDate.contains(slot)
            var isPast = false
            if (isToday) {
                try {
                    val slotTime = currentFormat.parse(slot)
                    if (slotTime != null && currentTimeParsed != null && slotTime.before(currentTimeParsed)) {
                        isPast = true
                    }
                } catch (e: Exception) {}
            }
            SlotItem(slot, !isBooked && !isPast)
        }
        
        binding.rvTimeSlots.adapter = SlotAdapter(validSlots) { time ->
            selectedTimeStr = time
        }
        
        if (validSlots.none { it.isAvailable }) {
            binding.rvTimeSlots.visibility = View.GONE
            binding.tvNoSlots.visibility = View.VISIBLE
            binding.tvNoSlots.text = "No slots available for this date."
        }
    }

    private fun validateInputs(doctorId: String): Boolean {
        if (doctorId.isBlank()) {
            Toast.makeText(this, "Doctor information is missing.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedDateStr.isBlank()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedTimeStr.isBlank()) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedConsultationType == null) {
            Toast.makeText(this, "Please select how you would like to consult the doctor.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    data class SlotItem(val time: String, val isAvailable: Boolean)

    inner class SlotAdapter(
        private val slots: List<SlotItem>,
        private val onSlotSelected: (String) -> Unit
    ) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {
        
        private var selectedPosition = -1

        inner class ViewHolder(val binding: ItemTimeSlotBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val slot = slots[position]
            holder.binding.tvTime.text = slot.time
            
            if (!slot.isAvailable) {
                holder.binding.cardSlot.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                holder.binding.tvTime.setTextColor(Color.GRAY)
                holder.binding.cardSlot.isClickable = false
            } else {
                if (position == selectedPosition) {
                    holder.binding.cardSlot.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                    holder.binding.tvTime.setTextColor(Color.WHITE)
                } else {
                    holder.binding.cardSlot.setCardBackgroundColor(Color.TRANSPARENT)
                    holder.binding.tvTime.setTextColor(Color.BLACK)
                }
                
                holder.binding.cardSlot.setOnClickListener {
                    val oldPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onSlotSelected(slot.time)
                }
            }
        }

        override fun getItemCount() = slots.size
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
