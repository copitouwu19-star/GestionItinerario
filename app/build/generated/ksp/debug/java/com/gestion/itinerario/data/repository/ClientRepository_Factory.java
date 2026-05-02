package com.gestion.itinerario.data.repository;

import com.gestion.itinerario.data.db.ClientDao;
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
public final class ClientRepository_Factory implements Factory<ClientRepository> {
  private final Provider<ClientDao> daoProvider;

  public ClientRepository_Factory(Provider<ClientDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ClientRepository get() {
    return newInstance(daoProvider.get());
  }

  public static ClientRepository_Factory create(Provider<ClientDao> daoProvider) {
    return new ClientRepository_Factory(daoProvider);
  }

  public static ClientRepository newInstance(ClientDao dao) {
    return new ClientRepository(dao);
  }
}
