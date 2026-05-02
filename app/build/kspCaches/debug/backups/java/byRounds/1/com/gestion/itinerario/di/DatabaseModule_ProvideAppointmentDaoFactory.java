package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.AppointmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "KotlinInternalInJava",
    "cast"
})
public final class DatabaseModule_ProvideAppointmentDaoFactory implements Factory<AppointmentDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideAppointmentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AppointmentDao get() {
    return provideAppointmentDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideAppointmentDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideAppointmentDaoFactory(dbProvider);
  }

  public static AppointmentDao provideAppointmentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAppointmentDao(db));
  }
}
