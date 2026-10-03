package com.metron.app

import android.app.Application
import android.util.Log
import com.metron.app.data.MetronRepository
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MetronApp : Application() {

    companion object {
        @Volatile
        private var _instance: MetronApp? = null

        val instance: MetronApp
            get() = _instance ?: error("MetronApp instance not initialized")

        val repository: MetronRepository by lazy {
            MetronRepository(_instance ?: error("MetronApp instance not initialized"))
        }
    }

    init {
        _instance = this
    }

    override fun attachBaseContext(base: android.content.Context?) {
        super.attachBaseContext(base)
        _instance = this
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("MetronCrash", "Uncaught exception in thread ${thread.name}", throwable)
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val crashFile = File(filesDir, "last_crash.txt")
                crashFile.writeText(sw.toString())
            } catch (ignored: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            // Initialize local notification channels
            com.metron.app.notification.MetronNotificationManager.initNotificationChannels(this)
            if (repository.isNotificationsEnabled.value) {
                com.metron.app.notification.MetronNotificationManager.scheduleDailyReminder(
                    this,
                    repository.dailyReminderHour.value,
                    repository.dailyReminderMinute.value
                )
            }
            com.metron.app.backup.AutoBackupManager.scheduleWeeklyAutoBackup(this)
        } catch (e: Throwable) {
            Log.e("MetronApp", "Error during app background initialization", e)
        }
    }
}
