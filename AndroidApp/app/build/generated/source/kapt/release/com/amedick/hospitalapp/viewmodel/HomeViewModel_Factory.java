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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  public HomeViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(authRepositoryProvider.get(), firestoreRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new HomeViewModel_Factory(authRepositoryProvider, firestoreRepositoryProvider);
  }

  public static HomeViewModel newInstance(AuthRepository authRepository,
      FirestoreRepository firestoreRepository) {
    return new HomeViewModel(authRepository, firestoreRepository);
  }
}
