package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.ClientDao;
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
public final class DatabaseModule_ProvideClientDaoFactory implements Factory<ClientDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideClientDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ClientDao get() {
    return provideClientDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideClientDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideClientDaoFactory(dbProvider);
  }

  public static ClientDao provideClientDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideClientDao(db));
  }
}
