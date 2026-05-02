package com.gestion.itinerario.data.repository;

import com.gestion.itinerario.data.db.EquipmentDao;
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
public final class EquipmentRepository_Factory implements Factory<EquipmentRepository> {
  private final Provider<EquipmentDao> daoProvider;

  public EquipmentRepository_Factory(Provider<EquipmentDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public EquipmentRepository get() {
    return newInstance(daoProvider.get());
  }

  public static EquipmentRepository_Factory create(Provider<EquipmentDao> daoProvider) {
    return new EquipmentRepository_Factory(daoProvider);
  }

  public static EquipmentRepository newInstance(EquipmentDao dao) {
    return new EquipmentRepository(dao);
  }
}
