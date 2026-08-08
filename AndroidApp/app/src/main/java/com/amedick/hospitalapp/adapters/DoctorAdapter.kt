package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemDoctorBinding
import com.amedick.hospitalapp.models.Doctor
import com.bumptech.glide.Glide

class DoctorAdapter(
    private var doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(private val binding: ItemDoctorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.doctorName.text = doctor.name.ifEmpty { "Unknown Doctor" }
            binding.doctorSpecialization.text = doctor.specialization.ifEmpty { "General Practitioner" }
            binding.doctorRating.text = String.format("%.1f", doctor.rating)
            binding.doctorExperience.text = if (doctor.experience > 0) "${doctor.experience} yrs exp" else ""

            // Availability
            if (doctor.available) {
                binding.availabilityChip.text = "Available"
                binding.availabilityChip.setChipBackgroundColorResource(
                    com.amedick.hospitalapp.R.color.status_confirmed_bg
                )
                binding.availabilityChip.setTextColor(
                    binding.root.context.getColor(com.amedick.hospitalapp.R.color.status_confirmed)
                )
            } else {
                binding.availabilityChip.text = "Unavailable"
                binding.availabilityChip.setChipBackgroundColorResource(
                    com.amedick.hospitalapp.R.color.status_cancelled_bg
                )
                binding.availabilityChip.setTextColor(
                    binding.root.context.getColor(com.amedick.hospitalapp.R.color.status_cancelled)
                )
            }

            // Load image
            if (doctor.image.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(doctor.image)
                    .placeholder(com.amedick.hospitalapp.R.drawable.ic_doctor)
                    .error(com.amedick.hospitalapp.R.drawable.ic_doctor)
                    .centerCrop()
                    .into(binding.doctorImage)
            } else {
                binding.doctorImage.setImageResource(com.amedick.hospitalapp.R.drawable.ic_doctor)
            }

            binding.root.setOnClickListener { onDoctorClick(doctor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount(): Int = doctors.size

    fun updateData(newDoctors: List<Doctor>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = doctors.size
            override fun getNewListSize() = newDoctors.size
            override fun areItemsTheSame(old: Int, new: Int) =
                doctors[old].doctorId == newDoctors[new].doctorId
            override fun areContentsTheSame(old: Int, new: Int) =
                doctors[old] == newDoctors[new]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        doctors = newDoctors
        diffResult.dispatchUpdatesTo(this)
    }
}
