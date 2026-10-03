package com.metron.app.backup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.metron.app.data.MetronRepository
import com.metron.app.notification.MetronAlarmReceiver
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class LocalBackupItem(
    val fileName: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val file: File
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format(Locale.getDefault(), "%.1f MB", sizeBytes.toDouble() / (1024 * 1024))
        }
}

object AutoBackupManager {

    private const val BACKUP_DIR_NAME = "metron_backups"
    private const val MAX_AUTO_BACKUPS_RETAINED = 5

    fun getBackupDirectory(context: Context): File {
        val dir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates a timestamped local JSON snapshot and prunes older auto-backups.
     */
    fun createAutoBackup(context: Context, repo: MetronRepository): File? {
        return try {
            val jsonString = repo.exportToJson()
            val dir = getBackupDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(dir, "metron_autobackup_$timeStamp.json")
            backupFile.writeText(jsonString)

            // Prune older backups
            pruneOldBackups(dir, MAX_AUTO_BACKUPS_RETAINED)

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lists all available local JSON backup archives, newest first.
     */
    fun listBackups(context: Context): List<LocalBackupItem> {
        val dir = getBackupDirectory(context)
        val files = dir.listFiles { f -> f.extension == "json" } ?: return emptyList()
        return files.map { file ->
            LocalBackupItem(
                fileName = file.name,
                timestamp = file.lastModified(),
                sizeBytes = file.length(),
                file = file
            )
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Restores application state from a selected local JSON backup file.
     */
    fun restoreBackup(repo: MetronRepository, file: File): Boolean {
        return try {
            if (!file.exists()) return false
            val content = file.readText()
            repo.importFromJson(content)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Schedules a recurring weekly auto-backup alarm with Android AlarmManager.
     */
    fun scheduleWeeklyAutoBackup(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MetronAlarmReceiver::class.java).apply {
            action = MetronAlarmReceiver.ACTION_AUTO_BACKUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1005,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sunday at 02:00 AM
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pruneOldBackups(dir: File, keepCount: Int) {
        val jsonFiles = dir.listFiles { f -> f.extension == "json" && f.name.startsWith("metron_autobackup_") } ?: return
        if (jsonFiles.size > keepCount) {
            val sorted = jsonFiles.sortedBy { it.lastModified() }
            val toDeleteCount = jsonFiles.size - keepCount
            for (i in 0 until toDeleteCount) {
                sorted[i].delete()
            }
        }
    }
}
