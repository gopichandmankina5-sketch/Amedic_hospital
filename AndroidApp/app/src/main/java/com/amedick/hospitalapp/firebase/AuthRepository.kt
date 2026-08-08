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
            val result = try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            } catch (e: Exception) {
                throw Exception(mapFirebaseAuthError(e), e)
            }
            
            val uid = result.user?.uid ?: throw Exception("User not found")
            
            val userDoc = try {
                firestore.collection("Users").document(uid).get().await()
            } catch (e: Exception) {
                throw Exception("Failed to load user profile data: ${e.message}", e)
            }
            
            if (!userDoc.exists()) {
                throw Exception("User role is not configured. Please contact administrator.")
            }
            
            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User profile could not be parsed.")
            
            if (user.role.isNullOrBlank()) {
                throw Exception("User role is not configured. Please contact administrator.")
            }
            
            runCatching { saveFcmToken(uid) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
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
        if (e is com.google.firebase.auth.FirebaseAuthException) {
            android.util.Log.e("FirebaseAuth", "Login failed: ${e.errorCode} - ${e.message}", e)
            return when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL", "INVALID_LOGIN_CREDENTIALS", "wrong-password", "invalid-credential" -> "Incorrect email or password."
                "ERROR_USER_NOT_FOUND", "user-not-found" -> "No account found with this email."
                "ERROR_TOO_MANY_REQUESTS", "too-many-requests" -> "Too many login attempts. Please try again later."
                "ERROR_USER_DISABLED", "user-disabled" -> "This account has been disabled."
                else -> "Unable to sign in. Please try again."
            }
        }
        
        if (e is com.google.firebase.FirebaseNetworkException) {
            android.util.Log.e("FirebaseAuth", "Login failed: Network Error", e)
            return "Network connection failed. Please check your internet connection."
        }

        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
            else -> when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network connection failed. Please check your internet connection."
                e.message?.contains("User role is not configured", ignoreCase = true) == true ->
                    e.message!!
                e.message?.contains("Failed to load user profile data", ignoreCase = true) == true ->
                    e.message!!
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                    "Incorrect email or password."
                else -> "Something went wrong. Please try again."
            }
        }
    }
}
