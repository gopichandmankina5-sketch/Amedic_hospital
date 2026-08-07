package com.amedick.hospitalapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amedick.hospitalapp.models.DoctorEntity

@Dao
interface DoctorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<DoctorEntity>)

    @Query("SELECT * FROM doctor")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Query("DELETE FROM doctor")
    suspend fun clearDoctors()
}
