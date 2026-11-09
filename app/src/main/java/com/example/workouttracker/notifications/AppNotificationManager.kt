package com.example.workouttracker.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.floor

data class NotificationSettings(
    val checkInEnabled: Boolean = true,
    val waterEnabled: Boolean = false,
    val waterStartHour: Int = 10,
    val waterStartMinute: Int = 0,
    val waterEndHour: Int = 22,
    val waterEndMinute: Int = 0,
    val waterIntervalMinutes: Int = 60
)

class AppNotificationManager(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    init {
        createNotificationChannels()
    }

    fun getSettings(): NotificationSettings = NotificationSettings(
        checkInEnabled = preferences.getBoolean(KEY_CHECK_IN, true),
        waterEnabled = preferences.getBoolean(KEY_WATER_ENABLED, false),
        waterStartHour = preferences.getInt(KEY_WATER_START_HOUR, 10),
        waterStartMinute = preferences.getInt(KEY_WATER_START_MINUTE, 0),
        waterEndHour = preferences.getInt(KEY_WATER_END_HOUR, 22),
        waterEndMinute = preferences.getInt(KEY_WATER_END_MINUTE, 0),
        waterIntervalMinutes = preferences.getInt(KEY_WATER_INTERVAL_MINUTES, 60)
    )

    fun saveSettings(settings: NotificationSettings) {
        preferences.edit()
            .putBoolean(KEY_CHECK_IN, settings.checkInEnabled)
            .putBoolean(KEY_WATER_ENABLED, settings.waterEnabled)
            .putInt(KEY_WATER_START_HOUR, settings.waterStartHour)
            .putInt(KEY_WATER_START_MINUTE, settings.waterStartMinute)
            .putInt(KEY_WATER_END_HOUR, settings.waterEndHour)
            .putInt(KEY_WATER_END_MINUTE, settings.waterEndMinute)
            .putInt(KEY_WATER_INTERVAL_MINUTES, settings.waterIntervalMinutes)
            .apply()
        scheduleAll(settings)
    }

    fun scheduleAll(settings: NotificationSettings = getSettings()) {
        if (settings.checkInEnabled) {
            schedule(ALARM_CHECK_IN, 22, 0)
        } else {
            cancel(ALARM_CHECK_IN)
        }
        if (settings.waterEnabled) {
            scheduleNextWaterReminder(settings)
        } else {
            cancel(ALARM_WATER)
        }
    }

    fun schedule(type: String, hour: Int, minute: Int) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).putExtra(EXTRA_TYPE, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(type),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val next = LocalDateTime.now()
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
            .let { if (!it.isAfter(LocalDateTime.now())) it.plusDays(1) else it }
        scheduleAt(type, next)
    }

    fun scheduleNextWaterReminder(settings: NotificationSettings = getSettings()) {
        val now = LocalDateTime.now()
        val start = now.withHour(settings.waterStartHour)
            .withMinute(settings.waterStartMinute).withSecond(0).withNano(0)
        val end = now.withHour(settings.waterEndHour)
            .withMinute(settings.waterEndMinute).withSecond(0).withNano(0)
        val interval = settings.waterIntervalMinutes.coerceAtLeast(15)

        val next = if (now.isBefore(start)) {
            start.plusMinutes(interval.toLong())
        } else {
            val elapsedMinutes = java.time.Duration.between(start, now).toMinutes()
            val nextOffset = (floor(elapsedMinutes.toDouble() / interval).toLong() + 1) * interval
            start.plusMinutes(nextOffset.toLong())
        }
        val scheduledTime = if (next.isAfter(end)) {
            start.plusDays(1).plusMinutes(interval.toLong())
        } else {
            next
        }
        scheduleAt(ALARM_WATER, scheduledTime)
    }

    private fun scheduleAt(type: String, next: LocalDateTime) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).putExtra(EXTRA_TYPE, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(type),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    fun cancel(type: String) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            requestCode(type),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )?.let(alarmManager::cancel)
    }

    private fun requestCode(type: String) = if (type == ALARM_CHECK_IN) 2200 else 2201

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(
                listOf(
                    NotificationChannel(CHECK_IN_CHANNEL, "Daily check-in", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(WATER_CHANNEL, "Water reminders", NotificationManager.IMPORTANCE_DEFAULT)
                )
            )
        }
    }

    companion object {
        const val EXTRA_TYPE = "notification_type"
        const val ALARM_CHECK_IN = "check_in"
        const val ALARM_WATER = "water"
        const val CHECK_IN_CHANNEL = "daily_check_in"
        const val WATER_CHANNEL = "water_reminders"
        private const val PREFERENCES_NAME = "notification_preferences"
        private const val KEY_CHECK_IN = "check_in_enabled"
        private const val KEY_WATER_ENABLED = "water_enabled"
        private const val KEY_WATER_START_HOUR = "water_start_hour"
        private const val KEY_WATER_START_MINUTE = "water_start_minute"
        private const val KEY_WATER_END_HOUR = "water_end_hour"
        private const val KEY_WATER_END_MINUTE = "water_end_minute"
        private const val KEY_WATER_INTERVAL_MINUTES = "water_interval_minutes"
    }
}
