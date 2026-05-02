package com.gestion.itinerario.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gestion.itinerario.data.entity.ServiceSparePart;
import java.lang.Class;
import java.lang.Exception;
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
public final class ServiceSparePartDao_Impl implements ServiceSparePartDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ServiceSparePart> __insertionAdapterOfServiceSparePart;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOrder;

  public ServiceSparePartDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfServiceSparePart = new EntityInsertionAdapter<ServiceSparePart>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `service_spare_parts` (`serviceOrderId`,`sparePartId`,`quantity`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ServiceSparePart entity) {
        statement.bindLong(1, entity.getServiceOrderId());
        statement.bindLong(2, entity.getSparePartId());
        statement.bindLong(3, entity.getQuantity());
      }
    };
    this.__preparedStmtOfDeleteByOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM service_spare_parts WHERE serviceOrderId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ServiceSparePart item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfServiceSparePart.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByOrder(final long serviceOrderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByOrder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, serviceOrderId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ServiceSparePart>> getByOrder(final long serviceOrderId) {
    final String _sql = "SELECT * FROM service_spare_parts WHERE serviceOrderId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, serviceOrderId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"service_spare_parts"}, new Callable<List<ServiceSparePart>>() {
      @Override
      @NonNull
      public List<ServiceSparePart> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfServiceOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceOrderId");
          final int _cursorIndexOfSparePartId = CursorUtil.getColumnIndexOrThrow(_cursor, "sparePartId");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final List<ServiceSparePart> _result = new ArrayList<ServiceSparePart>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ServiceSparePart _item;
            final long _tmpServiceOrderId;
            _tmpServiceOrderId = _cursor.getLong(_cursorIndexOfServiceOrderId);
            final long _tmpSparePartId;
            _tmpSparePartId = _cursor.getLong(_cursorIndexOfSparePartId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item = new ServiceSparePart(_tmpServiceOrderId,_tmpSparePartId,_tmpQuantity);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
