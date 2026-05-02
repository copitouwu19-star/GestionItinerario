package com.gestion.itinerario;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class GestionApp_MembersInjector implements MembersInjector<GestionApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public GestionApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<GestionApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new GestionApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(GestionApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.gestion.itinerario.GestionApp.workerFactory")
  public static void injectWorkerFactory(GestionApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
