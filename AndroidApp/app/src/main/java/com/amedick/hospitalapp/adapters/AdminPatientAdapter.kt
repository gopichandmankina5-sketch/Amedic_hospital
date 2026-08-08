package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemAdminPatientBinding
import com.amedick.hospitalapp.models.User
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class AdminPatientAdapter(
    private var patients: List<User>,
    private val onPatientClick: (User) -> Unit
) : RecyclerView.Adapter<AdminPatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(private val binding: ItemAdminPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: User) {
            binding.tvPatientName.text = patient.name.ifEmpty { "Unknown Patient" }
            binding.tvPatientEmail.text = patient.email.ifEmpty { "No Email" }
            binding.tvPatientPhone.text = patient.phone.ifEmpty { "No Phone" }

            if (patient.profileImage.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(patient.profileImage)
                    .placeholder(com.amedick.hospitalapp.R.drawable.ic_hospital)
                    .error(com.amedick.hospitalapp.R.drawable.ic_hospital)
                    .centerCrop()
                    .into(binding.ivPatientProfile)
            } else {
                binding.ivPatientProfile.setImageResource(com.amedick.hospitalapp.R.drawable.ic_hospital)
            }

            binding.root.setOnClickListener { onPatientClick(patient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemAdminPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int = patients.size

    fun updateData(newPatients: List<User>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = patients.size
            override fun getNewListSize() = newPatients.size
            override fun areItemsTheSame(old: Int, new: Int) =
                patients[old].uid == newPatients[new].uid
            override fun areContentsTheSame(old: Int, new: Int) =
                patients[old] == newPatients[new]
        }
        val result = DiffUtil.calculateDiff(diffCallback)
        patients = newPatients
        result.dispatchUpdatesTo(this)
    }
}
