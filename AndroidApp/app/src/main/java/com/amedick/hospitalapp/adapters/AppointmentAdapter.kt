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
    private val onCancelClick: (Appointment) -> Unit,
    private val onOpenChatClick: (Appointment) -> Unit,
    private val onRateClick: (Appointment) -> Unit,
    private val onJoinMeetClick: (Appointment) -> Unit,
    private val onRescheduleClick: (Appointment) -> Unit,
    private val onVerifyCompletionClick: (Appointment, Boolean) -> Unit,
    private val onItemClick: ((Appointment) -> Unit)? = null
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
                binding.tvConsultationType.text = "Hospital Visit"
                binding.tvConsultationType.setTextColor(ctx.getColor(R.color.color_text_secondary))
            }

            // Action Buttons
            val canCancel = appointment.status == AppointmentStatus.PENDING ||
                    appointment.status == AppointmentStatus.ACCEPTED
            binding.cancelButton.visibility = if (canCancel) View.VISIBLE else View.GONE
            binding.cancelButton.setOnClickListener { onCancelClick(appointment) }

            binding.openChatButton.visibility = if (appointment.status == AppointmentStatus.ACCEPTED) View.VISIBLE else View.GONE
            binding.openChatButton.setOnClickListener { onOpenChatClick(appointment) }

            if (isOnline && appointment.status == AppointmentStatus.ACCEPTED) {
                binding.btnJoinGoogleMeet.visibility = View.VISIBLE
                binding.btnJoinGoogleMeet.setOnClickListener {
                    onJoinMeetClick(appointment)
                }
            } else {
                binding.btnJoinGoogleMeet.visibility = View.GONE
            }

            if (appointment.status == AppointmentStatus.COMPLETED) {
                binding.rateButton.visibility = View.VISIBLE
                if (appointment.isRated) {
                    binding.rateButton.text = "Feedback Given"
                    binding.rateButton.isEnabled = false
                } else {
                    binding.rateButton.text = "Rate Experience"
                    binding.rateButton.isEnabled = true
                    binding.rateButton.setOnClickListener { onRateClick(appointment) }
                }
            } else {
                binding.rateButton.visibility = View.GONE
            }

            // Reschedule logic explicitly reset
            binding.btnReschedule.visibility = View.GONE
            binding.btnRescheduleRequested.visibility = View.GONE

            // Verification Request UI explicitly reset
            binding.layoutVerificationRequest.visibility = View.GONE
            binding.btnVerifyYes.setOnClickListener(null)
            binding.btnVerifyNo.setOnClickListener(null)

            if (appointment.status == AppointmentStatus.ACCEPTED) {
                if (appointment.completionVerificationStatus == "requested") {
                    binding.layoutVerificationRequest.visibility = View.VISIBLE
                    binding.tvVerificationMsg.text = "Doctor requested consultation completion confirmation."
                    if (appointment.earlyCompletionReason.isNotEmpty()) {
                        binding.tvVerificationMsg.text = "Doctor requested early completion confirmation.\nReason: ${appointment.earlyCompletionReason}"
                    }
                    binding.tvVerificationMsg.setTextColor(ctx.getColor(R.color.color_warning))

                    binding.btnVerifyYes.setOnClickListener { onVerifyCompletionClick(appointment, true) }
                    binding.btnVerifyNo.setOnClickListener { onVerifyCompletionClick(appointment, false) }
                } else if (appointment.completionVerificationStatus == "rejected") {
                    binding.layoutVerificationRequest.visibility = View.VISIBLE
                    binding.tvVerificationMsg.text = "You did not confirm consultation completion."
                    binding.tvVerificationMsg.setTextColor(ctx.getColor(R.color.color_error))
                    // Hide the yes/no buttons when rejected
                    binding.btnVerifyYes.visibility = View.GONE
                    binding.btnVerifyNo.visibility = View.GONE
                } else {
                    // Not requested/rejected verification, show reschedule logic
                    if (appointment.rescheduleStatus == "none") {
                        binding.btnReschedule.visibility = View.VISIBLE
                        binding.btnReschedule.setOnClickListener { onRescheduleClick(appointment) }
                    } else if (appointment.rescheduleStatus == "requested") {
                        binding.btnRescheduleRequested.visibility = View.VISIBLE
                    }
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(appointment)
            }
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
