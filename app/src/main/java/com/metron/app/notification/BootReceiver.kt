package com.metron.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.metron.app.MetronApp

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
                val repo = MetronApp.repository
                if (repo.isNotificationsEnabled.value) {
                    val hour = repo.dailyReminderHour.value
                    val minute = repo.dailyReminderMinute.value
                    MetronNotificationManager.initNotificationChannels(context)
                    MetronNotificationManager.scheduleDailyReminder(context, hour, minute)
                }
                if (repo.isAutoBackupEnabled.value) {
                    com.metron.app.backup.AutoBackupManager.scheduleWeeklyAutoBackup(context)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
