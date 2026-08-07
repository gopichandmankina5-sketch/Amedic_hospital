package com.amedick.hospitalapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amedick.hospitalapp.models.Doctor
import com.amedick.hospitalapp.repository.HospitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HospitalRepository
) : ViewModel() {

    private val _doctorsState = MutableStateFlow<HomeState>(HomeState.Idle)
    val doctorsState: StateFlow<HomeState> = _doctorsState.asStateFlow()

    fun loadDoctors() {
        _doctorsState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getDoctors()
                if (response.isSuccessful) {
                    val doctors = response.body() ?: emptyList()
                    _doctorsState.value = HomeState.DoctorsLoaded(doctors)
                } else {
                    _doctorsState.value = HomeState.Error(response.message())
                }
            } catch (ex: Exception) {
                _doctorsState.value = HomeState.Error(ex.localizedMessage ?: "Network error")
            }
        }
    }
}

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class DoctorsLoaded(val doctors: List<Doctor>) : HomeState()
    data class Error(val message: String) : HomeState()
}
