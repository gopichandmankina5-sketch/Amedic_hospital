package com.amedick.hospitalapp.api

import com.amedick.hospitalapp.models.ApiResponse
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.Doctor
import com.amedick.hospitalapp.models.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/user/login")
    suspend fun login(@Body body: Map<String, String>): Response<ApiResponse>

    @POST("/user/signup")
    suspend fun signup(@Body body: Map<String, String>): Response<ApiResponse>

    @POST("/user/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): Response<ApiResponse>

    @GET("/doctor/appointments/doctors")
    suspend fun getDoctors(): Response<List<Doctor>>

    @POST("/appointmentsbook/Appointment")
    suspend fun bookAppointment(@Body body: Map<String, String>): Response<ApiResponse>

    @GET("/appointmentsbook/patient/{patientId}")
    suspend fun getAppointments(@Path("patientId") patientId: String): Response<List<Appointment>>

    @GET("/doctor/profile")
    suspend fun getProfile(): Response<ApiResponse>
}
