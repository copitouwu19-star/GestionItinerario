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
public final class AppointmentReminderWorker_AssistedFactory_Impl implements AppointmentReminderWorker_AssistedFactory {
  private final AppointmentReminderWorker_Factory delegateFactory;

  AppointmentReminderWorker_AssistedFactory_Impl(
      AppointmentReminderWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AppointmentReminderWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<AppointmentReminderWorker_AssistedFactory> create(
      AppointmentReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AppointmentReminderWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<AppointmentReminderWorker_AssistedFactory> createFactoryProvider(
      AppointmentReminderWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AppointmentReminderWorker_AssistedFactory_Impl(delegateFactory));
  }
}
