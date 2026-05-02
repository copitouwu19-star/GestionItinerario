package com.gestion.itinerario.ui.reminders;

import com.gestion.itinerario.data.repository.ReminderRepository;
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
public final class ReminderViewModel_Factory implements Factory<ReminderViewModel> {
  private final Provider<ReminderRepository> repoProvider;

  public ReminderViewModel_Factory(Provider<ReminderRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public ReminderViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static ReminderViewModel_Factory create(Provider<ReminderRepository> repoProvider) {
    return new ReminderViewModel_Factory(repoProvider);
  }

  public static ReminderViewModel newInstance(ReminderRepository repo) {
    return new ReminderViewModel(repo);
  }
}
