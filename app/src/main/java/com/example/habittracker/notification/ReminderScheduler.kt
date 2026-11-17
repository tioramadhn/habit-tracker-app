package com.example.habittracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ReminderPlanner
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Schedules one alarm per habit with an enabled reminder.
 *
 * Alarms are inexact ([AlarmManager.setAndAllowWhileIdle]) so the app needs no exact-alarm
 * permission; the system may shift them by a few minutes, which is fine for a habit nudge.
 * After each alarm fires the receiver schedules the following one.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Ids with a live alarm, so habits that disappear can be cancelled on the next sync. */
    private val scheduledIds = mutableSetOf<Long>()

    fun schedule(habit: Habit, now: LocalDateTime = LocalDateTime.now()) {
        cancel(habit.id)
        val trigger = ReminderPlanner.nextTrigger(habit, now) ?: return
        scheduledIds += habit.id
        val triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent(habit.id),
            )
        }.onFailure { Log.w(TAG, "Gagal menjadwalkan pengingat untuk habit ${habit.id}", it) }
    }

    fun cancel(habitId: Long) {
        alarmManager.cancel(pendingIntent(habitId))
        scheduledIds -= habitId
    }

    /** Re-applies alarms for the given habits and drops alarms for habits that lost theirs. */
    fun syncAll(habits: List<Habit>, now: LocalDateTime = LocalDateTime.now()) {
        val stillPresent = habits.mapTo(mutableSetOf()) { it.id }
        // Habits that were deleted or archived since the last sync lose their alarm.
        (scheduledIds - stillPresent).forEach(::cancel)
        habits.forEach { habit ->
            if (habit.reminderEnabled && !habit.isArchived) schedule(habit, now) else cancel(habit.id)
        }
    }

    private fun pendingIntent(habitId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        ReminderReceiver.fireIntent(context, habitId),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val TAG = "ReminderScheduler"
    }
}
