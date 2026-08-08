package com.amedick.hospitalapp.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J8\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u00192\u0006\u0010\u001d\u001a\u00020\u00192\b\b\u0002\u0010\u001e\u001a\u00020\u0019J\u000e\u0010\u001f\u001a\u00020\u00172\u0006\u0010 \u001a\u00020\u0019J\u0006\u0010!\u001a\u00020\u0017J\u0006\u0010\"\u001a\u00020\u0017J\u0006\u0010#\u001a\u00020\u0017R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011\u00a8\u0006$"}, d2 = {"Lcom/amedick/hospitalapp/viewmodel/AppointmentViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/amedick/hospitalapp/firebase/AuthRepository;", "firestoreRepository", "Lcom/amedick/hospitalapp/firebase/FirestoreRepository;", "(Lcom/amedick/hospitalapp/firebase/AuthRepository;Lcom/amedick/hospitalapp/firebase/FirestoreRepository;)V", "_bookingState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/amedick/hospitalapp/viewmodel/AppointmentState;", "_cancelState", "Lcom/amedick/hospitalapp/viewmodel/CancelState;", "_listState", "Lcom/amedick/hospitalapp/viewmodel/AppointmentListState;", "bookingState", "Lkotlinx/coroutines/flow/StateFlow;", "getBookingState", "()Lkotlinx/coroutines/flow/StateFlow;", "cancelState", "getCancelState", "listState", "getListState", "bookAppointment", "", "doctorId", "", "doctorName", "date", "time", "reason", "patientName", "cancelAppointment", "appointmentId", "loadMyAppointments", "resetBookingState", "resetCancelState", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AppointmentViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.AppointmentState> _bookingState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.AppointmentState> bookingState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.AppointmentListState> _listState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.AppointmentListState> listState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.CancelState> _cancelState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.CancelState> cancelState = null;
    
    @javax.inject.Inject()
    public AppointmentViewModel(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.AppointmentState> getBookingState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.AppointmentListState> getListState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.CancelState> getCancelState() {
        return null;
    }
    
    public final void bookAppointment(@org.jetbrains.annotations.NotNull()
    java.lang.String doctorId, @org.jetbrains.annotations.NotNull()
    java.lang.String doctorName, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String time, @org.jetbrains.annotations.NotNull()
    java.lang.String reason, @org.jetbrains.annotations.NotNull()
    java.lang.String patientName) {
    }
    
    public final void loadMyAppointments() {
    }
    
    public final void cancelAppointment(@org.jetbrains.annotations.NotNull()
    java.lang.String appointmentId) {
    }
    
    public final void resetBookingState() {
    }
    
    public final void resetCancelState() {
    }
}