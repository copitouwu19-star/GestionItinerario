package com.gestion.itinerario.data.repository;

import com.gestion.itinerario.data.db.AppointmentDao;
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
    "KotlinInternalInJava",
    "cast"
})
public final class AppointmentRepository_Factory implements Factory<AppointmentRepository> {
  private final Provider<AppointmentDao> daoProvider;

  public AppointmentRepository_Factory(Provider<AppointmentDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public AppointmentRepository get() {
    return newInstance(daoProvider.get());
  }

  public static AppointmentRepository_Factory create(Provider<AppointmentDao> daoProvider) {
    return new AppointmentRepository_Factory(daoProvider);
  }

  public static AppointmentRepository newInstance(AppointmentDao dao) {
    return new AppointmentRepository(dao);
  }
}
