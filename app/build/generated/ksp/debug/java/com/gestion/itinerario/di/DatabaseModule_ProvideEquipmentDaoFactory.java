package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.EquipmentDao;
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
public final class DatabaseModule_ProvideEquipmentDaoFactory implements Factory<EquipmentDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideEquipmentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public EquipmentDao get() {
    return provideEquipmentDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideEquipmentDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideEquipmentDaoFactory(dbProvider);
  }

  public static EquipmentDao provideEquipmentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideEquipmentDao(db));
  }
}
