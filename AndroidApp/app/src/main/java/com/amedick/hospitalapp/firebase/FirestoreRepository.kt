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

    suspend fun markAppointmentCompleted(appointmentId: String): Result<Boolean> = try {
        firestore.runTransaction { transaction ->
            val ref = firestore.collection("Appointments").document(appointmentId)
            val snapshot = transaction.get(ref)
            if (snapshot.exists()) {
                val currentStatus = snapshot.getString("status")
                if (currentStatus != AppointmentStatus.ACCEPTED) {
                    throw Exception("Appointment is not in ACCEPTED state.")
                }
                
                // Note: Complete time validation is handled UI-side with AppointmentUtils 
                // to prevent differences between server and local timezone, but we enforce the status flow here.
                transaction.update(ref, "status", AppointmentStatus.COMPLETED)
            } else {
                throw Exception("Appointment not found")
            }
        }.await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

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

    suspend fun getReviewForAppointment(appointmentId: String): Result<Review?> = try {
        val reviewDocId = "${appointmentId}_review"
        val snapshot = firestore.collection("Reviews").document(reviewDocId).get().await()
        if (snapshot.exists()) {
            Result.success(snapshot.toObject(Review::class.java))
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
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
            .get().await()
        val list = snapshot.documents.map { it.toNotificationSafe() }.sortedByDescending { it.createdAt }
        Result.success(list)
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

    suspend fun createNotification(userId: String, title: String, message: String, type: String, relatedId: String = ""): Result<Boolean> = try {
        val ref = firestore.collection("Notifications").document()
        val notification = Notification(
            notificationId = ref.id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            createdAt = System.currentTimeMillis()
        )
        ref.set(notification).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createAdminNotification(title: String, message: String, type: String, relatedId: String = "") {
        try {
            val adminsSnapshot = firestore.collection("Users").whereEqualTo("role", "admin").get().await()
            for (adminDoc in adminsSnapshot.documents) {
                createNotification(adminDoc.id, title, message, type, relatedId)
            }
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    fun getAdminNotificationsRealtime(userId: String): Flow<Result<List<Notification>>> = callbackFlow {
        val listener = firestore.collection("Notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val list = snapshot.documents.map { it.toNotificationSafe() }
                            .sortedByDescending { it.createdAt }
                        trySend(Result.success(list))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    // ── Admin Dashboard ────────────────────────────────────────────────────────

    fun getPendingDoctorsCountRealtime(): Flow<Result<Int>> = callbackFlow {
        val listener = firestore.collection("Users")
            .whereEqualTo("role", "doctor")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val count = snapshot.documents.count { doc ->
                        val status = doc.getString("verificationStatus")
                        status == null || status == "PENDING"
                    }
                    trySend(Result.success(count))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getPlatformStats(): Result<Map<String, Int>> = try {
        // Aggregate queries
        val patientsCount = firestore.collection("Users").whereEqualTo("role", "patient").get().await().size()
        val allDoctorsSnapshot = firestore.collection("Users").whereEqualTo("role", "doctor").get().await()
        val doctorsCount = allDoctorsSnapshot.size()
        
        var verifiedDoctors = 0
        var pendingDoctors = 0
        for (doc in allDoctorsSnapshot.documents) {
            val status = doc.getString("verificationStatus")
            if (status == "VERIFIED") verifiedDoctors++
            else if (status == "REJECTED") {} // not pending or verified
            else pendingDoctors++ // if missing or "PENDING", it's pending
        }
        
        val totalAppointments = firestore.collection("Appointments").get().await().size()
        val completedAppointments = firestore.collection("Appointments").whereEqualTo("status", "completed").get().await().size()
        
        Result.success(mapOf(
            "patients" to patientsCount,
            "doctors" to doctorsCount,
            "verifiedDoctors" to verifiedDoctors,
            "pendingDoctors" to pendingDoctors,
            "totalAppointments" to totalAppointments,
            "completedAppointments" to completedAppointments
        ))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getPendingDoctors(doctorId: String? = null): Result<List<Doctor>> = try {
        var query: com.google.firebase.firestore.Query = firestore.collection("Users")
            .whereEqualTo("role", "doctor")
            
        if (doctorId != null) {
            query = query.whereEqualTo(com.google.firebase.firestore.FieldPath.documentId(), doctorId)
        }
        val snapshot = query.get().await()
        val doctors = snapshot.toObjects(Doctor::class.java).filter { it.verificationStatus == "PENDING" }
        Result.success(doctors)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun verifyDoctor(doctorId: String, isVerified: Boolean, status: String, rejectionReason: String = ""): Result<Boolean> = try {
        val updates = mutableMapOf<String, Any>(
            "isVerified" to isVerified,
            "verificationStatus" to status,
            "verificationDate" to System.currentTimeMillis()
        )
        if (!isVerified && rejectionReason.isNotEmpty()) {
            updates["verificationRejectedReason"] = rejectionReason
        } else {
            updates["verificationRejectedReason"] = ""
        }
        firestore.collection("Users").document(doctorId).update(updates).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Doctor submits verification request.
     * Sets verificationStatus to PENDING and notifies all admins.
     */
    suspend fun submitVerificationRequest(doctorId: String, doctorName: String): Result<Boolean> = try {
        val updates = mapOf(
            "verificationStatus" to "PENDING",
            "verificationSubmittedAt" to System.currentTimeMillis()
        )
        firestore.collection("Users").document(doctorId).update(updates).await()
        // Notify admins
        createAdminNotification(
            title = "New Verification Request",
            message = "Dr. $doctorName has submitted documents for professional verification.",
            type = "doctor_verification",
            relatedId = doctorId
        )
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Admin Realtime Lists ──────────────────────────────────────────────────

    fun getPatientsRealtime(): Flow<Result<List<User>>> = callbackFlow {
        val listener = firestore.collection("Users")
            .whereEqualTo("role", "patient")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val list = snapshot.toObjects(User::class.java).sortedByDescending { it.createdAt }
                        trySend(Result.success(list))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun getDoctorsRealtime(filterType: String): Flow<Result<List<Doctor>>> = callbackFlow {
        val query: Query = firestore.collection("Users").whereEqualTo("role", "doctor")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    var list = snapshot.toObjects(Doctor::class.java)
                    if (filterType == "VERIFIED") {
                        // Single source of truth: verificationStatus. Legacy fallback: isVerified==true with missing status.
                        list = list.filter { doctor ->
                            doctor.verificationStatus == "VERIFIED" ||
                            (doctor.verificationStatus.isEmpty() && doctor.isVerified)
                        }
                    } else if (filterType == "PENDING") {
                        list = list.filter { doctor ->
                            doctor.verificationStatus == "PENDING" ||
                            (doctor.verificationStatus.isEmpty() && !doctor.isVerified)
                        }
                    }
                    list = list.sortedByDescending { it.createdAt }
                    trySend(Result.success(list))
                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Realtime listener for a single doctor's profile.
     * This is the canonical way to observe verificationStatus changes.
     */
    fun getDoctorProfileRealtime(doctorId: String): Flow<Result<User>> = callbackFlow {
        val listener = firestore.collection("Users").document(doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            // Legacy handling: if verificationStatus is missing but isVerified=true, treat as VERIFIED
                            val effectiveStatus = when {
                                user.verificationStatus == "VERIFIED" -> user
                                user.verificationStatus.isEmpty() && user.isVerified -> user.copy(verificationStatus = "VERIFIED")
                                user.verificationStatus.isEmpty() -> user.copy(verificationStatus = "PENDING")
                                else -> user
                            }
                            trySend(Result.success(effectiveStatus))
                        } else {
                            trySend(Result.failure(Exception("User not found")))
                        }
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun getAppointmentsRealtime(filterType: String): Flow<Result<List<Appointment>>> = callbackFlow {
        var query: Query = firestore.collection("Appointments")
        if (filterType == "COMPLETED") {
            query = query.whereEqualTo("status", AppointmentStatus.COMPLETED)
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    val list = snapshot.toObjects(Appointment::class.java).sortedByDescending { it.createdAt }
                    trySend(Result.success(list))
                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            }
        }
        awaitClose { listener.remove() }
    }

    fun getAppointmentsForPatientRealtime(patientId: String): Flow<Result<List<Appointment>>> = callbackFlow {
        val listener = firestore.collection("Appointments")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val list = snapshot.toObjects(Appointment::class.java).sortedByDescending { it.createdAt }
                        trySend(Result.success(list))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        awaitClose { listener.remove() }
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
    
    suspend fun uploadMedicalDocument(patientId: String, name: String, type: String, size: Long, fileUri: Uri): Result<com.amedick.hospitalapp.models.MedicalDocument> = try {
        val docRef = firestore.collection("MedicalDocuments").document()
        val storagePath = "patients/$patientId/documents/${docRef.id}_${name.replace("[^A-Za-z0-9_.-]".toRegex(), "_")}"
        val storageRef = storage.reference.child(storagePath)
        storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        val document = com.amedick.hospitalapp.models.MedicalDocument(
            documentId = docRef.id,
            patientId = patientId,
            name = name,
            type = type,
            url = downloadUrl,
            size = size,
            storagePath = storagePath
        )
        docRef.set(document).await()
        Result.success(document)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveMedicalDocumentMetadata(document: com.amedick.hospitalapp.models.MedicalDocument): Result<Boolean> = try {
        val docRef = firestore.collection("MedicalDocuments").document()
        val finalDoc = document.copy(documentId = docRef.id)
        docRef.set(finalDoc).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMedicalDocument(document: com.amedick.hospitalapp.models.MedicalDocument): Result<Boolean> = try {
        if (document.storageProvider == "firebase" && document.storagePath.isNotEmpty()) {
            try {
                val storageRef = storage.reference.child(document.storagePath)
                storageRef.delete().await()
            } catch (e: Exception) {
                // Ignore if storage file is already deleted or not found
            }
        }
        firestore.collection("MedicalDocuments").document(document.documentId).delete().await()
        Result.success(true)
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

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotificationSafe(): Notification {
        val createdAtObj = this.get("createdAt")
        val time = when (createdAtObj) {
            is Long -> createdAtObj
            is Number -> createdAtObj.toLong()
            is com.google.firebase.Timestamp -> createdAtObj.toDate().time
            is java.util.Date -> createdAtObj.time
            else -> 0L
        }
        return Notification(
            notificationId = this.id,
            userId = this.getString("userId") ?: "",
            title = this.getString("title") ?: "",
            message = this.getString("message") ?: "",
            type = this.getString("type") ?: "",
            relatedId = this.getString("relatedId") ?: "",
            isRead = this.getBoolean("isRead") ?: false,
            createdAt = time
        )
    }

    // ── Doctor Verification Documents ─────────────────────────────────────────

    suspend fun saveVerificationDocument(document: com.amedick.hospitalapp.models.DoctorVerificationDocument): Result<Boolean> = try {
        val docRef = firestore.collection("DoctorVerificationDocuments").document()
        val finalDoc = document.copy(documentId = docRef.id)
        docRef.set(finalDoc).await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getVerificationDocuments(doctorId: String): Result<List<com.amedick.hospitalapp.models.DoctorVerificationDocument>> = try {
        val snapshot = firestore.collection("DoctorVerificationDocuments")
            .whereEqualTo("doctorId", doctorId)
            .get().await()
        Result.success(snapshot.toObjects(com.amedick.hospitalapp.models.DoctorVerificationDocument::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteVerificationDocument(documentId: String): Result<Boolean> = try {
        firestore.collection("DoctorVerificationDocuments").document(documentId).delete().await()
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Realtime listener for doctor verification documents.
     * Admin uses this to watch documents appear as the doctor uploads them.
     */
    fun getVerificationDocumentsRealtime(doctorId: String): Flow<Result<List<com.amedick.hospitalapp.models.DoctorVerificationDocument>>> = callbackFlow {
        val listener = firestore.collection("DoctorVerificationDocuments")
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val docs = snapshot.toObjects(com.amedick.hospitalapp.models.DoctorVerificationDocument::class.java)
                        trySend(Result.success(docs))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Update profile image URL in Firestore Users collection.
     */
    suspend fun updateUserProfileImage(userId: String, imageUrl: String): Result<Unit> = try {
        firestore.collection("Users").document(userId)
            .update("profileImage", imageUrl).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
