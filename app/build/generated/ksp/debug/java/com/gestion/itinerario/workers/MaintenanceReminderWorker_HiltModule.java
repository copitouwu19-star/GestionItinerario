package com.gestion.itinerario.workers;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = MaintenanceReminderWorker.class
)
public interface MaintenanceReminderWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.gestion.itinerario.workers.MaintenanceReminderWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      MaintenanceReminderWorker_AssistedFactory factory);
}
