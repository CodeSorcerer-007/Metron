package com.metron.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.metron.app.MainActivity
import com.metron.app.MetronApp
import com.metron.app.R
import com.metron.app.model.TransactionType
import java.util.*

class MetronWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = MetronApp.repository
        val transactions = repo.transactions.value
        val budgets = repo.budgets.value
        val sym = repo.currencySymbol.value

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        val todaySpent = transactions
            .filter { it.type == TransactionType.EXPENSE && it.timestamp >= startOfDay }
            .sumOf { it.amount }

        val monthlyBudget = budgets.find { it.categoryId == null }?.amount ?: 30000.0

        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val monthlySpent = transactions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == TransactionType.EXPENSE && c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        val remainingBudget = (monthlyBudget - monthlySpent).coerceAtLeast(0.0)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_metron)

            views.setTextViewText(R.id.widget_spent_today, "$sym${String.format("%,.0f", todaySpent)}")
            views.setTextViewText(R.id.widget_budget_left, "$sym${String.format("%,.0f", remainingBudget)}")

            // Intent to open MainActivity on click
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_quick_add, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
