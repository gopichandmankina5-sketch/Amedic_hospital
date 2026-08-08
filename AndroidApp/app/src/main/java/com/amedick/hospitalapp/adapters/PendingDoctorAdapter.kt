package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemPendingDoctorBinding
import com.amedick.hospitalapp.models.Doctor
import com.bumptech.glide.Glide

class PendingDoctorAdapter(
    private var doctors: List<Doctor>,
    private val onItemClick: (Doctor) -> Unit
) : RecyclerView.Adapter<PendingDoctorAdapter.PendingViewHolder>() {

    inner class PendingViewHolder(private val binding: ItemPendingDoctorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doctor: Doctor) {
            binding.tvDoctorName.text = doctor.name.ifEmpty { "Dr. Unknown" }
            binding.tvSpecialization.text = doctor.specialization
            binding.tvRegistrationNumber.text = "Med Reg No: ${doctor.medicalRegistrationNumber.ifEmpty { "Not Provided" }}"
            binding.tvExperience.text = "Experience: ${doctor.experience} years"
            binding.tvQualification.text = "Qualification: ${doctor.qualification}"
            
            if (doctor.image.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(doctor.image)
                    .placeholder(com.amedick.hospitalapp.R.drawable.ic_doctor)
                    .into(binding.ivDoctorImage)
            } else {
                binding.ivDoctorImage.setImageResource(com.amedick.hospitalapp.R.drawable.ic_doctor)
            }
            
            binding.root.setOnClickListener {
                onItemClick(doctor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val binding = ItemPendingDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount() = doctors.size

    fun updateData(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }
}
