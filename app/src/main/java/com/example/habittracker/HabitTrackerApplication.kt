package com.example.habittracker

import android.app.Application
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.local.HabitDatabase
import com.example.habittracker.notification.HabitNotifier
import com.example.habittracker.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

/** Minimal manual dependency container - no DI framework needed for an app this size. */
class AppContainer(private val application: Application) {
    private val database: HabitDatabase by lazy { HabitDatabase.getInstance(application) }
    val repository: HabitRepository by lazy { HabitRepository(database.habitDao()) }
    val notifier: HabitNotifier by lazy { HabitNotifier(application) }
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(application) }
}

class HabitTrackerApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifier.ensureChannel()
        syncRemindersWhenHabitsChange()
    }

    /**
     * Keeps the alarms in step with the database: every time a habit is added, edited,
     * archived or deleted the whole reminder set is re-applied.
     */
    private fun syncRemindersWhenHabitsChange() {
        container.repository.observeActiveHabits()
            .map { habits -> habits.map(::reminderKey) to habits }
            .distinctUntilChanged { old, new -> old.first == new.first }
            .map { (_, habits) -> habits }
            .onEach { habits -> container.reminderScheduler.syncAll(habits) }
            .launchIn(applicationScope)
    }

    private fun reminderKey(habit: com.example.habittracker.domain.Habit) = listOf(
        habit.id,
        habit.reminderEnabled,
        habit.reminderMinuteOfDay,
        habit.scheduleType,
        habit.daysOfWeek,
        habit.isArchived,
    )
}
