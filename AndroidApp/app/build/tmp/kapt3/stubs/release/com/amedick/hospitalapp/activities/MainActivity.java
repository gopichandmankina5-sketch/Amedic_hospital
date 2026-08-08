package com.amedick.hospitalapp.activities;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\u0006J\u0012\u0010\b\u001a\u00020\u00062\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0014J\u0010\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/amedick/hospitalapp/activities/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/amedick/hospitalapp/databinding/ActivityMainBinding;", "navigateToAppointments", "", "navigateToDoctors", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "replaceFragment", "fragment", "Landroidx/fragment/app/Fragment;", "app_release"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.amedick.hospitalapp.databinding.ActivityMainBinding binding;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void replaceFragment(androidx.fragment.app.Fragment fragment) {
    }
    
    /**
     * Allow fragments to navigate to the Doctors tab
     */
    public final void navigateToDoctors() {
    }
    
    /**
     * Allow fragments to navigate to the Appointments tab
     */
    public final void navigateToAppointments() {
    }
}