package com.amedick.hospitalapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amedick.hospitalapp.models.AppointmentEntity

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Query("SELECT * FROM appointment ORDER BY date ASC, time ASC")
    suspend fun getAllAppointments(): List<AppointmentEntity>

    @Query("DELETE FROM appointment")
    suspend fun clearAppointments()
}
