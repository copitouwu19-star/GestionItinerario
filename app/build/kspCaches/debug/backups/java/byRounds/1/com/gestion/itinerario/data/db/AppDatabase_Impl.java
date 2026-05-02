package com.gestion.itinerario.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EquipmentDao _equipmentDao;

  private volatile ClientDao _clientDao;

  private volatile SparePartDao _sparePartDao;

  private volatile StockMovementDao _stockMovementDao;

  private volatile ServiceOrderDao _serviceOrderDao;

  private volatile ServiceSparePartDao _serviceSparePartDao;

  private volatile AppointmentDao _appointmentDao;

  private volatile MaintenanceReminderDao _maintenanceReminderDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `equipment` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `brand` TEXT NOT NULL, `model` TEXT NOT NULL, `serial` TEXT NOT NULL, `status` TEXT NOT NULL, `clientId` INTEGER, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `clients` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `lastName` TEXT NOT NULL, `phone` TEXT NOT NULL, `email` TEXT NOT NULL, `address` TEXT NOT NULL, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `spare_parts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `minStock` INTEGER NOT NULL, `unit` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_movements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sparePartId` INTEGER NOT NULL, `type` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `notes` TEXT NOT NULL, `date` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `service_orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `equipmentId` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `type` TEXT NOT NULL, `description` TEXT NOT NULL, `diagnosis` TEXT NOT NULL, `status` TEXT NOT NULL, `totalCost` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `service_spare_parts` (`serviceOrderId` INTEGER NOT NULL, `sparePartId` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, PRIMARY KEY(`serviceOrderId`, `sparePartId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `appointments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientId` INTEGER NOT NULL, `equipmentId` INTEGER, `dateTime` INTEGER NOT NULL, `serviceType` TEXT NOT NULL, `status` TEXT NOT NULL, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `equipmentId` INTEGER NOT NULL, `intervalMonths` INTEGER NOT NULL, `lastServiceDate` INTEGER NOT NULL, `nextServiceDate` INTEGER NOT NULL, `notes` TEXT NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '14063736081aeaf0f13ba80c06e80c22')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `equipment`");
        db.execSQL("DROP TABLE IF EXISTS `clients`");
        db.execSQL("DROP TABLE IF EXISTS `spare_parts`");
        db.execSQL("DROP TABLE IF EXISTS `stock_movements`");
        db.execSQL("DROP TABLE IF EXISTS `service_orders`");
        db.execSQL("DROP TABLE IF EXISTS `service_spare_parts`");
        db.execSQL("DROP TABLE IF EXISTS `appointments`");
        db.execSQL("DROP TABLE IF EXISTS `maintenance_reminders`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsEquipment = new HashMap<String, TableInfo.Column>(8);
        _columnsEquipment.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("brand", new TableInfo.Column("brand", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("model", new TableInfo.Column("model", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("serial", new TableInfo.Column("serial", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("clientId", new TableInfo.Column("clientId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEquipment.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEquipment = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEquipment = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEquipment = new TableInfo("equipment", _columnsEquipment, _foreignKeysEquipment, _indicesEquipment);
        final TableInfo _existingEquipment = TableInfo.read(db, "equipment");
        if (!_infoEquipment.equals(_existingEquipment)) {
          return new RoomOpenHelper.ValidationResult(false, "equipment(com.gestion.itinerario.data.entity.Equipment).\n"
                  + " Expected:\n" + _infoEquipment + "\n"
                  + " Found:\n" + _existingEquipment);
        }
        final HashMap<String, TableInfo.Column> _columnsClients = new HashMap<String, TableInfo.Column>(8);
        _columnsClients.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("lastName", new TableInfo.Column("lastName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClients.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysClients = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesClients = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoClients = new TableInfo("clients", _columnsClients, _foreignKeysClients, _indicesClients);
        final TableInfo _existingClients = TableInfo.read(db, "clients");
        if (!_infoClients.equals(_existingClients)) {
          return new RoomOpenHelper.ValidationResult(false, "clients(com.gestion.itinerario.data.entity.Client).\n"
                  + " Expected:\n" + _infoClients + "\n"
                  + " Found:\n" + _existingClients);
        }
        final HashMap<String, TableInfo.Column> _columnsSpareParts = new HashMap<String, TableInfo.Column>(7);
        _columnsSpareParts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("minStock", new TableInfo.Column("minStock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpareParts.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSpareParts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSpareParts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSpareParts = new TableInfo("spare_parts", _columnsSpareParts, _foreignKeysSpareParts, _indicesSpareParts);
        final TableInfo _existingSpareParts = TableInfo.read(db, "spare_parts");
        if (!_infoSpareParts.equals(_existingSpareParts)) {
          return new RoomOpenHelper.ValidationResult(false, "spare_parts(com.gestion.itinerario.data.entity.SparePart).\n"
                  + " Expected:\n" + _infoSpareParts + "\n"
                  + " Found:\n" + _existingSpareParts);
        }
        final HashMap<String, TableInfo.Column> _columnsStockMovements = new HashMap<String, TableInfo.Column>(6);
        _columnsStockMovements.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("sparePartId", new TableInfo.Column("sparePartId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockMovements = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockMovements = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockMovements = new TableInfo("stock_movements", _columnsStockMovements, _foreignKeysStockMovements, _indicesStockMovements);
        final TableInfo _existingStockMovements = TableInfo.read(db, "stock_movements");
        if (!_infoStockMovements.equals(_existingStockMovements)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_movements(com.gestion.itinerario.data.entity.StockMovement).\n"
                  + " Expected:\n" + _infoStockMovements + "\n"
                  + " Found:\n" + _existingStockMovements);
        }
        final HashMap<String, TableInfo.Column> _columnsServiceOrders = new HashMap<String, TableInfo.Column>(10);
        _columnsServiceOrders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("equipmentId", new TableInfo.Column("equipmentId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("clientId", new TableInfo.Column("clientId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("diagnosis", new TableInfo.Column("diagnosis", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("totalCost", new TableInfo.Column("totalCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceOrders.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServiceOrders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesServiceOrders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoServiceOrders = new TableInfo("service_orders", _columnsServiceOrders, _foreignKeysServiceOrders, _indicesServiceOrders);
        final TableInfo _existingServiceOrders = TableInfo.read(db, "service_orders");
        if (!_infoServiceOrders.equals(_existingServiceOrders)) {
          return new RoomOpenHelper.ValidationResult(false, "service_orders(com.gestion.itinerario.data.entity.ServiceOrder).\n"
                  + " Expected:\n" + _infoServiceOrders + "\n"
                  + " Found:\n" + _existingServiceOrders);
        }
        final HashMap<String, TableInfo.Column> _columnsServiceSpareParts = new HashMap<String, TableInfo.Column>(3);
        _columnsServiceSpareParts.put("serviceOrderId", new TableInfo.Column("serviceOrderId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceSpareParts.put("sparePartId", new TableInfo.Column("sparePartId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServiceSpareParts.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServiceSpareParts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesServiceSpareParts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoServiceSpareParts = new TableInfo("service_spare_parts", _columnsServiceSpareParts, _foreignKeysServiceSpareParts, _indicesServiceSpareParts);
        final TableInfo _existingServiceSpareParts = TableInfo.read(db, "service_spare_parts");
        if (!_infoServiceSpareParts.equals(_existingServiceSpareParts)) {
          return new RoomOpenHelper.ValidationResult(false, "service_spare_parts(com.gestion.itinerario.data.entity.ServiceSparePart).\n"
                  + " Expected:\n" + _infoServiceSpareParts + "\n"
                  + " Found:\n" + _existingServiceSpareParts);
        }
        final HashMap<String, TableInfo.Column> _columnsAppointments = new HashMap<String, TableInfo.Column>(8);
        _columnsAppointments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("clientId", new TableInfo.Column("clientId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("equipmentId", new TableInfo.Column("equipmentId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("dateTime", new TableInfo.Column("dateTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("serviceType", new TableInfo.Column("serviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppointments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppointments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppointments = new TableInfo("appointments", _columnsAppointments, _foreignKeysAppointments, _indicesAppointments);
        final TableInfo _existingAppointments = TableInfo.read(db, "appointments");
        if (!_infoAppointments.equals(_existingAppointments)) {
          return new RoomOpenHelper.ValidationResult(false, "appointments(com.gestion.itinerario.data.entity.Appointment).\n"
                  + " Expected:\n" + _infoAppointments + "\n"
                  + " Found:\n" + _existingAppointments);
        }
        final HashMap<String, TableInfo.Column> _columnsMaintenanceReminders = new HashMap<String, TableInfo.Column>(7);
        _columnsMaintenanceReminders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("equipmentId", new TableInfo.Column("equipmentId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("intervalMonths", new TableInfo.Column("intervalMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("lastServiceDate", new TableInfo.Column("lastServiceDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("nextServiceDate", new TableInfo.Column("nextServiceDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceReminders.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMaintenanceReminders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMaintenanceReminders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMaintenanceReminders = new TableInfo("maintenance_reminders", _columnsMaintenanceReminders, _foreignKeysMaintenanceReminders, _indicesMaintenanceReminders);
        final TableInfo _existingMaintenanceReminders = TableInfo.read(db, "maintenance_reminders");
        if (!_infoMaintenanceReminders.equals(_existingMaintenanceReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "maintenance_reminders(com.gestion.itinerario.data.entity.MaintenanceReminder).\n"
                  + " Expected:\n" + _infoMaintenanceReminders + "\n"
                  + " Found:\n" + _existingMaintenanceReminders);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "14063736081aeaf0f13ba80c06e80c22", "dfd16e9fef7643ed92707f9f7f12ef9a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "equipment","clients","spare_parts","stock_movements","service_orders","service_spare_parts","appointments","maintenance_reminders");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `equipment`");
      _db.execSQL("DELETE FROM `clients`");
      _db.execSQL("DELETE FROM `spare_parts`");
      _db.execSQL("DELETE FROM `stock_movements`");
      _db.execSQL("DELETE FROM `service_orders`");
      _db.execSQL("DELETE FROM `service_spare_parts`");
      _db.execSQL("DELETE FROM `appointments`");
      _db.execSQL("DELETE FROM `maintenance_reminders`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EquipmentDao.class, EquipmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ClientDao.class, ClientDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SparePartDao.class, SparePartDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockMovementDao.class, StockMovementDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ServiceOrderDao.class, ServiceOrderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ServiceSparePartDao.class, ServiceSparePartDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppointmentDao.class, AppointmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MaintenanceReminderDao.class, MaintenanceReminderDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public EquipmentDao equipmentDao() {
    if (_equipmentDao != null) {
      return _equipmentDao;
    } else {
      synchronized(this) {
        if(_equipmentDao == null) {
          _equipmentDao = new EquipmentDao_Impl(this);
        }
        return _equipmentDao;
      }
    }
  }

  @Override
  public ClientDao clientDao() {
    if (_clientDao != null) {
      return _clientDao;
    } else {
      synchronized(this) {
        if(_clientDao == null) {
          _clientDao = new ClientDao_Impl(this);
        }
        return _clientDao;
      }
    }
  }

  @Override
  public SparePartDao sparePartDao() {
    if (_sparePartDao != null) {
      return _sparePartDao;
    } else {
      synchronized(this) {
        if(_sparePartDao == null) {
          _sparePartDao = new SparePartDao_Impl(this);
        }
        return _sparePartDao;
      }
    }
  }

  @Override
  public StockMovementDao stockMovementDao() {
    if (_stockMovementDao != null) {
      return _stockMovementDao;
    } else {
      synchronized(this) {
        if(_stockMovementDao == null) {
          _stockMovementDao = new StockMovementDao_Impl(this);
        }
        return _stockMovementDao;
      }
    }
  }

  @Override
  public ServiceOrderDao serviceOrderDao() {
    if (_serviceOrderDao != null) {
      return _serviceOrderDao;
    } else {
      synchronized(this) {
        if(_serviceOrderDao == null) {
          _serviceOrderDao = new ServiceOrderDao_Impl(this);
        }
        return _serviceOrderDao;
      }
    }
  }

  @Override
  public ServiceSparePartDao serviceSparePartDao() {
    if (_serviceSparePartDao != null) {
      return _serviceSparePartDao;
    } else {
      synchronized(this) {
        if(_serviceSparePartDao == null) {
          _serviceSparePartDao = new ServiceSparePartDao_Impl(this);
        }
        return _serviceSparePartDao;
      }
    }
  }

  @Override
  public AppointmentDao appointmentDao() {
    if (_appointmentDao != null) {
      return _appointmentDao;
    } else {
      synchronized(this) {
        if(_appointmentDao == null) {
          _appointmentDao = new AppointmentDao_Impl(this);
        }
        return _appointmentDao;
      }
    }
  }

  @Override
  public MaintenanceReminderDao maintenanceReminderDao() {
    if (_maintenanceReminderDao != null) {
      return _maintenanceReminderDao;
    } else {
      synchronized(this) {
        if(_maintenanceReminderDao == null) {
          _maintenanceReminderDao = new MaintenanceReminderDao_Impl(this);
        }
        return _maintenanceReminderDao;
      }
    }
  }
}
