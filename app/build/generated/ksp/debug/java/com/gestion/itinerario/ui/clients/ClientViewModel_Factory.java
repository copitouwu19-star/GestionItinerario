package com.gestion.itinerario.ui.clients;

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
public final class ClientViewModel_Factory implements Factory<ClientViewModel> {
  private final Provider<ClientRepository> clientRepoProvider;

  private final Provider<EquipmentRepository> equipmentRepoProvider;

  private final Provider<ServiceRepository> serviceRepoProvider;

  public ClientViewModel_Factory(Provider<ClientRepository> clientRepoProvider,
      Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<ServiceRepository> serviceRepoProvider) {
    this.clientRepoProvider = clientRepoProvider;
    this.equipmentRepoProvider = equipmentRepoProvider;
    this.serviceRepoProvider = serviceRepoProvider;
  }

  @Override
  public ClientViewModel get() {
    return newInstance(clientRepoProvider.get(), equipmentRepoProvider.get(), serviceRepoProvider.get());
  }

  public static ClientViewModel_Factory create(Provider<ClientRepository> clientRepoProvider,
      Provider<EquipmentRepository> equipmentRepoProvider,
      Provider<ServiceRepository> serviceRepoProvider) {
    return new ClientViewModel_Factory(clientRepoProvider, equipmentRepoProvider, serviceRepoProvider);
  }

  public static ClientViewModel newInstance(ClientRepository clientRepo,
      EquipmentRepository equipmentRepo, ServiceRepository serviceRepo) {
    return new ClientViewModel(clientRepo, equipmentRepo, serviceRepo);
  }
}
