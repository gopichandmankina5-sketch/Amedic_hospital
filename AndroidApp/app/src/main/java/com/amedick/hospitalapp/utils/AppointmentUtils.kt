package com.amedick.hospitalapp.utils

import android.util.Log
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppointmentUtils {

    /**
     * Parses the date and time from an Appointment.
     * Expected formats:
     * Date: "yyyy-MM-dd"
     * Time: "hh:mm a"
     * 
     * Returns null if parsing fails.
     */
    fun getAppointmentDateTime(appointment: Appointment): Date? {
        if (appointment.date.isEmpty() || appointment.time.isEmpty()) return null
        
        try {
            val dateTimeStr = "${appointment.date} ${appointment.time}"
            val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            return format.parse(dateTimeStr)
        } catch (e: Exception) {
            Log.e("AppointmentUtils", "Failed to parse date/time: ${appointment.date} ${appointment.time}", e)
            return null
        }
    }

    /**
     * Checks if the appointment is genuinely upcoming.
     * Criteria:
     * 1. Status is PENDING or ACCEPTED.
     * 2. The combined date/time is in the future.
     */
    fun isAppointmentUpcoming(appointment: Appointment): Boolean {
        if (appointment.status != AppointmentStatus.PENDING && 
            appointment.status != AppointmentStatus.ACCEPTED) {
            return false
        }
        
        val appointmentDate = getAppointmentDateTime(appointment)
        if (appointmentDate != null) {
            val now = Date()
            return appointmentDate.after(now)
        }
        
        // Fallback if parsing fails (we assume it's upcoming so it doesn't just disappear, but ideal is to not fail)
        return false 
    }

    /**
     * Checks if the appointment time has already passed or started.
     */
    fun isAppointmentStarted(appointment: Appointment): Boolean {
        val appointmentDate = getAppointmentDateTime(appointment)
        if (appointmentDate != null) {
            val now = Date()
            // It has started if the scheduled time is before or exactly now
            return !appointmentDate.after(now)
        }
        
        // If we can't parse it, we default to allowing completion to not block the doctor indefinitely
        return true
    }

    const val DEFAULT_APPOINTMENT_DURATION_MINUTES = 30

    /**
     * Checks if the current time is within the allowed video call window.
     * Starts at the scheduled appointment time.
     * Ends at scheduled time + DEFAULT_APPOINTMENT_DURATION_MINUTES.
     */
    fun isWithinVideoCallWindow(appointment: Appointment): Boolean {
        val start = getAppointmentDateTime(appointment) ?: return false
        val now = Date()
        val end = Date(start.time + DEFAULT_APPOINTMENT_DURATION_MINUTES * 60 * 1000L)
        return (now.after(start) || now.equals(start)) && now.before(end)
    }

    /**
     * Checks if the meeting is in the future.
     */
    fun isBeforeVideoCallWindow(appointment: Appointment): Boolean {
        val start = getAppointmentDateTime(appointment) ?: return false
        val now = Date()
        return now.before(start)
    }
}
