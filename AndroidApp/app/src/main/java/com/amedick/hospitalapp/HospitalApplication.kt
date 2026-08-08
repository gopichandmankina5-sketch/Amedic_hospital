package com.amedick.hospitalapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HospitalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val appointmentChannel = NotificationChannel(
                "appointments_channel",
                "Appointment Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Appointment updates and reminders"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                "general_channel",
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from AmedicK"
            }

            notificationManager.createNotificationChannel(appointmentChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}
