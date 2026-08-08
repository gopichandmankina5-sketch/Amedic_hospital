package com.amedick.hospitalapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginDebug", "Login started")
                android.util.Log.d("LoginDebug", "Email: $email")
                val result = authRepository.login(email.trim(), password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    android.util.Log.d("LoginDebug", "Firebase login successful")
                    android.util.Log.d("LoginDebug", "UID: ${user?.uid}")
                    _uiState.value = LoginState.Success(user)
                } else {
                    val exception = result.exceptionOrNull()
                    val cause = exception?.cause
                    if (cause is com.google.firebase.auth.FirebaseAuthException) {
                        android.util.Log.e("LoginDebug", "Firebase login failed: ${cause.errorCode} - ${cause.message}", exception)
                    } else {
                        android.util.Log.e("LoginDebug", "Firebase login failed", exception)
                    }
                    _uiState.value = LoginState.Error(exception?.message ?: "Unable to sign in.")
                }
            } catch (e: Exception) {
                val cause = e.cause
                if (cause is com.google.firebase.auth.FirebaseAuthException) {
                    android.util.Log.e("LoginDebug", "Firebase login failed: ${cause.errorCode} - ${cause.message}", e)
                } else {
                    android.util.Log.e("LoginDebug", "Firebase login failed", e)
                }
                _uiState.value = LoginState.Error(e.message ?: "Unable to sign in.")
            } finally {
                // Ensure the loading state is completely resolved
                if (_uiState.value is LoginState.Loading) {
                    _uiState.value = LoginState.Idle
                }
            }
        }
    }

    fun sendForgotPassword(email: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            _forgotPasswordState.value = if (result.isSuccess) {
                ForgotPasswordState.Success
            } else {
                ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email.")
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User?) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
