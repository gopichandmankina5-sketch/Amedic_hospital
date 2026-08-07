package com.amedick.hospitalapp.models

data class ApiResponse(
    val status: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val appointment: Appointment? = null,
    val doctor: Doctor? = null
)
