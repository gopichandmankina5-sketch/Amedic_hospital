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
import android.provider.OpenableColumns
import com.amedick.hospitalapp.config.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

    @Inject
    lateinit var storageRepository: com.amedick.hospitalapp.firebase.StorageRepository

    private var doctorAvailability: Availability? = null
    private var selectedDateStr: String = ""
    private var selectedTimeStr: String = ""
    private var bookedSlotsForDate: List<String> = emptyList()
    private var selectedConsultationType: String? = null
    private var patientName: String = ""
    
    private var doctorConsultationFee: Double = 0.0
    private var doctorUpiId: String = ""
    private var doctorPaymentQrUrl: String = ""

    private var selectedPaymentProofUri: android.net.Uri? = null
    private var ivScreenshotPreview: android.widget.ImageView? = null

    private val pickPaymentProofLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedPaymentProofUri = uri
            ivScreenshotPreview?.visibility = View.VISIBLE
            ivScreenshotPreview?.let {
                com.bumptech.glide.Glide.with(this).load(uri).into(it)
            }
        }
    }

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
        loadDoctorPaymentInfo(doctorId)

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
                if (selectedConsultationType == "ONLINE") {
                    if (doctorPaymentQrUrl.isEmpty() || doctorConsultationFee <= 0.0) {
                        Toast.makeText(this, "Payment QR is not available for this doctor yet.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    showPaymentDialog(doctorId, doctorName, rescheduleAppointmentId)
                } else {
                    submitBooking(doctorId, doctorName, rescheduleAppointmentId)
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
                            if (rescheduleAppointmentId != null) {
                                Toast.makeText(this@BookAppointmentActivity, "Reschedule request sent", Toast.LENGTH_SHORT).show()
                            } else {
                                if (selectedConsultationType == "ONLINE") {
                                    Toast.makeText(this@BookAppointmentActivity, "Payment submitted. Appointment request sent.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this@BookAppointmentActivity, "Appointment booked successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showSuccessDialog()
                        }
                        is AppointmentState.Error -> {
                            setLoading(false)
                            if (rescheduleAppointmentId != null) {
                                Toast.makeText(this@BookAppointmentActivity, "Failed to send reschedule request", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@BookAppointmentActivity, "Failed to submit appointment request. Please try again.", Toast.LENGTH_LONG).show()
                                android.util.Log.e("BookAppointment", "Failed to book appointment: ${state.message}")
                            }
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

    private fun loadDoctorPaymentInfo(doctorId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.getDoctorPaymentBookingInfo(doctorId)
            result.onSuccess { info ->
                doctorConsultationFee = info["consultationFee"] as? Double ?: 0.0
                doctorUpiId = info["upiId"] as? String ?: ""
                doctorPaymentQrUrl = info["paymentQrUrl"] as? String ?: ""
            }
        }
    }

    private fun submitBooking(doctorId: String, doctorName: String, rescheduleAppointmentId: String?, paymentStatus: String = "pending", paymentProofUrl: String = "") {
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
                consultationType = selectedConsultationType!!,
                consultationFee = if (selectedConsultationType == "ONLINE") doctorConsultationFee else 0.0,
                upiId = if (selectedConsultationType == "ONLINE") doctorUpiId else "",
                paymentQrUrl = if (selectedConsultationType == "ONLINE") doctorPaymentQrUrl else "",
                paymentStatus = paymentStatus,
                paymentProofUrl = paymentProofUrl
            )
        }
    }

    private fun uploadProofAndSubmit(doctorId: String, doctorName: String, rescheduleAppointmentId: String?) {
        setLoading(true)
        val patientId = firestoreRepository.getCurrentUserId()
        if (patientId == null) {
            setLoading(false)
            return
        }

        val uri = selectedPaymentProofUri!!
        var fileName = "payment_proof.jpg"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                }
            }
        } catch (_: Exception) {}

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Failed to read file bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart("file", fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/auto/upload")
                    .post(requestBody)
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Cloudinary server error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                val secureUrl = JSONObject(responseBody).getString("secure_url")

                withContext(Dispatchers.Main) {
                    submitBooking(doctorId, doctorName, rescheduleAppointmentId, "submitted", secureUrl)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@BookAppointmentActivity, "Upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("BookAppointment", "Upload failed", exception)
                }
            }
        }
    }

    private fun showPaymentDialog(doctorId: String, doctorName: String, rescheduleAppointmentId: String?) {
        val dialogView = layoutInflater.inflate(com.amedick.hospitalapp.R.layout.dialog_payment_booking, null)
        
        val tvDoctorName = dialogView.findViewById<android.widget.TextView>(com.amedick.hospitalapp.R.id.tvDoctorName)
        val tvConsultationFee = dialogView.findViewById<android.widget.TextView>(com.amedick.hospitalapp.R.id.tvConsultationFee)
        val tvUpiId = dialogView.findViewById<android.widget.TextView>(com.amedick.hospitalapp.R.id.tvUpiId)
        val ivPaymentQr = dialogView.findViewById<android.widget.ImageView>(com.amedick.hospitalapp.R.id.ivPaymentQr)
        val btnCancel = dialogView.findViewById<android.view.View>(com.amedick.hospitalapp.R.id.btnCancel)
        val btnIHavePaid = dialogView.findViewById<android.view.View>(com.amedick.hospitalapp.R.id.btnIHavePaid)

        tvDoctorName.text = doctorName
        tvConsultationFee.text = "₹$doctorConsultationFee"
        tvUpiId.text = doctorUpiId
        
        if (doctorPaymentQrUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this).load(doctorPaymentQrUrl).into(ivPaymentQr)
        }

        val btnUploadScreenshot = dialogView.findViewById<android.view.View>(com.amedick.hospitalapp.R.id.btnUploadScreenshot)
        ivScreenshotPreview = dialogView.findViewById<android.widget.ImageView>(com.amedick.hospitalapp.R.id.ivScreenshotPreview)
        selectedPaymentProofUri = null

        btnUploadScreenshot.setOnClickListener {
            pickPaymentProofLauncher.launch("image/*")
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnIHavePaid.setOnClickListener {
            if (selectedPaymentProofUri == null) {
                Toast.makeText(this, "Please upload a transaction screenshot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            uploadProofAndSubmit(doctorId, doctorName, rescheduleAppointmentId)
        }

        dialog.show()
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
