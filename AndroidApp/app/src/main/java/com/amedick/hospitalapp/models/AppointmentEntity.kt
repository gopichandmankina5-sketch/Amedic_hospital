package com.amedick.hospitalapp.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointment")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val patientId: String,
    val date: String,
    val time: String,
    val status: String
)
