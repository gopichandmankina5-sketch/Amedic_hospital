package com.amedick.hospitalapp.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FirestoreRepository_Factory implements Factory<FirestoreRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseAuth> authProvider;

  public FirestoreRepository_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider) {
    this.firestoreProvider = firestoreProvider;
    this.storageProvider = storageProvider;
    this.authProvider = authProvider;
  }

  @Override
  public FirestoreRepository get() {
    return newInstance(firestoreProvider.get(), storageProvider.get(), authProvider.get());
  }

  public static FirestoreRepository_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider) {
    return new FirestoreRepository_Factory(firestoreProvider, storageProvider, authProvider);
  }

  public static FirestoreRepository newInstance(FirebaseFirestore firestore,
      FirebaseStorage storage, FirebaseAuth auth) {
    return new FirestoreRepository(firestore, storage, auth);
  }
}
