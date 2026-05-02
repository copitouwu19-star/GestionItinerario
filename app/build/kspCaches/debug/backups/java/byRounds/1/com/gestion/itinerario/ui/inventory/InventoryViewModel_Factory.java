package com.gestion.itinerario.ui.inventory;

import com.gestion.itinerario.data.repository.EquipmentRepository;
import com.gestion.itinerario.data.repository.InventoryRepository;
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
    "KotlinInternalInJava",
    "cast"
})
public final class InventoryViewModel_Factory implements Factory<InventoryViewModel> {
  private final Provider<EquipmentRepository> equipmentRepoProvider;

  private final Provider<InventoryRepository> inventoryRepoProvider;

  public InventoryViewModel_Factory(Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<InventoryRepository> inventoryRepoProvider) {
    this.equipmentRepoProvider = equipmentRepoProvider;
    this.inventoryRepoProvider = inventoryRepoProvider;
  }

  @Override
  public InventoryViewModel get() {
    return newInstance(equipmentRepoProvider.get(), inventoryRepoProvider.get());
  }

  public static InventoryViewModel_Factory create(
      Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<InventoryRepository> inventoryRepoProvider) {
    return new InventoryViewModel_Factory(equipmentRepoProvider, inventoryRepoProvider);
  }

  public static InventoryViewModel newInstance(EquipmentRepository equipmentRepo,
      InventoryRepository inventoryRepo) {
    return new InventoryViewModel(equipmentRepo, inventoryRepo);
  }
}
