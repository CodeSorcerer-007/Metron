package com.metron.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.metron.app.MetronApp

class MetronAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_REMINDER = "com.metron.app.ACTION_DAILY_REMINDER"
        const val ACTION_BILL_REMINDER = "com.metron.app.ACTION_BILL_REMINDER"
        const val ACTION_AUTO_BACKUP = "com.metron.app.ACTION_AUTO_BACKUP"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val repo = MetronApp.repository

            if (intent.action == ACTION_AUTO_BACKUP) {
                if (repo.isAutoBackupEnabled.value) {
                    com.metron.app.backup.AutoBackupManager.createAutoBackup(context, repo)
                }
                return
            }

            val isNotifsEnabled = repo.isNotificationsEnabled.value
            if (!isNotifsEnabled) return

            when (intent.action) {
                ACTION_DAILY_REMINDER -> {
                    MetronNotificationManager.showDailyReminderNotification(context)
                }
                ACTION_BILL_REMINDER -> {
                    val billName = intent.getStringExtra("bill_name") ?: "Subscription"
                    val amountStr = intent.getStringExtra("amount_str") ?: ""
                    MetronNotificationManager.showBillReminderNotification(context, billName, amountStr)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
