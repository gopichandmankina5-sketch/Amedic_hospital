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
    private val onOpenChatClick: (Appointment) -> Unit,
    private val onJoinMeetClick: (Appointment) -> Unit,
    private var onCancelMeetClick: (Appointment) -> Unit = {},
    var onRescheduleMeetClick: (Appointment) -> Unit = {},
    var onViewPaymentProofClick: ((Appointment) -> Unit)? = null,
    private val onItemClick: ((Appointment) -> Unit)? = null
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
                AppointmentStatus.PENDING -> {
                    if (appointment.rescheduleStatus == "requested") {
                        Triple(R.color.status_pending_bg, R.color.status_pending, "Reschedule Request")
                    } else {
                        Triple(R.color.status_pending_bg, R.color.status_pending, "Pending")
                    }
                }
                AppointmentStatus.ACCEPTED -> Triple(R.color.status_confirmed_bg, R.color.status_confirmed, "Accepted")
                AppointmentStatus.COMPLETED -> Triple(R.color.status_completed_bg, R.color.status_completed, "Completed")
                AppointmentStatus.CANCELLED -> Triple(R.color.status_cancelled_bg, R.color.status_cancelled, "Cancelled")
                AppointmentStatus.REJECTED -> Triple(R.color.status_rejected_bg, R.color.status_rejected, "Rejected")
                else -> Triple(R.color.status_pending_bg, R.color.status_pending, appointment.status)
            }

            binding.statusChip.text = label
            binding.statusChip.setChipBackgroundColorResource(bgRes)
            binding.statusChip.setTextColor(ctx.getColor(textRes))

            // Consultation type display
            val isOnline = appointment.consultationType == "ONLINE"
            if (isOnline) {
                binding.tvConsultationIcon.text = "📹"
                binding.tvConsultationType.text = "Online Consultation"
                binding.tvConsultationType.setTextColor(ctx.getColor(R.color.color_primary))
            } else {
                binding.tvConsultationIcon.text = "🏥"
                binding.tvConsultationType.text = "Offline Consultation"
                binding.tvConsultationType.setTextColor(ctx.getColor(R.color.color_text_secondary))
            }

            // Reset default visibilities
            binding.actionButtonsContainer.visibility = View.GONE
            binding.acceptedActionButtonsContainer.visibility = View.GONE
            binding.btnJoinGoogleMeet.visibility = View.GONE
            binding.markCompletedButton.visibility = View.VISIBLE
            binding.tvVerificationStatus.visibility = View.GONE
            binding.btnViewPaymentProof.visibility = View.GONE

            if (appointment.paymentProofUrl.isNotEmpty()) {
                binding.btnViewPaymentProof.visibility = View.VISIBLE
                binding.btnViewPaymentProof.setOnClickListener {
                    onViewPaymentProofClick?.invoke(appointment)
                }
            }

            // Show action buttons based on status
            if (appointment.status == AppointmentStatus.PENDING) {
                binding.actionButtonsContainer.visibility = View.VISIBLE
                
                binding.acceptButton.setOnClickListener { onAcceptClick(appointment) }
                binding.rejectButton.setOnClickListener { onRejectClick(appointment) }
            } else if (appointment.status == AppointmentStatus.ACCEPTED) {
                binding.acceptedActionButtonsContainer.visibility = View.VISIBLE
                
                binding.markCompletedButton.setOnClickListener { onMarkCompletedClick(appointment) }
                binding.openChatButton.setOnClickListener { onOpenChatClick(appointment) }
                binding.btnCancelMeet.setOnClickListener { onCancelMeetClick(appointment) }
                binding.btnRescheduleMeet.setOnClickListener { onRescheduleMeetClick(appointment) }

                if (isOnline) {
                    binding.btnJoinGoogleMeet.visibility = View.VISIBLE
                    binding.btnJoinGoogleMeet.setOnClickListener {
                        onJoinMeetClick(appointment)
                    }
                }

                // Handle completion verification status
                if (appointment.completionVerificationStatus == "requested") {
                    binding.markCompletedButton.visibility = View.GONE
                    binding.tvVerificationStatus.visibility = View.VISIBLE
                    binding.tvVerificationStatus.text = "Awaiting Patient Confirmation"
                    binding.tvVerificationStatus.setTextColor(ctx.getColor(R.color.color_warning))
                } else if (appointment.completionVerificationStatus == "rejected") {
                    binding.tvVerificationStatus.visibility = View.VISIBLE
                    binding.tvVerificationStatus.text = "Patient did not confirm"
                    binding.tvVerificationStatus.setTextColor(ctx.getColor(R.color.color_error))
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(appointment)
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
