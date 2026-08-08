package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemMedicalDocumentBinding
import com.amedick.hospitalapp.models.MedicalDocument
import java.text.SimpleDateFormat
import java.util.Locale

class MedicalDocumentAdapter(
    private var documents: List<MedicalDocument>,
    private val onViewClick: (MedicalDocument) -> Unit
) : RecyclerView.Adapter<MedicalDocumentAdapter.DocumentViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class DocumentViewHolder(private val binding: ItemMedicalDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(document: MedicalDocument) {
            binding.tvDocumentName.text = document.name
            binding.tvDocumentType.text = document.type
            
            if (document.uploadedAt != null) {
                binding.tvUploadDate.text = "Uploaded: ${dateFormat.format(document.uploadedAt)}"
            } else {
                binding.tvUploadDate.text = "Just now"
            }

            binding.btnViewDocument.setOnClickListener {
                onViewClick(document)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemMedicalDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount() = documents.size

    fun updateData(newDocuments: List<MedicalDocument>) {
        documents = newDocuments
        notifyDataSetChanged()
    }
}
