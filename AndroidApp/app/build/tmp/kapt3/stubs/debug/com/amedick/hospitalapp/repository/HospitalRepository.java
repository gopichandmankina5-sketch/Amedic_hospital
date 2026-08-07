package com.amedick.hospitalapp.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J4\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\"\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\b2\u0006\u0010\f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u00110\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J$\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u001f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010 J\u001c\u0010!\u001a\u00020\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00150\u0011H\u0086@\u00a2\u0006\u0002\u0010$J\u001c\u0010%\u001a\u00020\"2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00180\u0011H\u0086@\u00a2\u0006\u0002\u0010$J\u0016\u0010\'\u001a\u00020\"2\u0006\u0010(\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010)J,\u0010*\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010+\u001a\u00020\u000b2\u0006\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u001f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010,J$\u0010-\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\u001e\u001a\u00020\u000b2\u0006\u0010.\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010 R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/amedick/hospitalapp/repository/HospitalRepository;", "", "apiService", "Lcom/amedick/hospitalapp/api/ApiService;", "database", "Lcom/amedick/hospitalapp/database/AppDatabase;", "(Lcom/amedick/hospitalapp/api/ApiService;Lcom/amedick/hospitalapp/database/AppDatabase;)V", "bookAppointment", "Lretrofit2/Response;", "Lcom/amedick/hospitalapp/models/ApiResponse;", "doctorId", "", "patientId", "date", "time", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchAppointments", "", "Lcom/amedick/hospitalapp/models/Appointment;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCachedAppointments", "Lcom/amedick/hospitalapp/models/AppointmentEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCachedDoctors", "Lcom/amedick/hospitalapp/models/DoctorEntity;", "getCachedProfile", "Lcom/amedick/hospitalapp/models/UserProfileEntity;", "getDoctors", "Lcom/amedick/hospitalapp/models/Doctor;", "login", "email", "password", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveAppointments", "", "appointments", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveDoctors", "doctors", "saveUserProfile", "profile", "(Lcom/amedick/hospitalapp/models/UserProfileEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "signup", "name", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOtp", "otp", "app_debug"})
public final class HospitalRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.api.ApiService apiService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.database.AppDatabase database = null;
    
    @javax.inject.Inject()
    public HospitalRepository(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.api.ApiService apiService, @org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.database.AppDatabase database) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object login(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.amedick.hospitalapp.models.ApiResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object signup(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.amedick.hospitalapp.models.ApiResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object verifyOtp(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String otp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.amedick.hospitalapp.models.ApiResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDoctors(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.amedick.hospitalapp.models.Doctor>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object bookAppointment(@org.jetbrains.annotations.NotNull()
    java.lang.String doctorId, @org.jetbrains.annotations.NotNull()
    java.lang.String patientId, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String time, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.amedick.hospitalapp.models.ApiResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object fetchAppointments(@org.jetbrains.annotations.NotNull()
    java.lang.String patientId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.amedick.hospitalapp.models.Appointment>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveUserProfile(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.models.UserProfileEntity profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCachedProfile(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.amedick.hospitalapp.models.UserProfileEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveDoctors(@org.jetbrains.annotations.NotNull()
    java.util.List<com.amedick.hospitalapp.models.DoctorEntity> doctors, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCachedDoctors(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.amedick.hospitalapp.models.DoctorEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveAppointments(@org.jetbrains.annotations.NotNull()
    java.util.List<com.amedick.hospitalapp.models.AppointmentEntity> appointments, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCachedAppointments(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.amedick.hospitalapp.models.AppointmentEntity>> $completion) {
        return null;
    }
}