package com.metron.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.model.Category
import com.metron.app.model.Transaction
import com.metron.app.model.TransactionType
import com.metron.app.theme.*
import com.metron.app.ui.components.DonutChart
import com.metron.app.ui.components.DonutSlice
import com.metron.app.ui.components.SpendingStoryCard
import com.metron.app.ui.components.parseColor
import java.util.*

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    currencySymbol: String,
    spendingStory: String,
    modifier: Modifier = Modifier
) {
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
    }

    val totalExpenses = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.amount }
    }

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    val netBalance = totalIncome - totalExpenses

    // Category Slices for Donut Chart
    val donutSlices = remember(expenseTransactions, categoryMap, totalExpenses) {
        if (totalExpenses <= 0) emptyList()
        else {
            expenseTransactions.groupBy { it.categoryId }
                .mapNotNull { (catId, txs) ->
                    val cat = categoryMap[catId] ?: return@mapNotNull null
                    val catTotal = txs.sumOf { it.amount }
                    val pct = (catTotal / totalExpenses).toFloat()
                    DonutSlice(
                        category = cat,
                        amount = catTotal,
                        percentage = pct,
                        color = parseColor(cat.colorHex)
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    // Top merchants
    val topMerchants = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.merchant.trim() }
            .filterKeys { it.isNotBlank() }
            .map { (name, txs) ->
                Triple(name, txs.size, txs.sumOf { it.amount })
            }
            .sortedByDescending { it.third }
            .take(5)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "THE ORACLE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = BronzeAccent
                )
                Text(
                    text = "Financial vision, patterns & balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Balance / Cashflow Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Net Cashflow Balance",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (netBalance >= 0) "+" else ""}$currencySymbol${String.format(Locale.getDefault(), "%,.2f", netBalance)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = if (netBalance >= 0) LaurelGreen else SpartanRose
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Income
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LaurelGreen.copy(alpha = 0.08f))
                                    .padding(10.dp)
                            ) {
                                Text("Inflow", style = MaterialTheme.typography.labelSmall, color = LaurelGreen)
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", totalIncome)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Total Expenses
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpartanRose.copy(alpha = 0.08f))
                                    .padding(10.dp)
                            ) {
                                Text("Outflow", style = MaterialTheme.typography.labelSmall, color = SpartanRose)
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", totalExpenses)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category Donut Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Category Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        DonutChart(
                            slices = donutSlices,
                            totalAmount = totalExpenses,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Greek Spending Story
            item {
                SpendingStoryCard(storyText = spendingStory)
            }

            // Top Merchants Leaderboard
            if (topMerchants.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LightBorder, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = BronzeAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Most Frequented Merchants",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topMerchants.forEachIndexed { idx, (name, count, amt) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${idx + 1}.",
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary,
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Column {
                                                Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                Text(text = "$count transactions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Text(
                                            text = "$currencySymbol${String.format("%,.0f", amt)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
