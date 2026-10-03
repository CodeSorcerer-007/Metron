package com.metron.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.*
import com.metron.app.theme.*
import com.metron.app.ui.components.TransactionListItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String,
    onDeleteTransaction: (Long) -> Unit,
    onDuplicateTransaction: (Long) -> Unit,
    onEditTransaction: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<Category?>(null) }
    var selectedAccountFilter by remember { mutableStateOf<Account?>(null) }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }

    // Multi-criteria instant search & filter
    val filteredTransactions = remember(
        transactions, searchQuery, selectedTypeFilter, selectedCategoryFilter, selectedAccountFilter
    ) {
        transactions.filter { tx ->
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesCat = selectedCategoryFilter == null || tx.categoryId == selectedCategoryFilter?.id
            val matchesAcc = selectedAccountFilter == null || tx.accountId == selectedAccountFilter?.id

            val catName = categoryMap[tx.categoryId]?.name ?: ""
            val accName = accountMap[tx.accountId]?.name ?: ""

            val matchesQuery = if (searchQuery.isBlank()) true else {
                tx.merchant.contains(searchQuery, ignoreCase = true) ||
                catName.contains(searchQuery, ignoreCase = true) ||
                accName.contains(searchQuery, ignoreCase = true) ||
                tx.notes.contains(searchQuery, ignoreCase = true) ||
                tx.tag.contains(searchQuery, ignoreCase = true) ||
                tx.amount.toString().contains(searchQuery)
            }

            matchesType && matchesCat && matchesAcc && matchesQuery
        }
    }

    // Group transactions by date string
    val groupedTransactions = remember(filteredTransactions) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        filteredTransactions.groupBy { sdf.format(Date(it.timestamp)) }
    }

    val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterdayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "THE LEDGER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search merchant, category, amount...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Filter Chips: All, Expense, Income, Transfer
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = {
                                HapticsManager.tick()
                                selectedTypeFilter = null
                            },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                    items(TransactionType.values()) { type ->
                        val isSelected = selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                HapticsManager.tick()
                                selectedTypeFilter = if (isSelected) null else type
                            },
                            label = { Text(type.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No records matching '$searchQuery'" else "No transactions recorded yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotBlank() || selectedTypeFilter != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = {
                            searchQuery = ""
                            selectedTypeFilter = null
                        }) {
                            Text("Reset filters", color = GoldPrimary)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                groupedTransactions.forEach { (dateKey, txList) ->
                    val headerTitle = when (dateKey) {
                        todayDateStr -> "Today"
                        yesterdayDateStr -> "Yesterday"
                        else -> {
                            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
                            if (parsed != null) displayDateFormat.format(parsed) else dateKey
                        }
                    }

                    val dayTotalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    item(key = "header_$dateKey") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (dayTotalExpense > 0) {
                                Text(
                                    text = "-$currencySymbol${String.format("%,.0f", dayTotalExpense)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpartanRose
                                )
                            }
                        }
                    }

                    items(txList, key = { it.id }) { tx ->
                        TransactionListItem(
                            transaction = tx,
                            category = categoryMap[tx.categoryId],
                            account = accountMap[tx.accountId],
                            currencySymbol = currencySymbol,
                            onDelete = { onDeleteTransaction(tx.id) },
                            onDuplicate = { onDuplicateTransaction(tx.id) },
                            onEdit = { onEditTransaction(tx) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}
