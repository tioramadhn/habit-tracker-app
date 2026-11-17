package com.example.habittracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habittracker.domain.Habit
import java.time.LocalDate

@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var instance: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase = instance ?: synchronized(this) {
            instance ?: build(context).also { instance = it }
        }

        private fun build(context: Context): HabitDatabase =
            Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db")
                .addCallback(SeedCallback)
                .addMigrations(MIGRATION_1_2)
                .build()

        /** v2 adds the per-habit reminder settings. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE habits ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE habits ADD COLUMN reminderMinuteOfDay INTEGER NOT NULL " +
                        "DEFAULT ${Habit.DEFAULT_REMINDER_MINUTE}",
                )
            }
        }

        /** Inserts a few starter habits the first time the database is created. */
        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val today = LocalDate.now().toEpochDay()
                val samples = listOf(
                    Triple("Minum 2 liter air", "💧", 2),
                    Triple("Olahraga 30 menit", "🏃", 5),
                    Triple("Baca buku 10 halaman", "📖", 0),
                )
                samples.forEachIndexed { index, (name, emoji, color) ->
                    db.execSQL(
                        "INSERT INTO habits " +
                            "(name, notes, emoji, colorIndex, scheduleType, daysOfWeekMask, " +
                            "timesPerWeek, createdAtEpochDay, isArchived, sortOrder, " +
                            "reminderEnabled, reminderMinuteOfDay) " +
                            "VALUES (?, '', ?, ?, 'DAILY', ${HabitEntity.ALL_DAYS_MASK}, 3, ?, 0, ?, " +
                            "0, ${Habit.DEFAULT_REMINDER_MINUTE})",
                        arrayOf(name, emoji, color, today, index),
                    )
                }
            }
        }
    }
}
