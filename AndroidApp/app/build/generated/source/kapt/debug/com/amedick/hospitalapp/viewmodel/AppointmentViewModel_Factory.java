package com.amedick.hospitalapp.viewmodel;

import com.amedick.hospitalapp.firebase.AuthRepository;
import com.amedick.hospitalapp.firebase.FirestoreRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppointmentViewModel_Factory implements Factory<AppointmentViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  public AppointmentViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public AppointmentViewModel get() {
    return newInstance(authRepositoryProvider.get(), firestoreRepositoryProvider.get());
  }

  public static AppointmentViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new AppointmentViewModel_Factory(authRepositoryProvider, firestoreRepositoryProvider);
  }

  public static AppointmentViewModel newInstance(AuthRepository authRepository,
      FirestoreRepository firestoreRepository) {
    return new AppointmentViewModel(authRepository, firestoreRepository);
  }
}
