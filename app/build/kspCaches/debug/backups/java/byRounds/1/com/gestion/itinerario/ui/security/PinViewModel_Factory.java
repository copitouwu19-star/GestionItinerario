package com.gestion.itinerario.ui.security;

import com.gestion.itinerario.data.repository.PreferencesRepository;
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
public final class PinViewModel_Factory implements Factory<PinViewModel> {
  private final Provider<PreferencesRepository> prefsProvider;

  public PinViewModel_Factory(Provider<PreferencesRepository> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public PinViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static PinViewModel_Factory create(Provider<PreferencesRepository> prefsProvider) {
    return new PinViewModel_Factory(prefsProvider);
  }

  public static PinViewModel newInstance(PreferencesRepository prefs) {
    return new PinViewModel(prefs);
  }
}
