package com.amedick.hospitalapp.utils

object HealthProblemMapping {

    val problemToSpecialization = mapOf(
        "Headache" to "Neurologist",
        "Skin problem" to "Dermatologist",
        "Eye problem" to "Ophthalmologist",
        "Dental problem" to "Dentist",
        "Chest/heart-related symptoms" to "Cardiologist",
        "Stomach/digestive problems" to "Gastroenterologist",
        "Bone/joint problem" to "Orthopedic Specialist",
        "Women's health" to "Gynecologist",
        "Child health" to "Pediatrician",
        "Mental health" to "Psychiatrist",
        "Ear/Nose/Throat" to "ENT Specialist",
        "General fever/cold" to "General Physician",
        "Other" to "General Physician"
    )

    val problemsList = problemToSpecialization.keys.toList()

    fun getRecommendedSpecialty(problem: String): String {
        return problemToSpecialization[problem] ?: "General Physician"
    }
}
