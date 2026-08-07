package com.amedick.hospitalapp.models

data class Appointment(
    val _id: String = "",
    val doctorId: Doctor? = null,
    val patientId: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "booked"
)
