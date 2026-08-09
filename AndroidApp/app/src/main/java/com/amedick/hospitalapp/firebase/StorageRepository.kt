package com.amedick.hospitalapp.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dedicated Storage repository for image upload operations.
 * Upload logic is also available via FirestoreRepository for convenience.
 */
@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadUserProfileImage(userId: String, imageUri: Uri): Result<String> =
        upload("users/$userId/profile/${UUID.randomUUID()}", imageUri)

    suspend fun uploadDoctorProfileImage(doctorId: String, imageUri: Uri): Result<String> =
        upload("doctors/$doctorId/profile/${UUID.randomUUID()}", imageUri)

    suspend fun uploadPaymentProof(patientId: String, imageUri: Uri): Result<String> =
        upload("users/$patientId/profile/payment_${UUID.randomUUID()}", imageUri)

    private suspend fun upload(path: String, imageUri: Uri): Result<String> = try {
        val reference = storage.reference.child(path)
        reference.putFile(imageUri).await()
        Result.success(reference.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
