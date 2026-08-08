package com.amedick.hospitalapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.Doctor
import com.amedick.hospitalapp.models.User
import com.amedick.hospitalapp.utils.AppointmentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _doctorsState = MutableStateFlow<HomeState>(HomeState.Idle)
    val doctorsState: StateFlow<HomeState> = _doctorsState.asStateFlow()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _upcomingAppointment = MutableStateFlow<Appointment?>(null)
    val upcomingAppointment: StateFlow<Appointment?> = _upcomingAppointment.asStateFlow()

    init {
        loadUser()
        loadDoctors()
        loadUpcomingAppointment()
    }

    private fun loadUser() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            firestoreRepository.getUserProfile(uid).onSuccess { user ->
                _userState.value = user
            }
        }
    }

    fun loadDoctors() {
        _doctorsState.value = HomeState.Loading
        viewModelScope.launch {
            firestoreRepository.getDoctorsRealtime("VERIFIED").collect { result ->
                _doctorsState.value = if (result.isSuccess) {
                    HomeState.DoctorsLoaded(result.getOrDefault(emptyList()))
                } else {
                    HomeState.Error(result.exceptionOrNull()?.message ?: "Unable to load doctors.")
                }
            }
        }
    }

    private fun loadUpcomingAppointment() {
        val patientId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            firestoreRepository.getAppointmentsForPatientRealtime(patientId).collect { result ->
                if (result.isSuccess) {
                    val list = result.getOrDefault(emptyList())
                    // Filter using centralized logic
                    val upcomingAppointments = list.filter { AppointmentUtils.isAppointmentUpcoming(it) }
                    
                    // Sort to find the nearest upcoming appointment
                    val nearestAppointment = upcomingAppointments.minByOrNull {
                        AppointmentUtils.getAppointmentDateTime(it)?.time ?: Long.MAX_VALUE
                    }
                    _upcomingAppointment.value = nearestAppointment
                }
            }
        }
    }

    fun refresh() {
        loadUser()
        loadDoctors()
        loadUpcomingAppointment()
    }
}

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class DoctorsLoaded(val doctors: List<Doctor>) : HomeState()
    data class Error(val message: String) : HomeState()
}
