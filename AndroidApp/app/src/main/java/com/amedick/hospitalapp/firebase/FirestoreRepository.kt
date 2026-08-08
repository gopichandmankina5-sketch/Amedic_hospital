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
import com.amedick.hospitalapp.models.Availability
import com.amedick.hospitalapp.models.Doctor
import com.amedick.hospitalapp.models.Notification
import com.amedick.hospitalapp.models.User
import com.amedick.hospitalapp.models.ChatMessage
import com.amedick.hospitalapp.models.Review
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    // ── Doctors ──────────────────────────────────────────────────────────────

    suspend fun getDoctorDetails(doctorId: String): Result<com.amedick.hospitalapp.models.Doctor> = try {
        val doc = firestore.collection("Users").document(doctorId).get().await()
        if (doc.exists() && doc.getString("role") == "doctor") {
            val doctor = doc.toObject(com.amedick.hospitalapp.models.Doctor::class.java)
            if (doctor != null) {
                Result.success(doctor)
            } else {
                Result.failure(Exception("Doctor data is null"))
            }
        } else {
            Result.failure(Exception("Doctor not found or not a doctor"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

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

    suspend fun saveDoctorAvailability(availability: Availability): Result<Boolean> = try {
        firestore.collection("Availability").document(availability.doctorId)
            .set(availability).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDoctorAvailability(doctorId: String): Result<Availability> = try {
        val doc = firestore.collection("Availability").document(doctorId).get().await()
        if (doc.exists()) {
            val availability = doc.toObject(Availability::class.java) ?: Availability(doctorId = doctorId)
            Result.success(availability)
        } else {
            Result.success(Availability(doctorId = doctorId))
        }
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
        // Deterministic ID for atomic double-booking prevention
        val safeDate = appointment.date.replace("/", "").replace("-", "")
        val safeTime = appointment.time.replace(":", "").replace(" ", "")
        val slotDocId = "${appointment.doctorId}_${safeDate}_${safeTime}"
        
        val slotRef = firestore.collection("Appointments").document(slotDocId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(slotRef)
            if (snapshot.exists()) {
                val status = snapshot.getString("status")
                if (status != AppointmentStatus.CANCELLED && status != AppointmentStatus.REJECTED) {
                    throw Exception("This time slot has just been booked. Please select another time.")
                }
            }
            val newAppt = appointment.copy(appointmentId = slotDocId)
            transaction.set(slotRef, newAppt)
        }.await()
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

    suspend fun markAppointmentCompleted(appointmentId: String): Result<Boolean> =
        updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)

    // ── Chat ─────────────────────────────────────────────────────────────────

    fun getChatMessages(appointmentId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        val listener = firestore.collection("Appointments")
            .document(appointmentId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(Result.success(messages))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(message: ChatMessage): Result<Boolean> = try {
        val ref = firestore.collection("Appointments")
            .document(message.appointmentId)
            .collection("messages")
            .document()
        
        val msgToSave = message.copy(messageId = ref.id)
        ref.set(msgToSave).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Reviews ──────────────────────────────────────────────────────────────

    suspend fun getDoctorReviews(doctorId: String): Result<List<Review>> = try {
        val snapshot = firestore.collection("Reviews")
            .whereEqualTo("doctorId", doctorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snapshot.toObjects(Review::class.java))
    } catch (e: Exception) {
        // Fallback without ordering
        try {
            val snapshot = firestore.collection("Reviews")
                .whereEqualTo("doctorId", doctorId)
                .get().await()
            Result.success(snapshot.toObjects(Review::class.java))
        } catch (e2: Exception) {
            Result.failure(e2)
        }
    }

    suspend fun submitReview(review: Review): Result<Boolean> = try {
        val reviewDocId = "${review.appointmentId}_review"
        val reviewRef = firestore.collection("Reviews").document(reviewDocId)
        val doctorRef = firestore.collection("Users").document(review.doctorId)
        
        firestore.runTransaction { transaction ->
            // Check if already reviewed
            val existingReview = transaction.get(reviewRef)
            if (existingReview.exists()) {
                throw Exception("Review already submitted for this appointment.")
            }
            
            // Get current doctor stats
            val doctorDoc = transaction.get(doctorRef)
            if (doctorDoc.exists()) {
                val currentRating = doctorDoc.getDouble("rating") ?: 0.0
                val currentCount = doctorDoc.getLong("reviewsCount")?.toInt() ?: 0
                
                // Calculate new rating
                val newCount = currentCount + 1
                val newTotalScore = (currentRating * currentCount) + review.rating
                val newRating = newTotalScore / newCount
                
                // Update doctor
                transaction.update(doctorRef, "rating", newRating)
                transaction.update(doctorRef, "reviewsCount", newCount)
            }
            
            // Save the review
            val finalReview = review.copy(reviewId = reviewDocId)
            transaction.set(reviewRef, finalReview)
        }.await()
        
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

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

    suspend fun createNotification(userId: String, title: String, message: String, type: String): Result<Boolean> = try {
        val ref = firestore.collection("Notifications").document()
        val notification = Notification(
            notificationId = ref.id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            createdAt = System.currentTimeMillis()
        )
        ref.set(notification).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Admin Dashboard ────────────────────────────────────────────────────────

    suspend fun getPlatformStats(): Result<Map<String, Int>> = try {
        // Aggregate queries
        val patientsCount = firestore.collection("Users").whereEqualTo("role", "patient").get().await().size()
        val doctorsCount = firestore.collection("Users").whereEqualTo("role", "doctor").get().await().size()
        val verifiedDoctors = firestore.collection("Users").whereEqualTo("role", "doctor").whereEqualTo("verificationStatus", "VERIFIED").get().await().size()
        val pendingDoctors = firestore.collection("Users").whereEqualTo("role", "doctor").whereEqualTo("verificationStatus", "PENDING").get().await().size()
        val totalAppointments = firestore.collection("Appointments").get().await().size()
        
        Result.success(mapOf(
            "patients" to patientsCount,
            "doctors" to doctorsCount,
            "verifiedDoctors" to verifiedDoctors,
            "pendingDoctors" to pendingDoctors,
            "totalAppointments" to totalAppointments
        ))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getPendingDoctors(): Result<List<com.amedick.hospitalapp.models.Doctor>> = try {
        val snapshot = firestore.collection("Users")
            .whereEqualTo("role", "doctor")
            .whereEqualTo("verificationStatus", "PENDING")
            .get().await()
        Result.success(snapshot.toObjects(com.amedick.hospitalapp.models.Doctor::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun verifyDoctor(doctorId: String, isVerified: Boolean, status: String): Result<Boolean> = try {
        val updates = mapOf(
            "isVerified" to isVerified,
            "verificationStatus" to status,
            "verificationDate" to System.currentTimeMillis()
        )
        firestore.collection("Users").document(doctorId).update(updates).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Medical Profile & Documents ──────────────────────────────────────────

    suspend fun getMedicalProfile(patientId: String): Result<com.amedick.hospitalapp.models.MedicalProfile> = try {
        val doc = firestore.collection("MedicalProfiles").document(patientId).get().await()
        if (doc.exists()) {
            Result.success(doc.toObject(com.amedick.hospitalapp.models.MedicalProfile::class.java) ?: com.amedick.hospitalapp.models.MedicalProfile(patientId = patientId))
        } else {
            Result.success(com.amedick.hospitalapp.models.MedicalProfile(patientId = patientId))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveMedicalProfile(profile: com.amedick.hospitalapp.models.MedicalProfile): Result<Boolean> = try {
        firestore.collection("MedicalProfiles").document(profile.patientId).set(profile).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMedicalDocuments(patientId: String): Result<List<com.amedick.hospitalapp.models.MedicalDocument>> = try {
        val snapshot = firestore.collection("MedicalDocuments")
            .whereEqualTo("patientId", patientId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snapshot.toObjects(com.amedick.hospitalapp.models.MedicalDocument::class.java))
    } catch (e: Exception) {
        try {
            val snapshot = firestore.collection("MedicalDocuments")
                .whereEqualTo("patientId", patientId)
                .get().await()
            Result.success(snapshot.toObjects(com.amedick.hospitalapp.models.MedicalDocument::class.java))
        } catch (e2: Exception) {
            Result.failure(e2)
        }
    }
    
    suspend fun uploadMedicalDocument(patientId: String, name: String, type: String, fileUri: Uri): Result<com.amedick.hospitalapp.models.MedicalDocument> = try {
        val docRef = firestore.collection("MedicalDocuments").document()
        val storageRef = storage.reference.child("patients/$patientId/documents/${docRef.id}")
        storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        val document = com.amedick.hospitalapp.models.MedicalDocument(
            documentId = docRef.id,
            patientId = patientId,
            name = name,
            type = type,
            url = downloadUrl
        )
        docRef.set(document).await()
        Result.success(document)
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
