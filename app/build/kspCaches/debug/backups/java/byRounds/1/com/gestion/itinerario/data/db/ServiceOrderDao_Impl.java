package com.gestion.itinerario.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gestion.itinerario.data.entity.ServiceOrder;
import com.gestion.itinerario.data.entity.ServiceStatus;
import com.gestion.itinerario.data.entity.ServiceType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ServiceOrderDao_Impl implements ServiceOrderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ServiceOrder> __insertionAdapterOfServiceOrder;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ServiceOrder> __deletionAdapterOfServiceOrder;

  private final EntityDeletionOrUpdateAdapter<ServiceOrder> __updateAdapterOfServiceOrder;

  public ServiceOrderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfServiceOrder = new EntityInsertionAdapter<ServiceOrder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `service_orders` (`id`,`equipmentId`,`clientId`,`type`,`description`,`diagnosis`,`status`,`totalCost`,`createdAt`,`completedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServiceOrder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEquipmentId());
        statement.bindLong(3, entity.getClientId());
        final String _tmp = __converters.fromServiceType(entity.getType());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getDiagnosis());
        final String _tmp_1 = __converters.fromServiceStatus(entity.getStatus());
        statement.bindString(7, _tmp_1);
        statement.bindDouble(8, entity.getTotalCost());
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getCompletedAt());
        }
      }
    };
    this.__deletionAdapterOfServiceOrder = new EntityDeletionOrUpdateAdapter<ServiceOrder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `service_orders` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServiceOrder entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfServiceOrder = new EntityDeletionOrUpdateAdapter<ServiceOrder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `service_orders` SET `id` = ?,`equipmentId` = ?,`clientId` = ?,`type` = ?,`description` = ?,`diagnosis` = ?,`status` = ?,`totalCost` = ?,`createdAt` = ?,`completedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServiceOrder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEquipmentId());
        statement.bindLong(3, entity.getClientId());
        final String _tmp = __converters.fromServiceType(entity.getType());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getDiagnosis());
        final String _tmp_1 = __converters.fromServiceStatus(entity.getStatus());
        statement.bindString(7, _tmp_1);
        statement.bindDouble(8, entity.getTotalCost());
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getCompletedAt());
        }
        statement.bindLong(11, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ServiceOrder order, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfServiceOrder.insertAndReturnId(order);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ServiceOrder order, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfServiceOrder.handle(order);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ServiceOrder order, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfServiceOrder.handle(order);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ServiceOrder>> getAll() {
    final String _sql = "SELECT * FROM service_orders ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<List<ServiceOrder>>() {
      @Override
      @NonNull
      public List<ServiceOrder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<ServiceOrder> _result = new ArrayList<ServiceOrder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServiceOrder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final ServiceType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toServiceType(_tmp);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDiagnosis;
            _tmpDiagnosis = _cursor.getString(_cursorIndexOfDiagnosis);
            final ServiceStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toServiceStatus(_tmp_1);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new ServiceOrder(_tmpId,_tmpEquipmentId,_tmpClientId,_tmpType,_tmpDescription,_tmpDiagnosis,_tmpStatus,_tmpTotalCost,_tmpCreatedAt,_tmpCompletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super ServiceOrder> $completion) {
    final String _sql = "SELECT * FROM service_orders WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ServiceOrder>() {
      @Override
      @Nullable
      public ServiceOrder call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final ServiceOrder _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final ServiceType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toServiceType(_tmp);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDiagnosis;
            _tmpDiagnosis = _cursor.getString(_cursorIndexOfDiagnosis);
            final ServiceStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toServiceStatus(_tmp_1);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _result = new ServiceOrder(_tmpId,_tmpEquipmentId,_tmpClientId,_tmpType,_tmpDescription,_tmpDiagnosis,_tmpStatus,_tmpTotalCost,_tmpCreatedAt,_tmpCompletedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ServiceOrder>> getByEquipment(final long equipmentId) {
    final String _sql = "SELECT * FROM service_orders WHERE equipmentId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, equipmentId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<List<ServiceOrder>>() {
      @Override
      @NonNull
      public List<ServiceOrder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<ServiceOrder> _result = new ArrayList<ServiceOrder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServiceOrder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final ServiceType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toServiceType(_tmp);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDiagnosis;
            _tmpDiagnosis = _cursor.getString(_cursorIndexOfDiagnosis);
            final ServiceStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toServiceStatus(_tmp_1);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new ServiceOrder(_tmpId,_tmpEquipmentId,_tmpClientId,_tmpType,_tmpDescription,_tmpDiagnosis,_tmpStatus,_tmpTotalCost,_tmpCreatedAt,_tmpCompletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ServiceOrder>> getByClient(final long clientId) {
    final String _sql = "SELECT * FROM service_orders WHERE clientId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clientId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<List<ServiceOrder>>() {
      @Override
      @NonNull
      public List<ServiceOrder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<ServiceOrder> _result = new ArrayList<ServiceOrder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServiceOrder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final ServiceType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toServiceType(_tmp);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDiagnosis;
            _tmpDiagnosis = _cursor.getString(_cursorIndexOfDiagnosis);
            final ServiceStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toServiceStatus(_tmp_1);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new ServiceOrder(_tmpId,_tmpEquipmentId,_tmpClientId,_tmpType,_tmpDescription,_tmpDiagnosis,_tmpStatus,_tmpTotalCost,_tmpCreatedAt,_tmpCompletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ServiceOrder>> getByStatus(final ServiceStatus status) {
    final String _sql = "SELECT * FROM service_orders WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromServiceStatus(status);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<List<ServiceOrder>>() {
      @Override
      @NonNull
      public List<ServiceOrder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<ServiceOrder> _result = new ArrayList<ServiceOrder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServiceOrder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final ServiceType _tmpType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toServiceType(_tmp_1);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDiagnosis;
            _tmpDiagnosis = _cursor.getString(_cursorIndexOfDiagnosis);
            final ServiceStatus _tmpStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toServiceStatus(_tmp_2);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new ServiceOrder(_tmpId,_tmpEquipmentId,_tmpClientId,_tmpType,_tmpDescription,_tmpDiagnosis,_tmpStatus,_tmpTotalCost,_tmpCreatedAt,_tmpCompletedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> countActive() {
    final String _sql = "SELECT COUNT(*) FROM service_orders WHERE status != 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> countTotal() {
    final String _sql = "SELECT COUNT(*) FROM service_orders";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_orders"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
