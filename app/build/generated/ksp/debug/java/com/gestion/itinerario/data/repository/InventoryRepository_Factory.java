package com.gestion.itinerario.data.repository;

import com.gestion.itinerario.data.db.SparePartDao;
import com.gestion.itinerario.data.db.StockMovementDao;
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
public final class InventoryRepository_Factory implements Factory<InventoryRepository> {
  private final Provider<SparePartDao> sparePartDaoProvider;

  private final Provider<StockMovementDao> stockMovementDaoProvider;

  public InventoryRepository_Factory(Provider<SparePartDao> sparePartDaoProvider,
      Provider<StockMovementDao> stockMovementDaoProvider) {
    this.sparePartDaoProvider = sparePartDaoProvider;
    this.stockMovementDaoProvider = stockMovementDaoProvider;
  }

  @Override
  public InventoryRepository get() {
    return newInstance(sparePartDaoProvider.get(), stockMovementDaoProvider.get());
  }

  public static InventoryRepository_Factory create(Provider<SparePartDao> sparePartDaoProvider,
      Provider<StockMovementDao> stockMovementDaoProvider) {
    return new InventoryRepository_Factory(sparePartDaoProvider, stockMovementDaoProvider);
  }

  public static InventoryRepository newInstance(SparePartDao sparePartDao,
      StockMovementDao stockMovementDao) {
    return new InventoryRepository(sparePartDao, stockMovementDao);
  }
}
