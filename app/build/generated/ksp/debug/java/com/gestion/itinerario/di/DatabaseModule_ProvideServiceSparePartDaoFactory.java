package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.ServiceSparePartDao;
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
public final class DatabaseModule_ProvideServiceSparePartDaoFactory implements Factory<ServiceSparePartDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideServiceSparePartDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ServiceSparePartDao get() {
    return provideServiceSparePartDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideServiceSparePartDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideServiceSparePartDaoFactory(dbProvider);
  }

  public static ServiceSparePartDao provideServiceSparePartDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideServiceSparePartDao(db));
  }
}
