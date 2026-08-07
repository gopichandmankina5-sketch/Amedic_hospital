package com.amedick.hospitalapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(
    val _id: String = "",
    val name: String = "",
    val email: String? = null,
    val specialization: String? = null,
    val phone: String? = null,
    val profilePhoto: String? = null
) : Parcelable
