package com.amedick.hospitalapp.repository;

import com.amedick.hospitalapp.api.ApiService;
import com.amedick.hospitalapp.database.AppDatabase;
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
public final class HospitalRepository_Factory implements Factory<HospitalRepository> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<AppDatabase> databaseProvider;

  public HospitalRepository_Factory(Provider<ApiService> apiServiceProvider,
      Provider<AppDatabase> databaseProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public HospitalRepository get() {
    return newInstance(apiServiceProvider.get(), databaseProvider.get());
  }

  public static HospitalRepository_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<AppDatabase> databaseProvider) {
    return new HospitalRepository_Factory(apiServiceProvider, databaseProvider);
  }

  public static HospitalRepository newInstance(ApiService apiService, AppDatabase database) {
    return new HospitalRepository(apiService, database);
  }
}
