package com.gestion.itinerario.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MaintenanceReminderWorker_AssistedFactory_Impl implements MaintenanceReminderWorker_AssistedFactory {
  private final MaintenanceReminderWorker_Factory delegateFactory;

  MaintenanceReminderWorker_AssistedFactory_Impl(
      MaintenanceReminderWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public MaintenanceReminderWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<MaintenanceReminderWorker_AssistedFactory> create(
      MaintenanceReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MaintenanceReminderWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<MaintenanceReminderWorker_AssistedFactory> createFactoryProvider(
      MaintenanceReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MaintenanceReminderWorker_AssistedFactory_Impl(delegateFactory));
  }
}
