package com.amedick.hospitalapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemVerificationDocumentBinding
import com.amedick.hospitalapp.models.DoctorVerificationDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VerificationDocumentAdapter(
    private var documents: List<DoctorVerificationDocument>,
    private val onViewClick: (DoctorVerificationDocument) -> Unit,
    private val onDownloadClick: (DoctorVerificationDocument) -> Unit
) : RecyclerView.Adapter<VerificationDocumentAdapter.DocViewHolder>() {

    inner class DocViewHolder(private val binding: ItemVerificationDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(doc: DoctorVerificationDocument) {
            binding.tvDocumentType.text = documentTypeLabel(doc.documentType)
            binding.tvFileName.text = doc.fileName.ifEmpty { "Document" }

            // File type badge
            val ext = when {
                doc.fileType.contains("pdf", ignoreCase = true) -> "PDF"
                doc.fileType.contains("png", ignoreCase = true) -> "PNG"
                doc.fileType.contains("jpeg", ignoreCase = true) || doc.fileType.contains("jpg", ignoreCase = true) -> "JPG"
                doc.fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                doc.fileName.endsWith(".png", ignoreCase = true) -> "PNG"
                doc.fileName.endsWith(".jpg", ignoreCase = true) || doc.fileName.endsWith(".jpeg", ignoreCase = true) -> "JPG"
                else -> "DOC"
            }
            binding.tvFileType.text = ext

            // Icon based on type
            val iconRes = if (ext == "PDF") {
                com.amedick.hospitalapp.R.drawable.ic_file_document
            } else {
                com.amedick.hospitalapp.R.drawable.ic_image
            }
            binding.ivDocIcon.setImageResource(iconRes)

            // File size
            binding.tvFileSize.text = if (doc.size > 0) formatSize(doc.size) else "—"

            // Upload date
            val dateStr = try {
                // Try to get date from Firestore timestamp via the Date field
                doc.uploadedAt?.let {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                } ?: "—"
            } catch (e: Exception) { "—" }
            binding.tvUploadDate.text = if (dateStr != "—") "• $dateStr" else ""

            binding.btnView.setOnClickListener { onViewClick(doc) }
            binding.btnDownload.setOnClickListener { onDownloadClick(doc) }
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
                bytes >= 1024 -> "%.0f KB".format(bytes / 1024.0)
                else -> "$bytes B"
            }
        }

        private fun documentTypeLabel(type: String): String = when (type) {
            "MEDICAL_REGISTRATION" -> "Medical Registration Certificate"
            "MEDICAL_DEGREE" -> "Medical Degree Certificate"
            "GOVERNMENT_ID" -> "Government ID"
            "EXPERIENCE_CERTIFICATE" -> "Experience Certificate"
            else -> type.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val binding = ItemVerificationDocumentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DocViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount() = documents.size

    fun updateData(newDocs: List<DoctorVerificationDocument>) {
        documents = newDocs
        notifyDataSetChanged()
    }
}
