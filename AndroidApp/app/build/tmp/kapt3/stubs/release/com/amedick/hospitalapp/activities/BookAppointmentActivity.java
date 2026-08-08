package com.amedick.hospitalapp.activities;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0014J\u0010\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\b\u0010\u0012\u001a\u00020\fH\u0002J\u0010\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0014\u001a\u00020\u0015H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u0017"}, d2 = {"Lcom/amedick/hospitalapp/activities/BookAppointmentActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/amedick/hospitalapp/databinding/ActivityBookAppointmentBinding;", "viewModel", "Lcom/amedick/hospitalapp/viewmodel/AppointmentViewModel;", "getViewModel", "()Lcom/amedick/hospitalapp/viewmodel/AppointmentViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setLoading", "loading", "", "showSuccessDialog", "validateInputs", "doctorId", "", "Companion", "app_release"})
public final class BookAppointmentActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_DOCTOR_ID = "doctorId";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_DOCTOR_NAME = "doctorName";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_DOCTOR_SPEC = "doctorSpec";
    private com.amedick.hospitalapp.databinding.ActivityBookAppointmentBinding binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.amedick.hospitalapp.activities.BookAppointmentActivity.Companion Companion = null;
    
    public BookAppointmentActivity() {
        super();
    }
    
    private final com.amedick.hospitalapp.viewmodel.AppointmentViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final boolean validateInputs(java.lang.String doctorId) {
        return false;
    }
    
    private final void showSuccessDialog() {
    }
    
    private final void setLoading(boolean loading) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/amedick/hospitalapp/activities/BookAppointmentActivity$Companion;", "", "()V", "EXTRA_DOCTOR_ID", "", "EXTRA_DOCTOR_NAME", "EXTRA_DOCTOR_SPEC", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}