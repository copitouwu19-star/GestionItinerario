package com.gestion.itinerario.ui.agenda;

import com.gestion.itinerario.data.repository.AppointmentRepository;
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
public final class AgendaViewModel_Factory implements Factory<AgendaViewModel> {
  private final Provider<AppointmentRepository> repoProvider;

  public AgendaViewModel_Factory(Provider<AppointmentRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public AgendaViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static AgendaViewModel_Factory create(Provider<AppointmentRepository> repoProvider) {
    return new AgendaViewModel_Factory(repoProvider);
  }

  public static AgendaViewModel newInstance(AppointmentRepository repo) {
    return new AgendaViewModel(repo);
  }
}
