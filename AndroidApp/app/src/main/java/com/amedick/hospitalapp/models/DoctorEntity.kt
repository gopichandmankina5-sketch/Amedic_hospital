package com.amedick.hospitalapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialization: String?,
    val email: String?
)
