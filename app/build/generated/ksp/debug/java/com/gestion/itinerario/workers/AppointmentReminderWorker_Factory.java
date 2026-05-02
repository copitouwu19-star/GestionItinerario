package com.gestion.itinerario.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.gestion.itinerario.data.repository.AppointmentRepository;
import dagger.internal.DaggerGenerated;
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
public final class AppointmentReminderWorker_Factory {
  private final Provider<AppointmentRepository> repositoryProvider;

  public AppointmentReminderWorker_Factory(Provider<AppointmentRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public AppointmentReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get());
  }

  public static AppointmentReminderWorker_Factory create(
      Provider<AppointmentRepository> repositoryProvider) {
    return new AppointmentReminderWorker_Factory(repositoryProvider);
  }

  public static AppointmentReminderWorker newInstance(Context context, WorkerParameters params,
      AppointmentRepository repository) {
    return new AppointmentReminderWorker(context, params, repository);
  }
}
