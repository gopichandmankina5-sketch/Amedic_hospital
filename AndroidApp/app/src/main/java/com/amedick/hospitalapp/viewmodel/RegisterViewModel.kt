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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val uiState: StateFlow<RegistrationState> = _uiState.asStateFlow()

    fun register(
        name: String, 
        email: String, 
        phone: String, 
        password: String, 
        role: String,
        specialization: String = "",
        qualification: String = "",
        experience: Int = 0,
        consultationFee: Double = 0.0,
        hospital: String = "",
        location: String = ""
    ) {
        _uiState.value = RegistrationState.Loading
        viewModelScope.launch {
            val user = User(
                name = name, 
                email = email, 
                phone = phone, 
                role = role,
                specialization = specialization,
                qualification = qualification,
                experience = experience,
                consultationFee = consultationFee,
                hospital = hospital,
                location = location
            )
            val result = authRepository.register(user, password)
            _uiState.value = result.fold(
                onSuccess = { RegistrationState.Success },
                onFailure = { RegistrationState.Error(it.message ?: "Registration failed. Please try again.") }
            )
        }
    }

    fun resetState() {
        _uiState.value = RegistrationState.Idle
    }
}

sealed class RegistrationState {
    data object Idle : RegistrationState()
    data object Loading : RegistrationState()
    data object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}
