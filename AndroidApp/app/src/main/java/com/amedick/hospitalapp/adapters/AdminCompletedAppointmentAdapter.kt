package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemAdminCompletedAppointmentBinding
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.Review

class AdminCompletedAppointmentAdapter(
    private var appointments: List<Appointment>,
    private var reviewsMap: Map<String, Review?>
) : RecyclerView.Adapter<AdminCompletedAppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminCompletedAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.tvDoctorName.text = if (appointment.doctorName.isNotEmpty())
                "Dr. ${appointment.doctorName}" else "Unknown Doctor"
            
            // Note: Specialization might not be in appointment model, just leaving it empty or hidden for now
            binding.tvSpecialization.visibility = View.GONE
            
            binding.tvPatientName.text = if (appointment.patientName.isNotEmpty())
                appointment.patientName else "Unknown Patient"
            
            binding.tvDate.text = appointment.date.ifEmpty { "Date TBD" }
            binding.tvTime.text = appointment.time.ifEmpty { "Time TBD" }

            if (appointment.reason.isNotEmpty()) {
                binding.tvReason.text = "Reason: ${appointment.reason}"
                binding.tvReason.visibility = View.VISIBLE
            } else {
                binding.tvReason.visibility = View.GONE
            }

            // Rating / Review Section
            val review = reviewsMap[appointment.appointmentId]
            if (review != null) {
                binding.tvNotRated.visibility = View.GONE
                binding.ratingContentLayout.visibility = View.VISIBLE
                
                binding.ratingBar.rating = review.rating
                binding.tvRatingValue.text = "${review.rating}/5"
                
                if (review.feedback.isNotEmpty()) {
                    binding.tvReviewComment.text = "\"${review.feedback}\""
                    binding.tvReviewComment.visibility = View.VISIBLE
                } else {
                    binding.tvReviewComment.visibility = View.GONE
                }
            } else {
                binding.ratingContentLayout.visibility = View.GONE
                binding.tvNotRated.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminCompletedAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    fun updateData(newAppointments: List<Appointment>, newReviewsMap: Map<String, Review?>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = appointments.size
            override fun getNewListSize() = newAppointments.size
            override fun areItemsTheSame(old: Int, new: Int) =
                appointments[old].appointmentId == newAppointments[new].appointmentId
            override fun areContentsTheSame(old: Int, new: Int) =
                appointments[old] == newAppointments[new] && reviewsMap[appointments[old].appointmentId] == newReviewsMap[newAppointments[new].appointmentId]
        }
        val result = DiffUtil.calculateDiff(diffCallback)
        appointments = newAppointments
        reviewsMap = newReviewsMap
        result.dispatchUpdatesTo(this)
    }
}
