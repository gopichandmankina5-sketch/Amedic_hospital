package com.amedick.hospitalapp.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0016\u001a\u00020\u0017J\b\u0010\u0018\u001a\u00020\u0017H\u0002J\b\u0010\u0019\u001a\u00020\u0017H\u0002J\u0006\u0010\u001a\u001a\u00020\u0017R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011\u00a8\u0006\u001b"}, d2 = {"Lcom/amedick/hospitalapp/viewmodel/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/amedick/hospitalapp/firebase/AuthRepository;", "firestoreRepository", "Lcom/amedick/hospitalapp/firebase/FirestoreRepository;", "(Lcom/amedick/hospitalapp/firebase/AuthRepository;Lcom/amedick/hospitalapp/firebase/FirestoreRepository;)V", "_doctorsState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/amedick/hospitalapp/viewmodel/HomeState;", "_upcomingAppointment", "Lcom/amedick/hospitalapp/models/Appointment;", "_userState", "Lcom/amedick/hospitalapp/models/User;", "doctorsState", "Lkotlinx/coroutines/flow/StateFlow;", "getDoctorsState", "()Lkotlinx/coroutines/flow/StateFlow;", "upcomingAppointment", "getUpcomingAppointment", "userState", "getUserState", "loadDoctors", "", "loadUpcomingAppointment", "loadUser", "refresh", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.HomeState> _doctorsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.HomeState> doctorsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.models.User> _userState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.models.User> userState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.models.Appointment> _upcomingAppointment = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.models.Appointment> upcomingAppointment = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.HomeState> getDoctorsState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.models.User> getUserState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.models.Appointment> getUpcomingAppointment() {
        return null;
    }
    
    private final void loadUser() {
    }
    
    public final void loadDoctors() {
    }
    
    private final void loadUpcomingAppointment() {
    }
    
    public final void refresh() {
    }
}