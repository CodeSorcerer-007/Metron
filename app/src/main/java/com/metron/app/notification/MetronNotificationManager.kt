package com.metron.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.metron.app.MainActivity
import com.metron.app.MetronApp
import com.metron.app.R
import com.metron.app.model.TransactionType
import java.util.*

object MetronNotificationManager {

    const val CHANNEL_DAILY = "metron_daily_reminder"
    const val CHANNEL_BILLS = "metron_recurring_bills"
    const val CHANNEL_BUDGET = "metron_budget_alerts"

    const val NOTIF_ID_DAILY = 1001
    const val NOTIF_ID_BILLS = 1002
    const val NOTIF_ID_BUDGET = 1003

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "Daily Mindful Check-in",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle evening reminder to record your daily transactions and review your measure"
                enableVibration(true)
            }

            val billsChannel = NotificationChannel(
                CHANNEL_BILLS,
                "Upcoming Recurring Bills",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for scheduled subscriptions, rent, and recurring cycle due dates"
                enableVibration(true)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET,
                "Monthly Measure Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when approaching or reaching monthly spending budget limits"
                enableVibration(true)
            }

            nm.createNotificationChannel(dailyChannel)
            nm.createNotificationChannel(billsChannel)
            nm.createNotificationChannel(budgetChannel)
        }
    }

    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 30) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MetronAlarmReceiver::class.java).apply {
            action = MetronAlarmReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MetronAlarmReceiver::class.java).apply {
            action = MetronAlarmReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showDailyReminderNotification(context: Context) {
        val quotes = listOf(
            "Μέτρον ἄριστον • Measure is best. Have you recorded today's expenses?",
            "“Wealth consists not in having great possessions, but in having few wants.” — Epictetus",
            "Take 10 seconds to record today's transactions and keep your sanctuary in harmony.",
            "“He is rich who is content with the least.” — Socrates • Review your spending today."
        )
        val selectedQuote = quotes.random()

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_metron_logo)
            .setContentTitle("METRON • Evening Mindful Measure")
            .setContentText(selectedQuote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(selectedQuote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_DAILY, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }

    fun showBillReminderNotification(context: Context, billName: String, amountStr: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Upcoming cycle: $billName ($amountStr) is due soon. Open Metron to review or mark as paid."
        val builder = NotificationCompat.Builder(context, CHANNEL_BILLS)
            .setSmallIcon(R.drawable.ic_metron_logo)
            .setContentTitle("METRON • Recurring Bill Due")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_BILLS, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }

    fun showBudgetWarningNotification(context: Context, percentage: Int, remainingStr: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "You have used $percentage% of your monthly measure. $remainingStr remaining to spend mindfully."
        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_metron_logo)
            .setContentTitle("METRON • Monthly Budget Notice")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_BUDGET, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }
}
