package com.amedick.hospitalapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.amedick.hospitalapp.models.User

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User not found")
            val userDoc = firestore.collection("Users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: User(uid = uid, email = result.user?.email.orEmpty()).also {
                    firestore.collection("Users").document(uid).set(it).await()
                }
            runCatching { saveFcmToken(uid) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseAuthError(e)))
        }
    }

    suspend fun register(user: User, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed")
            val newUser = user.copy(uid = uid)
            firestore.collection("Users").document(uid).set(newUser).await()
            runCatching { saveFcmToken(uid) }
            result.user?.sendEmailVerification()?.await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseAuthError(e)))
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapFirebaseAuthError(e)))
    }

    suspend fun sendEmailVerification(): Result<Unit> = try {
        val user = auth.currentUser ?: throw IllegalStateException("No signed-in user")
        user.sendEmailVerification().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified == true
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Not signed in")
        user.updatePassword(newPassword).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapFirebaseAuthError(e)))
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun isLoggedIn(): Boolean = auth.currentUser != null

    private suspend fun saveFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            firestore.collection("Users").document(userId).update("fcmToken", token).await()
        } catch (_: Exception) { /* ignore token save failures */ }
    }

    private fun mapFirebaseAuthError(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Email or password is incorrect."
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
            else -> when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "No internet connection. Please check your network."
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                    "Email or password is incorrect."
                else -> "Something went wrong. Please try again."
            }
        }
    }
}
