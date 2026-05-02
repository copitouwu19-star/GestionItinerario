package com.gestion.itinerario.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gestion.itinerario.data.entity.MovementType;
import com.gestion.itinerario.data.entity.StockMovement;
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
public final class StockMovementDao_Impl implements StockMovementDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockMovement> __insertionAdapterOfStockMovement;

  private final Converters __converters = new Converters();

  public StockMovementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockMovement = new EntityInsertionAdapter<StockMovement>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `stock_movements` (`id`,`sparePartId`,`type`,`quantity`,`notes`,`date`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockMovement entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSparePartId());
        final String _tmp = __converters.fromMovementType(entity.getType());
        statement.bindString(3, _tmp);
        statement.bindLong(4, entity.getQuantity());
        statement.bindString(5, entity.getNotes());
        statement.bindLong(6, entity.getDate());
      }
    };
  }

  @Override
  public Object insert(final StockMovement movement, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockMovement.insert(movement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<StockMovement>> getBySpare(final long sparePartId) {
    final String _sql = "SELECT * FROM stock_movements WHERE sparePartId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sparePartId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"stock_movements"}, new Callable<List<StockMovement>>() {
      @Override
      @NonNull
      public List<StockMovement> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSparePartId = CursorUtil.getColumnIndexOrThrow(_cursor, "sparePartId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final List<StockMovement> _result = new ArrayList<StockMovement>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockMovement _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSparePartId;
            _tmpSparePartId = _cursor.getLong(_cursorIndexOfSparePartId);
            final MovementType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toMovementType(_tmp);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            _item = new StockMovement(_tmpId,_tmpSparePartId,_tmpType,_tmpQuantity,_tmpNotes,_tmpDate);
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
