package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ActivityDoctorAvailabilityBinding
import com.amedick.hospitalapp.databinding.ItemAvailabilityScheduleBinding
import com.amedick.hospitalapp.databinding.ItemBlockedDateBinding
import com.amedick.hospitalapp.firebase.AuthRepository
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

@AndroidEntryPoint
class DoctorAvailabilityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorAvailabilityBinding
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var currentSchedule = mutableMapOf<String, List<String>>()
    private var currentBlockedDates = mutableListOf<String>()
    private var doctorId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAvailabilityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.toolbar.setNavigationIcon(com.amedick.hospitalapp.R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        doctorId = authRepository.getCurrentUserId() ?: run {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.rvSchedule.layoutManager = LinearLayoutManager(this)
        binding.rvBlockedDates.layoutManager = LinearLayoutManager(this)

        setupButtons()
        loadAvailability()
    }

    private fun setupButtons() {
        binding.btnAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }
        
        binding.btnAddBlockedDate.setOnClickListener {
            showAddBlockedDateDialog()
        }
        
        binding.btnSave.setOnClickListener {
            saveAvailability()
        }
    }

    private fun loadAvailability() {
        lifecycleScope.launch {
            val result = firestoreRepository.getDoctorAvailability(doctorId)
            result.onSuccess { availability ->
                currentSchedule.clear()
                currentSchedule.putAll(availability.schedule)
                currentBlockedDates.clear()
                currentBlockedDates.addAll(availability.blockedDates)
                
                updateScheduleUI()
                updateBlockedDatesUI()
            }.onFailure {
                Toast.makeText(this@DoctorAvailabilityActivity, "Failed to load availability", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAvailability() {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."
        
        lifecycleScope.launch {
            val availability = Availability(
                doctorId = doctorId,
                schedule = currentSchedule,
                blockedDates = currentBlockedDates,
                slotDuration = 30 // Hardcoded to 30 for now as per requirements
            )
            
            val result = firestoreRepository.saveDoctorAvailability(availability)
            result.onSuccess {
                Toast.makeText(this@DoctorAvailabilityActivity, "Availability Saved Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@DoctorAvailabilityActivity, "Error saving availability", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Availability"
            }
        }
    }

    private fun updateScheduleUI() {
        binding.rvSchedule.adapter = ScheduleAdapter(currentSchedule.toList()) { day ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Availability")
                .setMessage("Remove availability for $day?")
                .setPositiveButton("Delete") { _, _ ->
                    currentSchedule.remove(day)
                    updateScheduleUI()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateBlockedDatesUI() {
        binding.rvBlockedDates.adapter = BlockedDateAdapter(currentBlockedDates) { date ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Remove Blocked Date")
                .setMessage("Remove block for $date?")
                .setPositiveButton("Remove") { _, _ ->
                    currentBlockedDates.remove(date)
                    updateBlockedDatesUI()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showAddScheduleDialog() {
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val availableDays = days.filter { !currentSchedule.containsKey(it) }.toTypedArray()
        
        if (availableDays.isEmpty()) {
            Toast.makeText(this, "All days are already scheduled", Toast.LENGTH_SHORT).show()
            return
        }

        var selectedDay = availableDays[0]
        // Simplified for this phase: pre-generating common slots based on user prompt (09:00 AM - 12:00 PM)
        // In a full implementation, you'd show time pickers.
        val defaultSlots = listOf("09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM")

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Availability")
            .setSingleChoiceItems(availableDays, 0) { _, which ->
                selectedDay = availableDays[which]
            }
            .setPositiveButton("Add Default (9 AM - 12 PM)") { _, _ ->
                currentSchedule[selectedDay] = defaultSlots
                updateScheduleUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddBlockedDateDialog() {
        // Very simple simulated date picker for demo purposes using a quick dialog.
        // A real app would use MaterialDatePicker
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val nextDay = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dayAfter = dateFormat.format(cal.time)

        val options = arrayOf(tomorrow, nextDay, dayAfter)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Date to Block")
            .setItems(options) { _, which ->
                val date = options[which]
                if (!currentBlockedDates.contains(date)) {
                    currentBlockedDates.add(date)
                    updateBlockedDatesUI()
                } else {
                    Toast.makeText(this, "Date already blocked", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ScheduleAdapter(
        private val items: List<Pair<String, List<String>>>,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemAvailabilityScheduleBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAvailabilityScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvDay.text = item.first
            holder.binding.tvTimeSlots.text = "${item.second.size} slots"
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item.first) }
        }

        override fun getItemCount() = items.size
    }

    inner class BlockedDateAdapter(
        private val dates: List<String>,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<BlockedDateAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemBlockedDateBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemBlockedDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val date = dates[position]
            holder.binding.tvDate.text = date
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(date) }
        }

        override fun getItemCount() = dates.size
    }
}
