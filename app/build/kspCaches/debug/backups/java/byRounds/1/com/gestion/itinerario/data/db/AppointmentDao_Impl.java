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
import com.gestion.itinerario.data.entity.Appointment;
import com.gestion.itinerario.data.entity.AppointmentStatus;
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
public final class AppointmentDao_Impl implements AppointmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Appointment> __insertionAdapterOfAppointment;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Appointment> __deletionAdapterOfAppointment;

  private final EntityDeletionOrUpdateAdapter<Appointment> __updateAdapterOfAppointment;

  public AppointmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppointment = new EntityInsertionAdapter<Appointment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `appointments` (`id`,`clientId`,`equipmentId`,`dateTime`,`serviceType`,`status`,`notes`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Appointment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClientId());
        if (entity.getEquipmentId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEquipmentId());
        }
        statement.bindLong(4, entity.getDateTime());
        final String _tmp = __converters.fromServiceType(entity.getServiceType());
        statement.bindString(5, _tmp);
        final String _tmp_1 = __converters.fromAppointmentStatus(entity.getStatus());
        statement.bindString(6, _tmp_1);
        statement.bindString(7, entity.getNotes());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfAppointment = new EntityDeletionOrUpdateAdapter<Appointment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `appointments` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Appointment entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAppointment = new EntityDeletionOrUpdateAdapter<Appointment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `appointments` SET `id` = ?,`clientId` = ?,`equipmentId` = ?,`dateTime` = ?,`serviceType` = ?,`status` = ?,`notes` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Appointment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClientId());
        if (entity.getEquipmentId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEquipmentId());
        }
        statement.bindLong(4, entity.getDateTime());
        final String _tmp = __converters.fromServiceType(entity.getServiceType());
        statement.bindString(5, _tmp);
        final String _tmp_1 = __converters.fromAppointmentStatus(entity.getStatus());
        statement.bindString(6, _tmp_1);
        statement.bindString(7, entity.getNotes());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Appointment appointment,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAppointment.insertAndReturnId(appointment);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Appointment appointment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAppointment.handle(appointment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Appointment appointment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppointment.handle(appointment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Appointment>> getAll() {
    final String _sql = "SELECT * FROM appointments ORDER BY dateTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"appointments"}, new Callable<List<Appointment>>() {
      @Override
      @NonNull
      public List<Appointment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfDateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTime");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Appointment> _result = new ArrayList<Appointment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Appointment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpEquipmentId;
            if (_cursor.isNull(_cursorIndexOfEquipmentId)) {
              _tmpEquipmentId = null;
            } else {
              _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            }
            final long _tmpDateTime;
            _tmpDateTime = _cursor.getLong(_cursorIndexOfDateTime);
            final ServiceType _tmpServiceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfServiceType);
            _tmpServiceType = __converters.toServiceType(_tmp);
            final AppointmentStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAppointmentStatus(_tmp_1);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Appointment(_tmpId,_tmpClientId,_tmpEquipmentId,_tmpDateTime,_tmpServiceType,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Object getById(final long id, final Continuation<? super Appointment> $completion) {
    final String _sql = "SELECT * FROM appointments WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Appointment>() {
      @Override
      @Nullable
      public Appointment call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfDateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTime");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Appointment _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpEquipmentId;
            if (_cursor.isNull(_cursorIndexOfEquipmentId)) {
              _tmpEquipmentId = null;
            } else {
              _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            }
            final long _tmpDateTime;
            _tmpDateTime = _cursor.getLong(_cursorIndexOfDateTime);
            final ServiceType _tmpServiceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfServiceType);
            _tmpServiceType = __converters.toServiceType(_tmp);
            final AppointmentStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAppointmentStatus(_tmp_1);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Appointment(_tmpId,_tmpClientId,_tmpEquipmentId,_tmpDateTime,_tmpServiceType,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Flow<List<Appointment>> getTodayAppointments(final long startOfDay, final long endOfDay) {
    final String _sql = "SELECT * FROM appointments WHERE dateTime >= ? AND dateTime <= ? AND status = 'SCHEDULED' ORDER BY dateTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"appointments"}, new Callable<List<Appointment>>() {
      @Override
      @NonNull
      public List<Appointment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfDateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTime");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Appointment> _result = new ArrayList<Appointment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Appointment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpEquipmentId;
            if (_cursor.isNull(_cursorIndexOfEquipmentId)) {
              _tmpEquipmentId = null;
            } else {
              _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            }
            final long _tmpDateTime;
            _tmpDateTime = _cursor.getLong(_cursorIndexOfDateTime);
            final ServiceType _tmpServiceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfServiceType);
            _tmpServiceType = __converters.toServiceType(_tmp);
            final AppointmentStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAppointmentStatus(_tmp_1);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Appointment(_tmpId,_tmpClientId,_tmpEquipmentId,_tmpDateTime,_tmpServiceType,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Flow<List<Appointment>> getUpcoming(final long from) {
    final String _sql = "SELECT * FROM appointments WHERE dateTime >= ? AND status = 'SCHEDULED' ORDER BY dateTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"appointments"}, new Callable<List<Appointment>>() {
      @Override
      @NonNull
      public List<Appointment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfDateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTime");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Appointment> _result = new ArrayList<Appointment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Appointment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpEquipmentId;
            if (_cursor.isNull(_cursorIndexOfEquipmentId)) {
              _tmpEquipmentId = null;
            } else {
              _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            }
            final long _tmpDateTime;
            _tmpDateTime = _cursor.getLong(_cursorIndexOfDateTime);
            final ServiceType _tmpServiceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfServiceType);
            _tmpServiceType = __converters.toServiceType(_tmp);
            final AppointmentStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toAppointmentStatus(_tmp_1);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Appointment(_tmpId,_tmpClientId,_tmpEquipmentId,_tmpDateTime,_tmpServiceType,_tmpStatus,_tmpNotes,_tmpCreatedAt);
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
  public Flow<Integer> countToday(final long startOfDay, final long endOfDay) {
    final String _sql = "SELECT COUNT(*) FROM appointments WHERE dateTime >= ? AND dateTime <= ? AND status = 'SCHEDULED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"appointments"}, new Callable<Integer>() {
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
