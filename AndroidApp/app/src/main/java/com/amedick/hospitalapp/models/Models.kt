package com.amedick.hospitalapp.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.PropertyName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

data class User(
    @DocumentId var uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String? = null, // "patient", "doctor", "admin"
    val profileImage: String = "",
    val fcmToken: String = "",
    
    // Doctor specific fields
    val specialization: String = "",
    val hospital: String = "",
    val qualification: String = "",
    val experience: Int = 0,
    val consultationFee: Double = 0.0,
    val location: String = "",
    val about: String = "",
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val isVerified: Boolean = false,
    val available: Boolean = true,
    val googleMeetLink: String = "",
    
    // Phase 5 Additions
    val medicalRegistrationNumber: String = "",
    val verificationStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val verificationDate: Long = 0L,
    val verificationRejectedReason: String = "",
    val verificationSubmittedAt: Long = 0L,
    
    @ServerTimestamp val createdAt: Date? = null
)

@Parcelize
data class Doctor(
    @DocumentId var doctorId: String = "",
    val name: String = "",
    val specialization: String = "",
    val hospital: String = "",
    val qualification: String = "",
    val about: String = "",
    val experience: Int = 0,
    val consultationFee: Double = 0.0,
    val location: String = "",
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val image: String = "",
    val email: String = "",
    val phone: String = "",
    val available: Boolean = true,
    val isVerified: Boolean = false,
    val googleMeetLink: String = "",
    val role: String = "doctor",
    
    // Phase 5 Additions
    val medicalRegistrationNumber: String = "",
    val verificationStatus: String = "PENDING",
    val verificationDate: Long = 0L,
    val verificationRejectedReason: String = "",
    
    @ServerTimestamp val createdAt: Date? = null
) : Parcelable

/**
 * A document uploaded by a doctor for professional verification.
 * Stored in: Firestore collection "DoctorVerificationDocuments"
 * documentType: MEDICAL_REGISTRATION | MEDICAL_DEGREE | GOVERNMENT_ID | EXPERIENCE_CERTIFICATE
 */
data class DoctorVerificationDocument(
    @DocumentId var documentId: String = "",
    val doctorId: String = "",
    val documentType: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val size: Long = 0L,
    @ServerTimestamp val uploadedAt: Date? = null
)

data class Appointment(
    @DocumentId var appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "",
    val reason: String = "",
    val status: String = AppointmentStatus.PENDING,
    val patientMessage: String = "",
    val rejectionReason: String = "",
    val documents: List<String> = emptyList(),
    
    // Video consultation additions
    val consultationType: String = "OFFLINE", // ONLINE, OFFLINE
    val videoRoomId: String = "",
    val videoRoomUrl: String = "",
    val onlineMeetingStarted: Boolean = false,
    val meetingStartedAt: Long = 0L,
    val meetingEndedAt: Long = 0L,

    // Google Meet additions
    val meetingProvider: String = "",
    val meetingUri: String = "",
    val meetingCode: String = "",
    val meetingCreatedAt: Long = 0L,
    val meetingStatus: String = "Not Created",

    // Ratings
    @get:PropertyName("isRated")
    @set:PropertyName("isRated")
    var isRated: Boolean = false,
    
    // Rescheduling state (none, requested, completed)
    val rescheduleStatus: String = "none",

    // Completion Verification Workflow
    val completionVerificationStatus: String = "none",
    val earlyCompletionReason: String = "",
    val completionVerificationRequestedAt: Long = 0L,
    val completionVerificationAt: Long = 0L,
    val completionVerifiedBy: String = "",
    val completedAt: Long = 0L,
    val completedBy: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

object AppointmentStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val REJECTED = "rejected"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
    const val RESCHEDULE_REQUESTED = "reschedule_requested"
}

data class Notification(
    @DocumentId var notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

data class Availability(
    @DocumentId var doctorId: String = "",
    // Map of day of week (e.g. "Monday") to list of time slots (e.g. "09:00 AM - 09:30 AM")
    val schedule: Map<String, List<String>> = emptyMap(),
    // List of specific blocked dates (e.g. "2026-08-15")
    val blockedDates: List<String> = emptyList(),
    // Configurable slot duration in minutes
    val slotDuration: Int = 30
)

data class ChatMessage(
    @DocumentId var messageId: String = "",
    val appointmentId: String = "",
    val senderId: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val timestamp: Date? = null
)

data class Review(
    @DocumentId var reviewId: String = "",
    val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val rating: Float = 0f,
    val feedback: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

// Phase 5 Models
data class MedicalProfile(
    @DocumentId var patientId: String = "",
    val bloodGroup: String = "",
    val allergies: String = "",
    val currentMedications: String = "",
    val medicalHistory: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    @ServerTimestamp val updatedAt: Date? = null
)

data class MedicalDocument(
    @DocumentId var documentId: String = "",
    val patientId: String = "",
    val name: String = "",
    val type: String = "", // e.g., "Prescription", "Lab Report"
    val url: String = "",
    val size: Long = 0L,
    val storagePath: String = "",
    val storageProvider: String = "firebase",
    @ServerTimestamp val uploadedAt: Date? = null
)
