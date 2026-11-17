package com.example.habittracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.habittracker.MainActivity
import com.example.habittracker.R
import com.example.habittracker.domain.Habit
import java.time.LocalDate

/** Builds and posts the habit reminder notifications. */
class HabitNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pengingat habit",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifikasi pengingat untuk kebiasaan harianmu"
        }
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return manager.areNotificationsEnabled()
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED && manager.areNotificationsEnabled()
    }

    fun notifyReminder(habit: Habit, date: LocalDate) {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted || !manager.areNotificationsEnabled()) return
        ensureChannel()

        val openIntent = PendingIntent.getActivity(
            context,
            habit.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val markDoneIntent = PendingIntent.getBroadcast(
            context,
            MARK_DONE_REQUEST_OFFSET + habit.id.toInt(),
            ReminderReceiver.markDoneIntent(context, habit.id, date),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${habit.emoji}  ${habit.name}")
            .setContentText(reminderMessage(habit))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(0, "Tandai selesai", markDoneIntent)
            .build()

        runCatching { manager.notify(notificationId(habit.id), notification) }
    }

    fun cancel(habitId: Long) = manager.cancel(notificationId(habitId))

    private fun reminderMessage(habit: Habit): String = when {
        habit.notes.isNotBlank() -> habit.notes
        else -> "Waktunya menjalankan kebiasaan ini."
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        private const val MARK_DONE_REQUEST_OFFSET = 1_000_000

        fun notificationId(habitId: Long): Int = habitId.toInt()
    }
}
