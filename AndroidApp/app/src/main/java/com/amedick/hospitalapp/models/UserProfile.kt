package com.amedick.hospitalapp.models

data class UserProfile(
    val _id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val dateJoined: String? = null
)
