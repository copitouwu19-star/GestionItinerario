package com.gestion.itinerario.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.gestion.itinerario.data.repository.ReminderRepository;
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
public final class MaintenanceReminderWorker_Factory {
  private final Provider<ReminderRepository> repositoryProvider;

  public MaintenanceReminderWorker_Factory(Provider<ReminderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public MaintenanceReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get());
  }

  public static MaintenanceReminderWorker_Factory create(
      Provider<ReminderRepository> repositoryProvider) {
    return new MaintenanceReminderWorker_Factory(repositoryProvider);
  }

  public static MaintenanceReminderWorker newInstance(Context context, WorkerParameters params,
      ReminderRepository repository) {
    return new MaintenanceReminderWorker(context, params, repository);
  }
}
