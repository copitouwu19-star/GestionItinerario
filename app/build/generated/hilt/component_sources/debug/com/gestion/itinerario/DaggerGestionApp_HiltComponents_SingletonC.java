package com.gestion.itinerario;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.gestion.itinerario.data.db.AppDatabase;
import com.gestion.itinerario.data.db.AppointmentDao;
import com.gestion.itinerario.data.db.ClientDao;
import com.gestion.itinerario.data.db.EquipmentDao;
import com.gestion.itinerario.data.db.MaintenanceReminderDao;
import com.gestion.itinerario.data.db.ServiceOrderDao;
import com.gestion.itinerario.data.db.ServiceSparePartDao;
import com.gestion.itinerario.data.db.SparePartDao;
import com.gestion.itinerario.data.db.StockMovementDao;
import com.gestion.itinerario.data.repository.AppointmentRepository;
import com.gestion.itinerario.data.repository.ClientRepository;
import com.gestion.itinerario.data.repository.EquipmentRepository;
import com.gestion.itinerario.data.repository.InventoryRepository;
import com.gestion.itinerario.data.repository.PreferencesRepository;
import com.gestion.itinerario.data.repository.ReminderRepository;
import com.gestion.itinerario.data.repository.ServiceRepository;
import com.gestion.itinerario.di.DatabaseModule_ProvideAppointmentDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideClientDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideDatabaseFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideEquipmentDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideMaintenanceReminderDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideServiceOrderDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideServiceSparePartDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideSparePartDaoFactory;
import com.gestion.itinerario.di.DatabaseModule_ProvideStockMovementDaoFactory;
import com.gestion.itinerario.ui.agenda.AgendaViewModel;
import com.gestion.itinerario.ui.agenda.AgendaViewModel_HiltModules;
import com.gestion.itinerario.ui.clients.ClientViewModel;
import com.gestion.itinerario.ui.clients.ClientViewModel_HiltModules;
import com.gestion.itinerario.ui.dashboard.DashboardViewModel;
import com.gestion.itinerario.ui.dashboard.DashboardViewModel_HiltModules;
import com.gestion.itinerario.ui.inventory.InventoryViewModel;
import com.gestion.itinerario.ui.inventory.InventoryViewModel_HiltModules;
import com.gestion.itinerario.ui.reminders.ReminderViewModel;
import com.gestion.itinerario.ui.reminders.ReminderViewModel_HiltModules;
import com.gestion.itinerario.ui.security.PinViewModel;
import com.gestion.itinerario.ui.security.PinViewModel_HiltModules;
import com.gestion.itinerario.ui.services.ServiceViewModel;
import com.gestion.itinerario.ui.services.ServiceViewModel_HiltModules;
import com.gestion.itinerario.workers.AppointmentReminderWorker;
import com.gestion.itinerario.workers.AppointmentReminderWorker_AssistedFactory;
import com.gestion.itinerario.workers.MaintenanceReminderWorker;
import com.gestion.itinerario.workers.MaintenanceReminderWorker_AssistedFactory;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerGestionApp_HiltComponents_SingletonC {
  private DaggerGestionApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public GestionApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements GestionApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements GestionApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements GestionApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements GestionApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements GestionApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements GestionApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements GestionApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public GestionApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends GestionApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends GestionApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends GestionApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends GestionApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(7).put(LazyClassKeyProvider.com_gestion_itinerario_ui_agenda_AgendaViewModel, AgendaViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_clients_ClientViewModel, ClientViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_dashboard_DashboardViewModel, DashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_inventory_InventoryViewModel, InventoryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_security_PinViewModel, PinViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_reminders_ReminderViewModel, ReminderViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gestion_itinerario_ui_services_ServiceViewModel, ServiceViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_gestion_itinerario_ui_services_ServiceViewModel = "com.gestion.itinerario.ui.services.ServiceViewModel";

      static String com_gestion_itinerario_ui_agenda_AgendaViewModel = "com.gestion.itinerario.ui.agenda.AgendaViewModel";

      static String com_gestion_itinerario_ui_clients_ClientViewModel = "com.gestion.itinerario.ui.clients.ClientViewModel";

      static String com_gestion_itinerario_ui_security_PinViewModel = "com.gestion.itinerario.ui.security.PinViewModel";

      static String com_gestion_itinerario_ui_dashboard_DashboardViewModel = "com.gestion.itinerario.ui.dashboard.DashboardViewModel";

      static String com_gestion_itinerario_ui_inventory_InventoryViewModel = "com.gestion.itinerario.ui.inventory.InventoryViewModel";

      static String com_gestion_itinerario_ui_reminders_ReminderViewModel = "com.gestion.itinerario.ui.reminders.ReminderViewModel";

      @KeepFieldType
      ServiceViewModel com_gestion_itinerario_ui_services_ServiceViewModel2;

      @KeepFieldType
      AgendaViewModel com_gestion_itinerario_ui_agenda_AgendaViewModel2;

      @KeepFieldType
      ClientViewModel com_gestion_itinerario_ui_clients_ClientViewModel2;

      @KeepFieldType
      PinViewModel com_gestion_itinerario_ui_security_PinViewModel2;

      @KeepFieldType
      DashboardViewModel com_gestion_itinerario_ui_dashboard_DashboardViewModel2;

      @KeepFieldType
      InventoryViewModel com_gestion_itinerario_ui_inventory_InventoryViewModel2;

      @KeepFieldType
      ReminderViewModel com_gestion_itinerario_ui_reminders_ReminderViewModel2;
    }
  }

  private static final class ViewModelCImpl extends GestionApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AgendaViewModel> agendaViewModelProvider;

    private Provider<ClientViewModel> clientViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<InventoryViewModel> inventoryViewModelProvider;

    private Provider<PinViewModel> pinViewModelProvider;

    private Provider<ReminderViewModel> reminderViewModelProvider;

    private Provider<ServiceViewModel> serviceViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.agendaViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.clientViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.inventoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.pinViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.reminderViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.serviceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(7).put(LazyClassKeyProvider.com_gestion_itinerario_ui_agenda_AgendaViewModel, ((Provider) agendaViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_clients_ClientViewModel, ((Provider) clientViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_dashboard_DashboardViewModel, ((Provider) dashboardViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_inventory_InventoryViewModel, ((Provider) inventoryViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_security_PinViewModel, ((Provider) pinViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_reminders_ReminderViewModel, ((Provider) reminderViewModelProvider)).put(LazyClassKeyProvider.com_gestion_itinerario_ui_services_ServiceViewModel, ((Provider) serviceViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_gestion_itinerario_ui_security_PinViewModel = "com.gestion.itinerario.ui.security.PinViewModel";

      static String com_gestion_itinerario_ui_reminders_ReminderViewModel = "com.gestion.itinerario.ui.reminders.ReminderViewModel";

      static String com_gestion_itinerario_ui_inventory_InventoryViewModel = "com.gestion.itinerario.ui.inventory.InventoryViewModel";

      static String com_gestion_itinerario_ui_services_ServiceViewModel = "com.gestion.itinerario.ui.services.ServiceViewModel";

      static String com_gestion_itinerario_ui_clients_ClientViewModel = "com.gestion.itinerario.ui.clients.ClientViewModel";

      static String com_gestion_itinerario_ui_dashboard_DashboardViewModel = "com.gestion.itinerario.ui.dashboard.DashboardViewModel";

      static String com_gestion_itinerario_ui_agenda_AgendaViewModel = "com.gestion.itinerario.ui.agenda.AgendaViewModel";

      @KeepFieldType
      PinViewModel com_gestion_itinerario_ui_security_PinViewModel2;

      @KeepFieldType
      ReminderViewModel com_gestion_itinerario_ui_reminders_ReminderViewModel2;

      @KeepFieldType
      InventoryViewModel com_gestion_itinerario_ui_inventory_InventoryViewModel2;

      @KeepFieldType
      ServiceViewModel com_gestion_itinerario_ui_services_ServiceViewModel2;

      @KeepFieldType
      ClientViewModel com_gestion_itinerario_ui_clients_ClientViewModel2;

      @KeepFieldType
      DashboardViewModel com_gestion_itinerario_ui_dashboard_DashboardViewModel2;

      @KeepFieldType
      AgendaViewModel com_gestion_itinerario_ui_agenda_AgendaViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.gestion.itinerario.ui.agenda.AgendaViewModel 
          return (T) new AgendaViewModel(singletonCImpl.appointmentRepositoryProvider.get());

          case 1: // com.gestion.itinerario.ui.clients.ClientViewModel 
          return (T) new ClientViewModel(singletonCImpl.clientRepositoryProvider.get(), singletonCImpl.equipmentRepositoryProvider.get(), singletonCImpl.serviceRepositoryProvider.get());

          case 2: // com.gestion.itinerario.ui.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.equipmentRepositoryProvider.get(), singletonCImpl.serviceRepositoryProvider.get(), singletonCImpl.inventoryRepositoryProvider.get(), singletonCImpl.appointmentRepositoryProvider.get());

          case 3: // com.gestion.itinerario.ui.inventory.InventoryViewModel 
          return (T) new InventoryViewModel(singletonCImpl.equipmentRepositoryProvider.get(), singletonCImpl.inventoryRepositoryProvider.get());

          case 4: // com.gestion.itinerario.ui.security.PinViewModel 
          return (T) new PinViewModel(singletonCImpl.preferencesRepositoryProvider.get());

          case 5: // com.gestion.itinerario.ui.reminders.ReminderViewModel 
          return (T) new ReminderViewModel(singletonCImpl.reminderRepositoryProvider.get());

          case 6: // com.gestion.itinerario.ui.services.ServiceViewModel 
          return (T) new ServiceViewModel(singletonCImpl.serviceRepositoryProvider.get(), singletonCImpl.clientRepositoryProvider.get(), singletonCImpl.equipmentRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends GestionApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends GestionApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends GestionApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<AppointmentRepository> appointmentRepositoryProvider;

    private Provider<AppointmentReminderWorker_AssistedFactory> appointmentReminderWorker_AssistedFactoryProvider;

    private Provider<ReminderRepository> reminderRepositoryProvider;

    private Provider<MaintenanceReminderWorker_AssistedFactory> maintenanceReminderWorker_AssistedFactoryProvider;

    private Provider<ClientRepository> clientRepositoryProvider;

    private Provider<EquipmentRepository> equipmentRepositoryProvider;

    private Provider<ServiceRepository> serviceRepositoryProvider;

    private Provider<InventoryRepository> inventoryRepositoryProvider;

    private Provider<PreferencesRepository> preferencesRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private AppointmentDao appointmentDao() {
      return DatabaseModule_ProvideAppointmentDaoFactory.provideAppointmentDao(provideDatabaseProvider.get());
    }

    private MaintenanceReminderDao maintenanceReminderDao() {
      return DatabaseModule_ProvideMaintenanceReminderDaoFactory.provideMaintenanceReminderDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return MapBuilder.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>newMapBuilder(2).put("com.gestion.itinerario.workers.AppointmentReminderWorker", ((Provider) appointmentReminderWorker_AssistedFactoryProvider)).put("com.gestion.itinerario.workers.MaintenanceReminderWorker", ((Provider) maintenanceReminderWorker_AssistedFactoryProvider)).build();
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    private ClientDao clientDao() {
      return DatabaseModule_ProvideClientDaoFactory.provideClientDao(provideDatabaseProvider.get());
    }

    private EquipmentDao equipmentDao() {
      return DatabaseModule_ProvideEquipmentDaoFactory.provideEquipmentDao(provideDatabaseProvider.get());
    }

    private ServiceOrderDao serviceOrderDao() {
      return DatabaseModule_ProvideServiceOrderDaoFactory.provideServiceOrderDao(provideDatabaseProvider.get());
    }

    private ServiceSparePartDao serviceSparePartDao() {
      return DatabaseModule_ProvideServiceSparePartDaoFactory.provideServiceSparePartDao(provideDatabaseProvider.get());
    }

    private SparePartDao sparePartDao() {
      return DatabaseModule_ProvideSparePartDaoFactory.provideSparePartDao(provideDatabaseProvider.get());
    }

    private StockMovementDao stockMovementDao() {
      return DatabaseModule_ProvideStockMovementDaoFactory.provideStockMovementDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.appointmentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AppointmentRepository>(singletonCImpl, 1));
      this.appointmentReminderWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AppointmentReminderWorker_AssistedFactory>(singletonCImpl, 0));
      this.reminderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ReminderRepository>(singletonCImpl, 4));
      this.maintenanceReminderWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<MaintenanceReminderWorker_AssistedFactory>(singletonCImpl, 3));
      this.clientRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ClientRepository>(singletonCImpl, 5));
      this.equipmentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<EquipmentRepository>(singletonCImpl, 6));
      this.serviceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ServiceRepository>(singletonCImpl, 7));
      this.inventoryRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<InventoryRepository>(singletonCImpl, 8));
      this.preferencesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PreferencesRepository>(singletonCImpl, 9));
    }

    @Override
    public void injectGestionApp(GestionApp gestionApp) {
      injectGestionApp2(gestionApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private GestionApp injectGestionApp2(GestionApp instance) {
      GestionApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.gestion.itinerario.workers.AppointmentReminderWorker_AssistedFactory 
          return (T) new AppointmentReminderWorker_AssistedFactory() {
            @Override
            public AppointmentReminderWorker create(Context context, WorkerParameters params) {
              return new AppointmentReminderWorker(context, params, singletonCImpl.appointmentRepositoryProvider.get());
            }
          };

          case 1: // com.gestion.itinerario.data.repository.AppointmentRepository 
          return (T) new AppointmentRepository(singletonCImpl.appointmentDao());

          case 2: // com.gestion.itinerario.data.db.AppDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.gestion.itinerario.workers.MaintenanceReminderWorker_AssistedFactory 
          return (T) new MaintenanceReminderWorker_AssistedFactory() {
            @Override
            public MaintenanceReminderWorker create(Context context2, WorkerParameters params2) {
              return new MaintenanceReminderWorker(context2, params2, singletonCImpl.reminderRepositoryProvider.get());
            }
          };

          case 4: // com.gestion.itinerario.data.repository.ReminderRepository 
          return (T) new ReminderRepository(singletonCImpl.maintenanceReminderDao());

          case 5: // com.gestion.itinerario.data.repository.ClientRepository 
          return (T) new ClientRepository(singletonCImpl.clientDao());

          case 6: // com.gestion.itinerario.data.repository.EquipmentRepository 
          return (T) new EquipmentRepository(singletonCImpl.equipmentDao());

          case 7: // com.gestion.itinerario.data.repository.ServiceRepository 
          return (T) new ServiceRepository(singletonCImpl.serviceOrderDao(), singletonCImpl.serviceSparePartDao());

          case 8: // com.gestion.itinerario.data.repository.InventoryRepository 
          return (T) new InventoryRepository(singletonCImpl.sparePartDao(), singletonCImpl.stockMovementDao());

          case 9: // com.gestion.itinerario.data.repository.PreferencesRepository 
          return (T) new PreferencesRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
