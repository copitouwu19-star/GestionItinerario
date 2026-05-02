package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.SparePartDao;
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
public final class DatabaseModule_ProvideSparePartDaoFactory implements Factory<SparePartDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSparePartDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SparePartDao get() {
    return provideSparePartDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSparePartDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSparePartDaoFactory(dbProvider);
  }

  public static SparePartDao provideSparePartDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSparePartDao(db));
  }
}
