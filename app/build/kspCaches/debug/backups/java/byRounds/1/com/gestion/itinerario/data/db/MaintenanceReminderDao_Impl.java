package com.gestion.itinerario.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gestion.itinerario.data.entity.MaintenanceReminder;
import java.lang.Class;
import java.lang.Exception;
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
public final class MaintenanceReminderDao_Impl implements MaintenanceReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MaintenanceReminder> __insertionAdapterOfMaintenanceReminder;

  private final EntityDeletionOrUpdateAdapter<MaintenanceReminder> __deletionAdapterOfMaintenanceReminder;

  private final EntityDeletionOrUpdateAdapter<MaintenanceReminder> __updateAdapterOfMaintenanceReminder;

  public MaintenanceReminderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMaintenanceReminder = new EntityInsertionAdapter<MaintenanceReminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `maintenance_reminders` (`id`,`equipmentId`,`intervalMonths`,`lastServiceDate`,`nextServiceDate`,`notes`,`isActive`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MaintenanceReminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEquipmentId());
        statement.bindLong(3, entity.getIntervalMonths());
        statement.bindLong(4, entity.getLastServiceDate());
        statement.bindLong(5, entity.getNextServiceDate());
        statement.bindString(6, entity.getNotes());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__deletionAdapterOfMaintenanceReminder = new EntityDeletionOrUpdateAdapter<MaintenanceReminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `maintenance_reminders` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MaintenanceReminder entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMaintenanceReminder = new EntityDeletionOrUpdateAdapter<MaintenanceReminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `maintenance_reminders` SET `id` = ?,`equipmentId` = ?,`intervalMonths` = ?,`lastServiceDate` = ?,`nextServiceDate` = ?,`notes` = ?,`isActive` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MaintenanceReminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEquipmentId());
        statement.bindLong(3, entity.getIntervalMonths());
        statement.bindLong(4, entity.getLastServiceDate());
        statement.bindLong(5, entity.getNextServiceDate());
        statement.bindString(6, entity.getNotes());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final MaintenanceReminder reminder,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMaintenanceReminder.insertAndReturnId(reminder);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final MaintenanceReminder reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMaintenanceReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final MaintenanceReminder reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMaintenanceReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MaintenanceReminder>> getActive() {
    final String _sql = "SELECT * FROM maintenance_reminders WHERE isActive = 1 ORDER BY nextServiceDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"maintenance_reminders"}, new Callable<List<MaintenanceReminder>>() {
      @Override
      @NonNull
      public List<MaintenanceReminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfIntervalMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMonths");
          final int _cursorIndexOfLastServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastServiceDate");
          final int _cursorIndexOfNextServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextServiceDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<MaintenanceReminder> _result = new ArrayList<MaintenanceReminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaintenanceReminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final int _tmpIntervalMonths;
            _tmpIntervalMonths = _cursor.getInt(_cursorIndexOfIntervalMonths);
            final long _tmpLastServiceDate;
            _tmpLastServiceDate = _cursor.getLong(_cursorIndexOfLastServiceDate);
            final long _tmpNextServiceDate;
            _tmpNextServiceDate = _cursor.getLong(_cursorIndexOfNextServiceDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new MaintenanceReminder(_tmpId,_tmpEquipmentId,_tmpIntervalMonths,_tmpLastServiceDate,_tmpNextServiceDate,_tmpNotes,_tmpIsActive);
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
  public Flow<List<MaintenanceReminder>> getByEquipment(final long equipmentId) {
    final String _sql = "SELECT * FROM maintenance_reminders WHERE equipmentId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, equipmentId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"maintenance_reminders"}, new Callable<List<MaintenanceReminder>>() {
      @Override
      @NonNull
      public List<MaintenanceReminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfIntervalMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMonths");
          final int _cursorIndexOfLastServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastServiceDate");
          final int _cursorIndexOfNextServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextServiceDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<MaintenanceReminder> _result = new ArrayList<MaintenanceReminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaintenanceReminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final int _tmpIntervalMonths;
            _tmpIntervalMonths = _cursor.getInt(_cursorIndexOfIntervalMonths);
            final long _tmpLastServiceDate;
            _tmpLastServiceDate = _cursor.getLong(_cursorIndexOfLastServiceDate);
            final long _tmpNextServiceDate;
            _tmpNextServiceDate = _cursor.getLong(_cursorIndexOfNextServiceDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new MaintenanceReminder(_tmpId,_tmpEquipmentId,_tmpIntervalMonths,_tmpLastServiceDate,_tmpNextServiceDate,_tmpNotes,_tmpIsActive);
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
  public Object getDue(final long date,
      final Continuation<? super List<MaintenanceReminder>> $completion) {
    final String _sql = "SELECT * FROM maintenance_reminders WHERE nextServiceDate <= ? AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MaintenanceReminder>>() {
      @Override
      @NonNull
      public List<MaintenanceReminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEquipmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "equipmentId");
          final int _cursorIndexOfIntervalMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMonths");
          final int _cursorIndexOfLastServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastServiceDate");
          final int _cursorIndexOfNextServiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextServiceDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<MaintenanceReminder> _result = new ArrayList<MaintenanceReminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaintenanceReminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEquipmentId;
            _tmpEquipmentId = _cursor.getLong(_cursorIndexOfEquipmentId);
            final int _tmpIntervalMonths;
            _tmpIntervalMonths = _cursor.getInt(_cursorIndexOfIntervalMonths);
            final long _tmpLastServiceDate;
            _tmpLastServiceDate = _cursor.getLong(_cursorIndexOfLastServiceDate);
            final long _tmpNextServiceDate;
            _tmpNextServiceDate = _cursor.getLong(_cursorIndexOfNextServiceDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new MaintenanceReminder(_tmpId,_tmpEquipmentId,_tmpIntervalMonths,_tmpLastServiceDate,_tmpNextServiceDate,_tmpNotes,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
