package com.metron.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.model.*
import com.metron.app.theme.*
import com.metron.app.ui.components.MetronTopBar
import com.metron.app.ui.components.PrimarySpendingCard
import com.metron.app.ui.components.TransactionListItem
import java.util.*

@Composable
fun HomeScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    accounts: List<Account>,
    budgets: List<Budget>,
    currencySymbol: String,
    isCalmMode: Boolean,
    onToggleCalmMode: () -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onDuplicateTransaction: (Long) -> Unit,
    onViewAllTransactions: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(TimeFilter.THIS_MONTH) }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }

    // Filter transactions based on selected tab
    val filteredTransactions = remember(transactions, selectedFilter) {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        when (selectedFilter) {
            TimeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfDay }
            }
            TimeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfWeek = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfWeek }
            }
            TimeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfMonth = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfMonth }
            }
            TimeFilter.ALL_TIME, TimeFilter.THIS_YEAR -> transactions
        }
    }

    val totalSpent = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    // Monthly overall budget and monthly total spent
    val monthlyBudget = remember(budgets) {
        budgets.find { it.categoryId == null }?.amount ?: 0.0
    }

    val monthlySpent = remember(transactions) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        transactions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == TransactionType.EXPENSE && c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    // Today's transactions
    val todayTransactions = remember(transactions) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        transactions.filter { it.timestamp >= startOfDay }
    }

    // Dynamic smart insight
    val insightText = remember(transactions, totalSpent, monthlySpent, monthlyBudget) {
        when {
            transactions.isEmpty() -> "Welcome to Metron. Measure in all things. Begin by recording your first expense."
            todayTransactions.isNotEmpty() -> "You have recorded ${todayTransactions.size} transactions today totaling $currencySymbol${String.format("%,.0f", todayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })}."
            monthlyBudget > 0 && monthlySpent < monthlyBudget * 0.5 -> "You have used less than half of your monthly budget. Your financial harmony is strong."
            else -> "Mindful awareness of small daily expenses is the key to lasting financial calm."
        }
    }

    Scaffold(
        topBar = {
            MetronTopBar(
                isCalmMode = isCalmMode,
                onToggleCalmMode = onToggleCalmMode,
                onSearchClick = onSearchClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Spending Card
            item {
                PrimarySpendingCard(
                    totalSpent = totalSpent,
                    totalIncome = totalIncome,
                    currencySymbol = currencySymbol,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    monthlyBudget = monthlyBudget,
                    monthlySpent = monthlySpent,
                    isCalmMode = isCalmMode
                )
            }

            // Actionable Insight Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            // Quick Stats Row (Transactions count, average size, top category)
            if (!isCalmMode && filteredTransactions.isNotEmpty()) {
                item {
                    val expenseList = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
                    val avgSize = if (expenseList.isNotEmpty()) expenseList.sumOf { it.amount } / expenseList.size else 0.0
                    val topCatId = expenseList.groupBy { it.categoryId }.maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key
                    val topCatName = categoryMap[topCatId]?.name ?: "—"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avg Transaction Size
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Average Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", avgSize)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Top Category
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Top Outflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = topCatName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Today's Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Measure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (transactions.isNotEmpty()) {
                        TextButton(onClick = onViewAllTransactions) {
                            Text(text = "View Ledger", color = GoldPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Transactions Feed or Empty State
            if (todayTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your money story starts here",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Record your first expense in seconds with zero friction.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Expense", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(todayTransactions.take(8), key = { it.id }) { tx ->
                    TransactionListItem(
                        transaction = tx,
                        category = categoryMap[tx.categoryId],
                        account = accountMap[tx.accountId],
                        currencySymbol = currencySymbol,
                        onDelete = { onDeleteTransaction(tx.id) },
                        onDuplicate = { onDuplicateTransaction(tx.id) },
                        onEdit = { /* Edit flow */ }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp)) // Padding for bottom bar
            }
        }
    }
}
