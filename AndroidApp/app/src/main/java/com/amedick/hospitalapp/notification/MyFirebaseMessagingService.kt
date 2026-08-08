package com.amedick.hospitalapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.amedick.hospitalapp.R
import com.amedick.hospitalapp.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID_APPOINTMENTS = "appointments_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        createNotificationChannels()

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "AmedicK"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "You have a new notification."
        val type = remoteMessage.data["type"] ?: "general"

        showNotification(title, body, type)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Token will be saved on next login via AuthRepository.saveFcmToken
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val appointmentChannel = NotificationChannel(
                CHANNEL_ID_APPOINTMENTS,
                "Appointment Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for appointment bookings, confirmations, and cancellations"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannel(appointmentChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = when (type) {
            "appointment_booked", "appointment_confirmed",
            "appointment_cancelled", "appointment_rejected",
            "appointment_reminder" -> CHANNEL_ID_APPOINTMENTS
            else -> CHANNEL_ID_GENERAL
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_appointments)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}
