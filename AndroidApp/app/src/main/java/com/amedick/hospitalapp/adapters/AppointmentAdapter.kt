package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.R
import com.amedick.hospitalapp.databinding.ItemAppointmentBinding
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus

class AppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onCancelClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            val ctx = binding.root.context

            binding.doctorNameText.text = if (appointment.doctorName.isNotEmpty())
                "Dr. ${appointment.doctorName}" else "Unknown Doctor"
            binding.specializationText.text = ""
            binding.dateText.text = appointment.date.ifEmpty { "Date TBD" }
            binding.timeText.text = appointment.time.ifEmpty { "Time TBD" }

            if (appointment.reason.isNotEmpty()) {
                binding.reasonText.text = "Reason: ${appointment.reason}"
                binding.reasonText.visibility = View.VISIBLE
            } else {
                binding.reasonText.visibility = View.GONE
            }

            // Status chip styling
            val (bgRes, textRes, label) = when (appointment.status) {
                AppointmentStatus.PENDING -> Triple(R.color.status_pending_bg, R.color.status_pending, "Pending")
                AppointmentStatus.CONFIRMED -> Triple(R.color.status_confirmed_bg, R.color.status_confirmed, "Confirmed")
                AppointmentStatus.COMPLETED -> Triple(R.color.status_completed_bg, R.color.status_completed, "Completed")
                AppointmentStatus.CANCELLED -> Triple(R.color.status_cancelled_bg, R.color.status_cancelled, "Cancelled")
                AppointmentStatus.REJECTED -> Triple(R.color.status_rejected_bg, R.color.status_rejected, "Rejected")
                else -> Triple(R.color.status_pending_bg, R.color.status_pending, appointment.status)
            }

            binding.statusChip.text = label
            binding.statusChip.setChipBackgroundColorResource(bgRes)
            binding.statusChip.setTextColor(ctx.getColor(textRes))

            // Show cancel button only for cancellable statuses
            val canCancel = appointment.status == AppointmentStatus.PENDING ||
                    appointment.status == AppointmentStatus.CONFIRMED
            binding.cancelButton.visibility = if (canCancel) View.VISIBLE else View.GONE
            binding.cancelButton.setOnClickListener { onCancelClick(appointment) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    fun updateData(newAppointments: List<Appointment>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = appointments.size
            override fun getNewListSize() = newAppointments.size
            override fun areItemsTheSame(old: Int, new: Int) =
                appointments[old].appointmentId == newAppointments[new].appointmentId
            override fun areContentsTheSame(old: Int, new: Int) =
                appointments[old] == newAppointments[new]
        }
        val result = DiffUtil.calculateDiff(diffCallback)
        appointments = newAppointments
        result.dispatchUpdatesTo(this)
    }
}
