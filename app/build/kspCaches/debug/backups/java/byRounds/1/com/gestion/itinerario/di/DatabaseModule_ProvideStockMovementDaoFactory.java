package com.gestion.itinerario.di;

import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.StockMovementDao;
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
public final class DatabaseModule_ProvideStockMovementDaoFactory implements Factory<StockMovementDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideStockMovementDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public StockMovementDao get() {
    return provideStockMovementDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideStockMovementDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideStockMovementDaoFactory(dbProvider);
  }

  public static StockMovementDao provideStockMovementDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStockMovementDao(db));
  }
}
