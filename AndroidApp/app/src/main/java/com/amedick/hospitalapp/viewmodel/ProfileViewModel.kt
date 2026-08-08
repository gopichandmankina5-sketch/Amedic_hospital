package com.amedick.hospitalapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = authRepository.getCurrentUserId() ?: run {
            _profileState.value = ProfileState.Error("Not signed in.")
            return
        }
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.getUserProfile(uid)
            _profileState.value = result.fold(
                onSuccess = { ProfileState.Loaded(it) },
                onFailure = { ProfileState.Error(it.message ?: "Failed to load profile.") }
            )
        }
    }

    fun updateProfile(user: User) {
        _updateState.value = UpdateState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.updateUserProfile(user)
            _updateState.value = result.fold(
                onSuccess = { UpdateState.Success("Profile updated successfully.") },
                onFailure = { UpdateState.Error(it.message ?: "Failed to update profile.") }
            )
        }
    }

    fun uploadProfileImage(userId: String, imageUri: Uri) {
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            val result = firestoreRepository.uploadUserProfileImage(userId, imageUri)
            _imageUploadState.value = result.fold(
                onSuccess = { ImageUploadState.Success(it) },
                onFailure = { ImageUploadState.Error(it.message ?: "Image upload failed.") }
            )
        }
    }

    fun updatePassword(newPassword: String) {
        _updateState.value = UpdateState.Loading
        viewModelScope.launch {
            val result = authRepository.updatePassword(newPassword)
            _updateState.value = result.fold(
                onSuccess = { UpdateState.Success("Password changed successfully.") },
                onFailure = { UpdateState.Error(it.message ?: "Failed to change password.") }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun resetUpdateState() { _updateState.value = UpdateState.Idle }
    fun resetImageState() { _imageUploadState.value = ImageUploadState.Idle }
}

sealed class ProfileState {
    data object Idle : ProfileState()
    data object Loading : ProfileState()
    data class Loaded(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Loading : UpdateState()
    data class Success(val message: String) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

sealed class ImageUploadState {
    data object Idle : ImageUploadState()
    data object Loading : ImageUploadState()
    data class Success(val downloadUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
