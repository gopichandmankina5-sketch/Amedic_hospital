package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.R
import com.amedick.hospitalapp.databinding.ItemMedicalDocumentBinding
import com.amedick.hospitalapp.models.MedicalDocument
import java.text.SimpleDateFormat
import java.util.Locale

class MedicalDocumentAdapter(
    private var documents: List<MedicalDocument>,
    private val onViewClick: (MedicalDocument) -> Unit,
    private val onDeleteClick: (MedicalDocument) -> Unit
) : RecyclerView.Adapter<MedicalDocumentAdapter.DocumentViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class DocumentViewHolder(private val binding: ItemMedicalDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(document: MedicalDocument) {
            binding.tvDocumentName.text = document.name
            
            // Format size
            val sizeStr = if (document.size > 0) {
                if (document.size > 1024 * 1024) {
                    String.format(Locale.US, "%.1f MB", document.size / (1024f * 1024f))
                } else {
                    String.format(Locale.US, "%.1f KB", document.size / 1024f)
                }
            } else {
                "Unknown size"
            }
            
            binding.tvDocumentType.text = "${document.type} • $sizeStr"
            
            if (document.uploadedAt != null) {
                binding.tvUploadDate.text = "Uploaded: ${dateFormat.format(document.uploadedAt)}"
            } else {
                binding.tvUploadDate.text = "Just now"
            }

            // Set dynamic icon
            val isPdf = document.type.contains("PDF", ignoreCase = true) || document.name.endsWith(".pdf", ignoreCase = true)
            val isImage = document.type.contains("Image", ignoreCase = true) || document.type.contains("JPG", ignoreCase = true) || document.type.contains("PNG", ignoreCase = true)
            
            if (isPdf) {
                binding.ivDocumentIcon.setImageResource(R.drawable.ic_file_document)
                binding.ivDocumentIcon.setColorFilter(binding.root.context.getColor(R.color.color_error)) // Red for PDF
                binding.ivDocumentIcon.setBackgroundResource(R.drawable.bg_circle_primary_light) // You can change this to a red circle background if you have one
            } else if (isImage) {
                binding.ivDocumentIcon.setImageResource(R.drawable.ic_image)
                binding.ivDocumentIcon.setColorFilter(binding.root.context.getColor(R.color.color_primary)) // Blue for images
            } else {
                binding.ivDocumentIcon.setImageResource(R.drawable.ic_file_document)
                binding.ivDocumentIcon.setColorFilter(binding.root.context.getColor(R.color.color_text_secondary)) // Gray for others
            }

            binding.btnViewDocument.setOnClickListener {
                onViewClick(document)
            }
            
            binding.btnDeleteDocument.setOnClickListener {
                onDeleteClick(document)
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
