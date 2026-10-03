package com.metron.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.model.*
import com.metron.app.theme.*
import com.metron.app.ui.components.CategoryIconBox
import com.metron.app.ui.components.parseColor
import com.metron.app.util.MoneyUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetsScreen(
    budgets: List<Budget>,
    transactions: List<Transaction>,
    categories: List<Category>,
    accounts: List<Account>,
    recurringItems: List<RecurringItem>,
    currencySymbol: String,
    onSetOverallBudget: (Double) -> Unit,
    onSetCategoryBudget: (Long, Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddRecurringItem: (RecurringItem) -> Unit,
    onToggleRecurringItem: (Long, Boolean) -> Unit,
    onDeleteRecurringItem: (Long) -> Unit,
    onMarkRecurringPaid: (RecurringItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditOverallBudgetDialog by remember { mutableStateOf(false) }
    var showAddCategoryBudgetDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val cal = Calendar.getInstance()
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    // Current month's expenses
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)
    val monthExpenses = remember(transactions) {
        transactions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == TransactionType.EXPENSE && c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }
    }

    val overallBudget = remember(budgets) { budgets.find { it.categoryId == null }?.amount ?: 0.0 }
    val totalMonthSpent = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val overallRemaining = (overallBudget - totalMonthSpent).coerceAtLeast(0.0)
    val dailyAllowance = if (daysRemaining > 0) overallRemaining / daysRemaining else 0.0

    val categoryBudgets = remember(budgets) { budgets.filter { it.categoryId != null } }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "THE PILLARS & CYCLES",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = BronzeAccent
                )
                Text(
                    text = "Budgets, daily discipline & recurring cycles",
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
            // Overall Monthly Pillar Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightBorder, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Measure Pillar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { showEditOverallBudgetDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = BronzeAccent, modifier = Modifier.size(18.dp))
                            }
                        }

                        val progress = if (overallBudget > 0) (totalMonthSpent / overallBudget).toFloat().coerceIn(0f, 1f) else 0f
                        val pColor = if (progress > 0.9f) SpartanRose else BronzeAccent

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", overallRemaining)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " left of $currencySymbol${String.format(Locale.getDefault(), "%,.0f", overallBudget)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = pColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Allowance Banner in Parchment styling
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ParchmentBg)
                                .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BronzeAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", dailyAllowance)} / day remaining for the next $daysRemaining days",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ParchmentText
                            )
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showAddCategoryBudgetDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Budget", color = GoldPrimary, fontSize = 12.sp)
                    }
                }
            }

            if (categoryBudgets.isEmpty()) {
                item {
                    Text(
                        text = "No category budgets established. Set limits on dining, shopping, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(categoryBudgets, key = { it.id }) { b ->
                    val cat = categoryMap[b.categoryId]
                    val catSpent = monthExpenses.filter { it.categoryId == b.categoryId }.sumOf { it.amount }
                    val catProgress = if (b.amount > 0) (catSpent / b.amount).toFloat().coerceIn(0f, 1f) else 0f
                    val catLeft = (b.amount - catSpent).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconBox(
                                        iconName = cat?.iconName ?: "Category",
                                        colorHex = cat?.colorHex ?: "#D4AF37",
                                        size = 36.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = cat?.name ?: "Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "$currencySymbol${String.format("%,.0f", catLeft)} left of $currencySymbol${String.format("%,.0f", b.amount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteBudget(b.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { catProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = parseColor(cat?.colorHex ?: "#D4AF37"),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Recurring Subscriptions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Cycles & Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showAddRecurringDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Cycle", color = GoldPrimary, fontSize = 12.sp)
                    }
                }
            }

            if (recurringItems.isEmpty()) {
                item {
                    Text(
                        text = "No recurring bills or subscriptions configured.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recurringItems, key = { it.id }) { item ->
                    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    val dueDateStr = sdf.format(Date(item.nextDueDate))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", item.amount)} • ${item.frequency.title} • Due $dueDateStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onMarkRecurringPaid(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LaurelGreen, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { onDeleteRecurringItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SpartanRose, modifier = Modifier.size(16.dp))
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

    // Edit Overall Monthly Budget Dialog
    if (showEditOverallBudgetDialog) {
        var newAmountText by remember { mutableStateOf(if (overallBudget > 0) String.format(Locale.US, "%.2f", overallBudget).removeSuffix(".00") else "") }
        AlertDialog(
            onDismissRequest = { showEditOverallBudgetDialog = false },
            title = { Text("Monthly Measure Budget") },
            text = {
                OutlinedTextField(
                    value = newAmountText,
                    onValueChange = { if (it.length <= 15) newAmountText = it },
                    label = { Text("Budget Amount ($currencySymbol)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val raw = newAmountText.toDoubleOrNull() ?: 0.0
                    val amt = MoneyUtils.round(raw)
                    if (amt > 0) onSetOverallBudget(amt)
                    showEditOverallBudgetDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOverallBudgetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Category Budget Dialog
    if (showAddCategoryBudgetDialog) {
        var selectedCat by remember { mutableStateOf(categories.firstOrNull { it.type == CategoryType.EXPENSE }) }
        var budgetAmtText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryBudgetDialog = false },
            title = { Text("Add Category Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Category:", style = MaterialTheme.typography.labelMedium)
                    categories.filter { it.type == CategoryType.EXPENSE }.take(6).forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCat?.id == cat.id) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedCat = cat }
                                .padding(8.dp)
                        ) {
                            Text(cat.name, fontWeight = if (selectedCat?.id == cat.id) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    OutlinedTextField(
                        value = budgetAmtText,
                        onValueChange = { if (it.length <= 15) budgetAmtText = it },
                        label = { Text("Limit Amount ($currencySymbol)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val raw = budgetAmtText.toDoubleOrNull() ?: 0.0
                    val amt = MoneyUtils.round(raw)
                    if (amt > 0 && selectedCat != null) {
                        onSetCategoryBudget(selectedCat!!.id, amt)
                    }
                    showAddCategoryBudgetDialog = false
                }) {
                    Text("Set Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryBudgetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Recurring Subscription Dialog
    if (showAddRecurringDialog) {
        var nameText by remember { mutableStateOf("") }
        var amtText by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf(categories.firstOrNull()) }
        var selectedAcc by remember { mutableStateOf(accounts.firstOrNull()) }
        var selectedFreq by remember { mutableStateOf(RecurringFrequency.MONTHLY) }

        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = { Text("New Recurring Cycle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { if (it.length <= 100) nameText = it },
                        label = { Text("Name (e.g. Netflix, Rent)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amtText,
                        onValueChange = { if (it.length <= 15) amtText = it },
                        label = { Text("Amount ($currencySymbol)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val raw = amtText.toDoubleOrNull() ?: 0.0
                    val amt = MoneyUtils.round(raw)
                    val safeName = nameText.trim().take(100)
                    if (safeName.isNotBlank() && amt > 0 && selectedCat != null && selectedAcc != null) {
                        val item = RecurringItem(
                            name = safeName,
                            amount = amt,
                            categoryId = selectedCat!!.id,
                            accountId = selectedAcc!!.id,
                            frequency = selectedFreq,
                            nextDueDate = System.currentTimeMillis() + 30L * 86400000L
                        )
                        onAddRecurringItem(item)
                    }
                    showAddRecurringDialog = false
                }) {
                    Text("Save Cycle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecurringDialog = false }) { Text("Cancel") }
            }
        )
    }
}
