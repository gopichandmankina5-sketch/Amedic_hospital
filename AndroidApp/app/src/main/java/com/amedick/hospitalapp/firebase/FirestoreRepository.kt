package com.amedick.hospitalapp.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus
import com.amedick.hospitalapp.models.Doctor
import com.amedick.hospitalapp.models.Notification
import com.amedick.hospitalapp.models.User
import java.util.UUID

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    // ── Doctors ──────────────────────────────────────────────────────────────

    suspend fun getDoctors(): Result<List<Doctor>> = try {
        val snapshot = firestore.collection("Users")
            .whereEqualTo("role", "doctor")
            .get().await()
        Result.success(snapshot.toObjects(Doctor::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDoctorById(doctorId: String): Result<Doctor> = try {
        val doc = firestore.collection("Users").document(doctorId).get().await()
        val doctor = doc.toObject(Doctor::class.java) ?: throw Exception("Doctor not found")
        Result.success(doctor)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── User Profile ──────────────────────────────────────────────────────────

    suspend fun getUserProfile(userId: String): Result<User> = try {
        val doc = firestore.collection("Users").document(userId).get().await()
        val user = doc.toObject(User::class.java) ?: throw Exception("User not found")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserProfile(user: User): Result<Unit> = try {
        val updates = mapOf(
            "name" to user.name,
            "phone" to user.phone,
            "profileImage" to user.profileImage
        )
        firestore.collection("Users").document(user.uid).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    suspend fun bookAppointment(appointment: Appointment): Result<Boolean> = try {
        val docRef = firestore.collection("Appointments").document()
        val newAppt = appointment.copy(appointmentId = docRef.id)
        docRef.set(newAppt).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAppointmentsForPatient(patientId: String): Result<List<Appointment>> = try {
        val snapshot = firestore.collection("Appointments")
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snapshot.toObjects(Appointment::class.java))
    } catch (e: Exception) {
        // fallback without ordering if index missing
        try {
            val snapshot = firestore.collection("Appointments")
                .whereEqualTo("patientId", patientId)
                .get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e2: Exception) {
            Result.failure(e2)
        }
    }

    suspend fun getAppointmentsForDoctor(doctorId: String): Result<List<Appointment>> = try {
        val snapshot = firestore.collection("Appointments")
            .whereEqualTo("doctorId", doctorId)
            .get().await()
        Result.success(snapshot.toObjects(Appointment::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Boolean> = try {
        firestore.collection("Appointments").document(appointmentId)
            .update("status", status).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelAppointment(appointmentId: String): Result<Boolean> =
        updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED)

    // ── Notifications ──────────────────────────────────────────────────────────

    suspend fun getNotificationsForUser(userId: String): Result<List<Notification>> = try {
        val snapshot = firestore.collection("Notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snapshot.toObjects(Notification::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = try {
        firestore.collection("Notifications").document(notificationId)
            .update("isRead", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    suspend fun uploadUserProfileImage(userId: String, imageUri: Uri): Result<String> =
        uploadImage("users/$userId/profile/${UUID.randomUUID()}", imageUri)

    suspend fun uploadDoctorProfileImage(doctorId: String, imageUri: Uri): Result<String> =
        uploadImage("doctors/$doctorId/profile/${UUID.randomUUID()}", imageUri)

    private suspend fun uploadImage(path: String, imageUri: Uri): Result<String> = try {
        val reference = storage.reference.child(path)
        reference.putFile(imageUri).await()
        Result.success(reference.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
