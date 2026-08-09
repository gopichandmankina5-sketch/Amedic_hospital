package com.amedick.hospitalapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow<AppointmentState>(AppointmentState.Idle)
    val bookingState: StateFlow<AppointmentState> = _bookingState.asStateFlow()

    private val _listState = MutableStateFlow<AppointmentListState>(AppointmentListState.Idle)
    val listState: StateFlow<AppointmentListState> = _listState.asStateFlow()

    private val _cancelState = MutableStateFlow<CancelState>(CancelState.Idle)
    val cancelState: StateFlow<CancelState> = _cancelState.asStateFlow()

    fun bookAppointment(
        doctorId: String,
        doctorName: String,
        date: String,
        time: String,
        reason: String,
        patientName: String = "",
        consultationType: String = "OFFLINE",
        consultationFee: Double = 0.0,
        upiId: String = "",
        paymentQrUrl: String = "",
        paymentStatus: String = "pending",
        paymentProofUrl: String = ""
    ) {
        val patientId = authRepository.getCurrentUserId()
        if (patientId == null) {
            _bookingState.value = AppointmentState.Error("Please sign in again.")
            return
        }
        _bookingState.value = AppointmentState.Loading
        viewModelScope.launch {
            val safeDate = date.replace("/", "").replace("-", "")
            val safeTime = time.replace(":", "").replace(" ", "")
            val slotDocId = "${doctorId}_${safeDate}_${safeTime}"

            val appointment = Appointment(
                appointmentId = slotDocId,
                patientId = patientId,
                doctorId = doctorId,
                patientName = patientName,
                doctorName = doctorName,
                date = date,
                time = time,
                reason = reason,
                status = AppointmentStatus.PENDING,
                consultationType = consultationType,
                videoRoomId = if (consultationType == "ONLINE") "amedick-$slotDocId" else "",
                videoRoomUrl = if (consultationType == "ONLINE") "https://meet.jit.si/amedick-$slotDocId" else "",
                consultationFee = consultationFee,
                upiId = upiId,
                paymentQrUrl = paymentQrUrl,
                paymentStatus = paymentStatus,
                paymentDate = if (paymentStatus == "submitted") System.currentTimeMillis() else 0L,
                paymentProofUrl = paymentProofUrl
            )
            val result = firestoreRepository.bookAppointment(appointment)
            _bookingState.value = result.fold(
                onSuccess = { AppointmentState.Success },
                onFailure = { AppointmentState.Error(it.message ?: "Unable to book appointment.") }
            )
        }
    }

    fun rescheduleAppointment(
        appointmentId: String,
        date: String,
        time: String,
        consultationType: String
    ) {
        _bookingState.value = AppointmentState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.requestReschedule(appointmentId, date, time, consultationType)
            result.onSuccess {
                _bookingState.value = AppointmentState.Success
            }.onFailure { e ->
                _bookingState.value = AppointmentState.Error(e.message ?: "Failed to reschedule appointment")
            }
        }
    }

    fun loadMyAppointments() {
        val patientId = authRepository.getCurrentUserId() ?: return
        _listState.value = AppointmentListState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.getAppointmentsForPatient(patientId)
            _listState.value = result.fold(
                onSuccess = { AppointmentListState.Loaded(it) },
                onFailure = { AppointmentListState.Error(it.message ?: "Failed to load appointments.") }
            )
        }
    }

    fun cancelAppointment(appointmentId: String) {
        _cancelState.value = CancelState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.cancelAppointment(appointmentId)
            _cancelState.value = result.fold(
                onSuccess = { CancelState.Success },
                onFailure = { CancelState.Error(it.message ?: "Failed to cancel appointment.") }
            )
        }
    }

    fun respondToCompletionVerification(appointmentId: String, isConfirmed: Boolean, onResult: (Boolean, String?) -> Unit) {
        val patientId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = firestoreRepository.respondCompletionVerification(appointmentId, isConfirmed, patientId)
            if (result.isSuccess) {
                onResult(true, null)
                loadMyAppointments()
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to verify consultation")
            }
        }
    }

    fun resetBookingState() { _bookingState.value = AppointmentState.Idle }
    fun resetCancelState() { _cancelState.value = CancelState.Idle }
}

sealed class AppointmentState {
    data object Idle : AppointmentState()
    data object Loading : AppointmentState()
    data object Success : AppointmentState()
    data class Error(val message: String) : AppointmentState()
}

sealed class AppointmentListState {
    data object Idle : AppointmentListState()
    data object Loading : AppointmentListState()
    data class Loaded(val appointments: List<Appointment>) : AppointmentListState()
    data class Error(val message: String) : AppointmentListState()
}

sealed class CancelState {
    data object Idle : CancelState()
    data object Loading : CancelState()
    data object Success : CancelState()
    data class Error(val message: String) : CancelState()
}
