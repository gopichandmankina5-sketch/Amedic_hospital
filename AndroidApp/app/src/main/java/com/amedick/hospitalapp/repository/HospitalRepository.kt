package com.amedick.hospitalapp.repository

import com.amedick.hospitalapp.api.ApiService
import com.amedick.hospitalapp.database.AppDatabase
import com.amedick.hospitalapp.models.AppointmentEntity
import com.amedick.hospitalapp.models.DoctorEntity
import com.amedick.hospitalapp.models.UserProfileEntity
import javax.inject.Inject

class HospitalRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {

    suspend fun login(email: String, password: String) =
        apiService.login(mapOf("email" to email, "password" to password))

    suspend fun signup(name: String, email: String, password: String) =
        apiService.signup(mapOf("name" to name, "email" to email, "password" to password))

    suspend fun verifyOtp(email: String, otp: String) =
        apiService.verifyOtp(mapOf("email" to email, "otp" to otp))

    suspend fun getDoctors() = apiService.getDoctors()

    suspend fun bookAppointment(doctorId: String, patientId: String, date: String, time: String) =
        apiService.bookAppointment(mapOf(
            "doctorId" to doctorId,
            "patientId" to patientId,
            "date" to date,
            "time" to time
        ))

    suspend fun fetchAppointments(patientId: String) = apiService.getAppointments(patientId)

    suspend fun saveUserProfile(profile: UserProfileEntity) = database.userProfileDao().insert(profile)

    suspend fun getCachedProfile() = database.userProfileDao().getProfile()

    suspend fun saveDoctors(doctors: List<DoctorEntity>) = database.doctorDao().insertAll(doctors)

    suspend fun getCachedDoctors() = database.doctorDao().getAllDoctors()

    suspend fun saveAppointments(appointments: List<AppointmentEntity>) = database.appointmentDao().insertAll(appointments)

    suspend fun getCachedAppointments() = database.appointmentDao().getAllAppointments()
}
