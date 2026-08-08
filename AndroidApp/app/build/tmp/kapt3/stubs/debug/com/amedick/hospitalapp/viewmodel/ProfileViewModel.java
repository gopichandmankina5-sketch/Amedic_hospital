package com.amedick.hospitalapp.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017J\u0006\u0010\u0018\u001a\u00020\u0019J\u0006\u0010\u001a\u001a\u00020\u0019J\u0006\u0010\u001b\u001a\u00020\u0019J\u0006\u0010\u001c\u001a\u00020\u0019J\u000e\u0010\u001d\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\u0017J\u000e\u0010\u001f\u001a\u00020\u00192\u0006\u0010 \u001a\u00020!J\u0016\u0010\"\u001a\u00020\u00192\u0006\u0010#\u001a\u00020\u00172\u0006\u0010$\u001a\u00020%R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011\u00a8\u0006&"}, d2 = {"Lcom/amedick/hospitalapp/viewmodel/ProfileViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/amedick/hospitalapp/firebase/AuthRepository;", "firestoreRepository", "Lcom/amedick/hospitalapp/firebase/FirestoreRepository;", "(Lcom/amedick/hospitalapp/firebase/AuthRepository;Lcom/amedick/hospitalapp/firebase/FirestoreRepository;)V", "_imageUploadState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/amedick/hospitalapp/viewmodel/ImageUploadState;", "_profileState", "Lcom/amedick/hospitalapp/viewmodel/ProfileState;", "_updateState", "Lcom/amedick/hospitalapp/viewmodel/UpdateState;", "imageUploadState", "Lkotlinx/coroutines/flow/StateFlow;", "getImageUploadState", "()Lkotlinx/coroutines/flow/StateFlow;", "profileState", "getProfileState", "updateState", "getUpdateState", "getCurrentUserId", "", "loadProfile", "", "logout", "resetImageState", "resetUpdateState", "updatePassword", "newPassword", "updateProfile", "user", "Lcom/amedick/hospitalapp/models/User;", "uploadProfileImage", "userId", "imageUri", "Landroid/net/Uri;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProfileViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.ProfileState> _profileState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ProfileState> profileState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.UpdateState> _updateState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.UpdateState> updateState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.amedick.hospitalapp.viewmodel.ImageUploadState> _imageUploadState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ImageUploadState> imageUploadState = null;
    
    @javax.inject.Inject()
    public ProfileViewModel(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.firebase.FirestoreRepository firestoreRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ProfileState> getProfileState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.UpdateState> getUpdateState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.amedick.hospitalapp.viewmodel.ImageUploadState> getImageUploadState() {
        return null;
    }
    
    public final void loadProfile() {
    }
    
    public final void updateProfile(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.models.User user) {
    }
    
    public final void uploadProfileImage(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri) {
    }
    
    public final void updatePassword(@org.jetbrains.annotations.NotNull()
    java.lang.String newPassword) {
    }
    
    public final void logout() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentUserId() {
        return null;
    }
    
    public final void resetUpdateState() {
    }
    
    public final void resetImageState() {
    }
}