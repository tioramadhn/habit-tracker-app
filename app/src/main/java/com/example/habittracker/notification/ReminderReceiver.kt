package com.example.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habittracker.HabitTrackerApplication
import com.example.habittracker.domain.ReminderPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Receives the reminder alarm, decides whether the nudge is still relevant, posts the
 * notification and queues the next occurrence. Also handles the notification's
 * "Tandai selesai" action.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? HabitTrackerApplication ?: return
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0L)
        if (habitId == 0L) return
        val action = intent.action
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_FIRE -> fire(app, habitId)
                    ACTION_MARK_DONE -> markDone(
                        app = app,
                        habitId = habitId,
                        date = LocalDate.ofEpochDay(
                            intent.getLongExtra(EXTRA_DATE, LocalDate.now().toEpochDay()),
                        ),
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun fire(app: HabitTrackerApplication, habitId: Long) {
        val container = app.container
        val habit = container.repository.getHabit(habitId) ?: return
        val today = LocalDate.now()
        val completed = container.repository.completionsOnce(habitId)

        if (ReminderPlanner.shouldNotify(habit, completed, today)) {
            container.notifier.notifyReminder(habit, today)
        }
        // Queue tomorrow's (or the next scheduled day's) reminder.
        container.reminderScheduler.schedule(habit, LocalDateTime.now())
    }

    private suspend fun markDone(app: HabitTrackerApplication, habitId: Long, date: LocalDate) {
        app.container.repository.setCompletion(habitId, date, completed = true)
        app.container.notifier.cancel(habitId)
    }

    companion object {
        private const val ACTION_FIRE = "com.example.habittracker.action.REMINDER_FIRE"
        private const val ACTION_MARK_DONE = "com.example.habittracker.action.REMINDER_MARK_DONE"
        private const val EXTRA_HABIT_ID = "habitId"
        private const val EXTRA_DATE = "dateEpochDay"

        fun fireIntent(context: Context, habitId: Long): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_FIRE
                putExtra(EXTRA_HABIT_ID, habitId)
            }

        fun markDoneIntent(context: Context, habitId: Long, date: LocalDate): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_MARK_DONE
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_DATE, date.toEpochDay())
            }
    }
}
