package com.amedick.hospitalapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amedick.hospitalapp.models.AppointmentEntity
import com.amedick.hospitalapp.models.DoctorEntity
import com.amedick.hospitalapp.models.UserProfileEntity

@Database(entities = [UserProfileEntity::class, DoctorEntity::class, AppointmentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
}
