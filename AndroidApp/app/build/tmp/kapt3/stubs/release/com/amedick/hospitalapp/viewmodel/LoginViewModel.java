package com.amedick.hospitalapp.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013J\u0006\u0010\u0015\u001a\u00020\u0011J\u0006\u0010\u0016\u001a\u00020\u0011J\u000e\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\r\u00a8\u0006\u0018"}, d2 = {"Lcom/amedick/hospitalapp/viewmodel/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/amedick/hospitalapp/firebase/AuthRepository;", "(Lcom/amedick/hospitalapp/firebase/AuthRepository;)V", "_forgotPasswordState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/amedick/hospitalapp/viewmodel/ForgotPasswordState;", "_uiState", "Lcom/amedick/hospitalapp/viewmodel/LoginState;", "forgotPasswordState", "Lkotlinx/coroutines/flow/StateFlow;", "getForgotPasswordState", "()Lkotlinx/coroutines/flow/StateFlow;", "uiState", "getUiState", "login", "", "email", "", "password", "resetForgotPasswordState", "resetState", "sendForgotPassword", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.LoginState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.LoginState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.ForgotPasswordState> _forgotPasswordState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ForgotPasswordState> forgotPasswordState = null;
    
    @javax.inject.Inject()
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.LoginState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ForgotPasswordState> getForgotPasswordState() {
        return null;
    }
    
    public final void login(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password) {
    }
    
    public final void sendForgotPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
    }
    
    public final void resetForgotPasswordState() {
    }
    
    public final void resetState() {
    }
}