package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.R
import com.amedick.hospitalapp.databinding.ItemDoctorAppointmentBinding
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus

class DoctorAppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onAcceptClick: (Appointment) -> Unit,
    private val onRejectClick: (Appointment) -> Unit,
    private val onMarkCompletedClick: (Appointment) -> Unit,
    private val onOpenChatClick: (Appointment) -> Unit
) : RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(private val binding: ItemDoctorAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            val ctx = binding.root.context

            binding.patientNameText.text = if (appointment.patientName.isNotEmpty())
                appointment.patientName else "Unknown Patient"
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
                AppointmentStatus.ACCEPTED -> Triple(R.color.status_confirmed_bg, R.color.status_confirmed, "Accepted")
                AppointmentStatus.COMPLETED -> Triple(R.color.status_completed_bg, R.color.status_completed, "Completed")
                AppointmentStatus.CANCELLED -> Triple(R.color.status_cancelled_bg, R.color.status_cancelled, "Cancelled")
                AppointmentStatus.REJECTED -> Triple(R.color.status_rejected_bg, R.color.status_rejected, "Rejected")
                else -> Triple(R.color.status_pending_bg, R.color.status_pending, appointment.status)
            }

            binding.statusChip.text = label
            binding.statusChip.setChipBackgroundColorResource(bgRes)
            binding.statusChip.setTextColor(ctx.getColor(textRes))

            // Show action buttons based on status
            if (appointment.status == AppointmentStatus.PENDING) {
                binding.actionButtonsContainer.visibility = View.VISIBLE
                binding.acceptedActionButtonsContainer.visibility = View.GONE
                
                binding.acceptButton.setOnClickListener { onAcceptClick(appointment) }
                binding.rejectButton.setOnClickListener { onRejectClick(appointment) }
            } else if (appointment.status == AppointmentStatus.ACCEPTED) {
                binding.actionButtonsContainer.visibility = View.GONE
                binding.acceptedActionButtonsContainer.visibility = View.VISIBLE
                
                binding.markCompletedButton.setOnClickListener { onMarkCompletedClick(appointment) }
                binding.openChatButton.setOnClickListener { onOpenChatClick(appointment) }
            } else {
                binding.actionButtonsContainer.visibility = View.GONE
                binding.acceptedActionButtonsContainer.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemDoctorAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
