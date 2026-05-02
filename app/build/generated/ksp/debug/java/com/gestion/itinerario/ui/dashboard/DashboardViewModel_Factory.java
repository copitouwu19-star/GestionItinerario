package com.gestion.itinerario.ui.dashboard;

import com.gestion.itinerario.data.repository.AppointmentRepository;
import com.gestion.itinerario.data.repository.EquipmentRepository;
import com.gestion.itinerario.data.repository.InventoryRepository;
import com.gestion.itinerario.data.repository.ServiceRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<EquipmentRepository> equipmentRepoProvider;

  private final Provider<ServiceRepository> serviceRepoProvider;

  private final Provider<InventoryRepository> inventoryRepoProvider;

  private final Provider<AppointmentRepository> appointmentRepoProvider;

  public DashboardViewModel_Factory(Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<ServiceRepository> serviceRepoProvider,
      Provider<InventoryRepository> inventoryRepoProvider,
      Provider<AppointmentRepository> appointmentRepoProvider) {
    this.equipmentRepoProvider = equipmentRepoProvider;
    this.serviceRepoProvider = serviceRepoProvider;
    this.inventoryRepoProvider = inventoryRepoProvider;
    this.appointmentRepoProvider = appointmentRepoProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(equipmentRepoProvider.get(), serviceRepoProvider.get(), inventoryRepoProvider.get(), appointmentRepoProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<ServiceRepository> serviceRepoProvider,
      Provider<InventoryRepository> inventoryRepoProvider,
      Provider<AppointmentRepository> appointmentRepoProvider) {
    return new DashboardViewModel_Factory(equipmentRepoProvider, serviceRepoProvider, inventoryRepoProvider, appointmentRepoProvider);
  }

  public static DashboardViewModel newInstance(EquipmentRepository equipmentRepo,
      ServiceRepository serviceRepo, InventoryRepository inventoryRepo,
      AppointmentRepository appointmentRepo) {
    return new DashboardViewModel(equipmentRepo, serviceRepo, inventoryRepo, appointmentRepo);
  }
}
