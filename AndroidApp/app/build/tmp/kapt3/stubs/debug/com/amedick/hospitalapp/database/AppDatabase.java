package com.amedick.hospitalapp.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\t"}, d2 = {"Lcom/amedick/hospitalapp/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "appointmentDao", "Lcom/amedick/hospitalapp/database/AppointmentDao;", "doctorDao", "Lcom/amedick/hospitalapp/database/DoctorDao;", "userProfileDao", "Lcom/amedick/hospitalapp/database/UserProfileDao;", "app_debug"})
@androidx.room.Database(entities = {com.amedick.hospitalapp.models.UserProfileEntity.class, com.amedick.hospitalapp.models.DoctorEntity.class, com.amedick.hospitalapp.models.AppointmentEntity.class}, version = 1)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.amedick.hospitalapp.database.UserProfileDao userProfileDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.amedick.hospitalapp.database.DoctorDao doctorDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.amedick.hospitalapp.database.AppointmentDao appointmentDao();
}