package com.metron.app.widget.glance

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.metron.app.MainActivity
import com.metron.app.MetronApp
import com.metron.app.model.TransactionType
import java.util.*

class MetronGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                GlanceContent(context)
            }
        }
    }

    @Composable
    private fun GlanceContent(context: Context) {
        var sym = "₹"
        var todaySpent = 0.0
        var remainingBudget = 30000.0

        try {
            val repo = MetronApp.repository
            val transactions = repo.transactions.value
            val budgets = repo.budgets.value
            sym = repo.currencySymbol.value

            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            todaySpent = transactions
                .filter { it.type == TransactionType.EXPENSE && it.timestamp >= startOfDay }
                .sumOf { it.amount }

            val monthlyBudget = budgets.find { it.categoryId == null }?.amount ?: 30000.0

            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)
            val monthlySpent = transactions.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                it.type == TransactionType.EXPENSE && c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }

            remainingBudget = (monthlyBudget - monthlySpent).coerceAtLeast(0.0)
        } catch (e: Throwable) {
            // Resilient fallback if accessed before full app init
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(20.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏛️ MÉTRON",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "TODAY'S SPENT",
                            style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                        )
                        Text(
                            text = "$sym${String.format(Locale.getDefault(), "%,.0f", todaySpent)}",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "MONTHLY LEFT",
                            style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                        )
                        Text(
                            text = "$sym${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

class MetronGlanceReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MetronGlanceWidget()
}
