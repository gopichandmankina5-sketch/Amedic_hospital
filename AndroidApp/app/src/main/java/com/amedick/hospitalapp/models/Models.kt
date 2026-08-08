package com.amedick.hospitalapp.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String? = null, // "patient", "doctor", "admin"
    val profileImage: String = "",
    val fcmToken: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

@Parcelize
data class Doctor(
    @DocumentId val doctorId: String = "",
    val name: String = "",
    val specialization: String = "",
    val hospital: String = "",
    val qualification: String = "",
    val about: String = "",
    val experience: Int = 0,
    val rating: Float = 0f,
    val image: String = "",
    val email: String = "",
    val phone: String = "",
    val available: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null
) : Parcelable

data class Appointment(
    @DocumentId val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "",
    val reason: String = "",
    val status: String = AppointmentStatus.PENDING,
    @ServerTimestamp val createdAt: Date? = null
)

object AppointmentStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
    const val REJECTED = "REJECTED"
}

data class Notification(
    @DocumentId val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)
