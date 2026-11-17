package com.example.habittracker.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
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
public final class HabitDao_Impl implements HabitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HabitEntity> __insertionAdapterOfHabitEntity;

  private final EntityInsertionAdapter<CompletionEntity> __insertionAdapterOfCompletionEntity;

  private final EntityDeletionOrUpdateAdapter<HabitEntity> __updateAdapterOfHabitEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteHabit;

  private final SharedSQLiteStatement __preparedStmtOfSetArchived;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCompletion;

  public HabitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHabitEntity = new EntityInsertionAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `habits` (`id`,`name`,`notes`,`emoji`,`colorIndex`,`scheduleType`,`daysOfWeekMask`,`timesPerWeek`,`createdAtEpochDay`,`isArchived`,`sortOrder`,`reminderEnabled`,`reminderMinuteOfDay`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getNotes());
        statement.bindString(4, entity.getEmoji());
        statement.bindLong(5, entity.getColorIndex());
        statement.bindString(6, entity.getScheduleType());
        statement.bindLong(7, entity.getDaysOfWeekMask());
        statement.bindLong(8, entity.getTimesPerWeek());
        statement.bindLong(9, entity.getCreatedAtEpochDay());
        final int _tmp = entity.isArchived() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getSortOrder());
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getReminderMinuteOfDay());
      }
    };
    this.__insertionAdapterOfCompletionEntity = new EntityInsertionAdapter<CompletionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `completions` (`habitId`,`dateEpochDay`,`loggedAtMillis`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CompletionEntity entity) {
        statement.bindLong(1, entity.getHabitId());
        statement.bindLong(2, entity.getDateEpochDay());
        statement.bindLong(3, entity.getLoggedAtMillis());
      }
    };
    this.__updateAdapterOfHabitEntity = new EntityDeletionOrUpdateAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `habits` SET `id` = ?,`name` = ?,`notes` = ?,`emoji` = ?,`colorIndex` = ?,`scheduleType` = ?,`daysOfWeekMask` = ?,`timesPerWeek` = ?,`createdAtEpochDay` = ?,`isArchived` = ?,`sortOrder` = ?,`reminderEnabled` = ?,`reminderMinuteOfDay` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getNotes());
        statement.bindString(4, entity.getEmoji());
        statement.bindLong(5, entity.getColorIndex());
        statement.bindString(6, entity.getScheduleType());
        statement.bindLong(7, entity.getDaysOfWeekMask());
        statement.bindLong(8, entity.getTimesPerWeek());
        statement.bindLong(9, entity.getCreatedAtEpochDay());
        final int _tmp = entity.isArchived() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getSortOrder());
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getReminderMinuteOfDay());
        statement.bindLong(14, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteHabit = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM habits WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetArchived = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE habits SET isArchived = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCompletion = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM completions WHERE habitId = ? AND dateEpochDay = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertHabit(final HabitEntity habit, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfHabitEntity.insertAndReturnId(habit);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCompletion(final CompletionEntity completion,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCompletionEntity.insert(completion);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateHabit(final HabitEntity habit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfHabitEntity.handle(habit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHabit(final long habitId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteHabit.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, habitId);
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
          __preparedStmtOfDeleteHabit.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setArchived(final long habitId, final boolean archived,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetArchived.acquire();
        int _argIndex = 1;
        final int _tmp = archived ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, habitId);
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
          __preparedStmtOfSetArchived.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCompletion(final long habitId, final long epochDay,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCompletion.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, habitId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, epochDay);
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
          __preparedStmtOfDeleteCompletion.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HabitEntity>> observeActiveHabits() {
    final String _sql = "SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habits"}, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "emoji");
          final int _cursorIndexOfColorIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorIndex");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfDaysOfWeekMask = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeekMask");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfCreatedAtEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochDay");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderMinuteOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinuteOfDay");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpEmoji;
            _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
            final int _tmpColorIndex;
            _tmpColorIndex = _cursor.getInt(_cursorIndexOfColorIndex);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final int _tmpDaysOfWeekMask;
            _tmpDaysOfWeekMask = _cursor.getInt(_cursorIndexOfDaysOfWeekMask);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final long _tmpCreatedAtEpochDay;
            _tmpCreatedAtEpochDay = _cursor.getLong(_cursorIndexOfCreatedAtEpochDay);
            final boolean _tmpIsArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final int _tmpReminderMinuteOfDay;
            _tmpReminderMinuteOfDay = _cursor.getInt(_cursorIndexOfReminderMinuteOfDay);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpNotes,_tmpEmoji,_tmpColorIndex,_tmpScheduleType,_tmpDaysOfWeekMask,_tmpTimesPerWeek,_tmpCreatedAtEpochDay,_tmpIsArchived,_tmpSortOrder,_tmpReminderEnabled,_tmpReminderMinuteOfDay);
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
  public Flow<List<HabitEntity>> observeArchivedHabits() {
    final String _sql = "SELECT * FROM habits WHERE isArchived = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habits"}, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "emoji");
          final int _cursorIndexOfColorIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorIndex");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfDaysOfWeekMask = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeekMask");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfCreatedAtEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochDay");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderMinuteOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinuteOfDay");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpEmoji;
            _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
            final int _tmpColorIndex;
            _tmpColorIndex = _cursor.getInt(_cursorIndexOfColorIndex);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final int _tmpDaysOfWeekMask;
            _tmpDaysOfWeekMask = _cursor.getInt(_cursorIndexOfDaysOfWeekMask);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final long _tmpCreatedAtEpochDay;
            _tmpCreatedAtEpochDay = _cursor.getLong(_cursorIndexOfCreatedAtEpochDay);
            final boolean _tmpIsArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final int _tmpReminderMinuteOfDay;
            _tmpReminderMinuteOfDay = _cursor.getInt(_cursorIndexOfReminderMinuteOfDay);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpNotes,_tmpEmoji,_tmpColorIndex,_tmpScheduleType,_tmpDaysOfWeekMask,_tmpTimesPerWeek,_tmpCreatedAtEpochDay,_tmpIsArchived,_tmpSortOrder,_tmpReminderEnabled,_tmpReminderMinuteOfDay);
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
  public Flow<HabitEntity> observeHabit(final long habitId) {
    final String _sql = "SELECT * FROM habits WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habits"}, new Callable<HabitEntity>() {
      @Override
      @Nullable
      public HabitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "emoji");
          final int _cursorIndexOfColorIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorIndex");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfDaysOfWeekMask = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeekMask");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfCreatedAtEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochDay");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderMinuteOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinuteOfDay");
          final HabitEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpEmoji;
            _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
            final int _tmpColorIndex;
            _tmpColorIndex = _cursor.getInt(_cursorIndexOfColorIndex);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final int _tmpDaysOfWeekMask;
            _tmpDaysOfWeekMask = _cursor.getInt(_cursorIndexOfDaysOfWeekMask);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final long _tmpCreatedAtEpochDay;
            _tmpCreatedAtEpochDay = _cursor.getLong(_cursorIndexOfCreatedAtEpochDay);
            final boolean _tmpIsArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final int _tmpReminderMinuteOfDay;
            _tmpReminderMinuteOfDay = _cursor.getInt(_cursorIndexOfReminderMinuteOfDay);
            _result = new HabitEntity(_tmpId,_tmpName,_tmpNotes,_tmpEmoji,_tmpColorIndex,_tmpScheduleType,_tmpDaysOfWeekMask,_tmpTimesPerWeek,_tmpCreatedAtEpochDay,_tmpIsArchived,_tmpSortOrder,_tmpReminderEnabled,_tmpReminderMinuteOfDay);
          } else {
            _result = null;
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
  public Object getHabit(final long habitId, final Continuation<? super HabitEntity> $completion) {
    final String _sql = "SELECT * FROM habits WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HabitEntity>() {
      @Override
      @Nullable
      public HabitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "emoji");
          final int _cursorIndexOfColorIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorIndex");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfDaysOfWeekMask = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeekMask");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfCreatedAtEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochDay");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderMinuteOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinuteOfDay");
          final HabitEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpEmoji;
            _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
            final int _tmpColorIndex;
            _tmpColorIndex = _cursor.getInt(_cursorIndexOfColorIndex);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final int _tmpDaysOfWeekMask;
            _tmpDaysOfWeekMask = _cursor.getInt(_cursorIndexOfDaysOfWeekMask);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final long _tmpCreatedAtEpochDay;
            _tmpCreatedAtEpochDay = _cursor.getLong(_cursorIndexOfCreatedAtEpochDay);
            final boolean _tmpIsArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final int _tmpReminderMinuteOfDay;
            _tmpReminderMinuteOfDay = _cursor.getInt(_cursorIndexOfReminderMinuteOfDay);
            _result = new HabitEntity(_tmpId,_tmpName,_tmpNotes,_tmpEmoji,_tmpColorIndex,_tmpScheduleType,_tmpDaysOfWeekMask,_tmpTimesPerWeek,_tmpCreatedAtEpochDay,_tmpIsArchived,_tmpSortOrder,_tmpReminderEnabled,_tmpReminderMinuteOfDay);
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
  public Object getActiveHabits(final Continuation<? super List<HabitEntity>> $completion) {
    final String _sql = "SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "emoji");
          final int _cursorIndexOfColorIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorIndex");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfDaysOfWeekMask = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeekMask");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfCreatedAtEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochDay");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderMinuteOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinuteOfDay");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpEmoji;
            _tmpEmoji = _cursor.getString(_cursorIndexOfEmoji);
            final int _tmpColorIndex;
            _tmpColorIndex = _cursor.getInt(_cursorIndexOfColorIndex);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final int _tmpDaysOfWeekMask;
            _tmpDaysOfWeekMask = _cursor.getInt(_cursorIndexOfDaysOfWeekMask);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final long _tmpCreatedAtEpochDay;
            _tmpCreatedAtEpochDay = _cursor.getLong(_cursorIndexOfCreatedAtEpochDay);
            final boolean _tmpIsArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final int _tmpReminderMinuteOfDay;
            _tmpReminderMinuteOfDay = _cursor.getInt(_cursorIndexOfReminderMinuteOfDay);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpNotes,_tmpEmoji,_tmpColorIndex,_tmpScheduleType,_tmpDaysOfWeekMask,_tmpTimesPerWeek,_tmpCreatedAtEpochDay,_tmpIsArchived,_tmpSortOrder,_tmpReminderEnabled,_tmpReminderMinuteOfDay);
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

  @Override
  public Flow<List<CompletionEntity>> observeAllCompletions() {
    final String _sql = "SELECT * FROM completions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completions"}, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfLoggedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedAtMillis");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final long _tmpLoggedAtMillis;
            _tmpLoggedAtMillis = _cursor.getLong(_cursorIndexOfLoggedAtMillis);
            _item = new CompletionEntity(_tmpHabitId,_tmpDateEpochDay,_tmpLoggedAtMillis);
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
  public Flow<List<CompletionEntity>> observeCompletions(final long habitId) {
    final String _sql = "SELECT * FROM completions WHERE habitId = ? ORDER BY dateEpochDay ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completions"}, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfLoggedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedAtMillis");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final long _tmpLoggedAtMillis;
            _tmpLoggedAtMillis = _cursor.getLong(_cursorIndexOfLoggedAtMillis);
            _item = new CompletionEntity(_tmpHabitId,_tmpDateEpochDay,_tmpLoggedAtMillis);
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
  public Object getCompletions(final long habitId,
      final Continuation<? super List<CompletionEntity>> $completion) {
    final String _sql = "SELECT * FROM completions WHERE habitId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfLoggedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedAtMillis");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final long _tmpLoggedAtMillis;
            _tmpLoggedAtMillis = _cursor.getLong(_cursorIndexOfLoggedAtMillis);
            _item = new CompletionEntity(_tmpHabitId,_tmpDateEpochDay,_tmpLoggedAtMillis);
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

  @Override
  public Flow<Integer> observeTotalCompletions() {
    final String _sql = "SELECT COUNT(*) FROM completions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completions"}, new Callable<Integer>() {
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
  public Object nextSortOrder(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM habits";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object isCompleted(final long habitId, final long epochDay,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM completions WHERE habitId = ? AND dateEpochDay = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, epochDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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
