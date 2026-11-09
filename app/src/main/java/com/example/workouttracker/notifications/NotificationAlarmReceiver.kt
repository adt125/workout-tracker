package com.example.workouttracker.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.workouttracker.MainActivity
import com.example.workouttracker.R

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            AppNotificationManager(context).scheduleAll()
            return
        }
        val type = intent.getStringExtra(AppNotificationManager.EXTRA_TYPE) ?: return
        val settingsManager = AppNotificationManager(context)
        val settings = settingsManager.getSettings()
        val enabled = type == AppNotificationManager.ALARM_CHECK_IN && settings.checkInEnabled ||
            type == AppNotificationManager.ALARM_WATER && settings.waterEnabled
        if (!enabled) return

        val isCheckIn = type == AppNotificationManager.ALARM_CHECK_IN
        val contentIntent = PendingIntent.getActivity(
            context,
            2300,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (android.os.Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(
                context,
                if (isCheckIn) AppNotificationManager.CHECK_IN_CHANNEL else AppNotificationManager.WATER_CHANNEL
            )
                .setSmallIcon(R.drawable.ic_app_logo)
                .setContentTitle(if (isCheckIn) "Daily check-in" else "Time for some water")
                .setContentText(if (isCheckIn) "Take a moment to log today’s progress." else "Log a glass and keep your hydration on track.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
            NotificationManagerCompat.from(context).notify(if (isCheckIn) 2400 else 2401, notification)
        }

        if (isCheckIn) settingsManager.schedule(AppNotificationManager.ALARM_CHECK_IN, 22, 0)
        else settingsManager.scheduleNextWaterReminder(settings)
    }
}
