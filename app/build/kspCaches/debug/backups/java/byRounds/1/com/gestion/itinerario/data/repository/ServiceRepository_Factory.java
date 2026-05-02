package com.gestion.itinerario.data.repository;

import com.gestion.itinerario.data.db.ServiceOrderDao;
import com.gestion.itinerario.data.db.ServiceSparePartDao;
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
public final class ServiceRepository_Factory implements Factory<ServiceRepository> {
  private final Provider<ServiceOrderDao> orderDaoProvider;

  private final Provider<ServiceSparePartDao> sparePartDaoProvider;

  public ServiceRepository_Factory(Provider<ServiceOrderDao> orderDaoProvider,
      Provider<ServiceSparePartDao> sparePartDaoProvider) {
    this.orderDaoProvider = orderDaoProvider;
    this.sparePartDaoProvider = sparePartDaoProvider;
  }

  @Override
  public ServiceRepository get() {
    return newInstance(orderDaoProvider.get(), sparePartDaoProvider.get());
  }

  public static ServiceRepository_Factory create(Provider<ServiceOrderDao> orderDaoProvider,
      Provider<ServiceSparePartDao> sparePartDaoProvider) {
    return new ServiceRepository_Factory(orderDaoProvider, sparePartDaoProvider);
  }

  public static ServiceRepository newInstance(ServiceOrderDao orderDao,
      ServiceSparePartDao sparePartDao) {
    return new ServiceRepository(orderDao, sparePartDao);
  }
}
