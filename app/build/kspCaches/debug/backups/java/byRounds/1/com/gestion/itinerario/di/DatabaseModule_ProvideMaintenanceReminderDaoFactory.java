package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.MaintenanceReminderDao;
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
public final class DatabaseModule_ProvideMaintenanceReminderDaoFactory implements Factory<MaintenanceReminderDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideMaintenanceReminderDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MaintenanceReminderDao get() {
    return provideMaintenanceReminderDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMaintenanceReminderDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideMaintenanceReminderDaoFactory(dbProvider);
  }

  public static MaintenanceReminderDao provideMaintenanceReminderDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMaintenanceReminderDao(db));
  }
}
