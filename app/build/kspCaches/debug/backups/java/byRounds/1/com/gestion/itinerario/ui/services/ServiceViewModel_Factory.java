package com.gestion.itinerario.ui.services;

import com.gestion.itinerario.data.repository.ClientRepository;
import com.gestion.itinerario.data.repository.EquipmentRepository;
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
public final class ServiceViewModel_Factory implements Factory<ServiceViewModel> {
  private final Provider<ServiceRepository> serviceRepoProvider;

  private final Provider<ClientRepository> clientRepoProvider;

  private final Provider<EquipmentRepository> equipmentRepoProvider;

  public ServiceViewModel_Factory(Provider<ServiceRepository> serviceRepoProvider,
      Provider<ClientRepository> clientRepoProvider,
      Provider<EquipmentRepository> equipmentRepoProvider) {
    this.serviceRepoProvider = serviceRepoProvider;
    this.clientRepoProvider = clientRepoProvider;
    this.equipmentRepoProvider = equipmentRepoProvider;
  }

  @Override
  public ServiceViewModel get() {
    return newInstance(serviceRepoProvider.get(), clientRepoProvider.get(), equipmentRepoProvider.get());
  }

  public static ServiceViewModel_Factory create(Provider<ServiceRepository> serviceRepoProvider,
      Provider<ClientRepository> clientRepoProvider,
      Provider<EquipmentRepository> equipmentRepoProvider) {
    return new ServiceViewModel_Factory(serviceRepoProvider, clientRepoProvider, equipmentRepoProvider);
  }

  public static ServiceViewModel newInstance(ServiceRepository serviceRepo,
      ClientRepository clientRepo, EquipmentRepository equipmentRepo) {
    return new ServiceViewModel(serviceRepo, clientRepo, equipmentRepo);
  }
}
