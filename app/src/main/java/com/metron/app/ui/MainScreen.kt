package com.metron.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.metron.app.MetronApp
import com.metron.app.model.Transaction
import com.metron.app.theme.*
import com.metron.app.ui.components.QuickAddSheet
import com.metron.app.ui.screens.*
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    SANCTUARY("Sanctuary"),
    LEDGER("Ledger"),
    ORACLE("Oracle"),
    PILLARS("Pillars"),
    VAULT("Vault")
}

@Composable
fun MainScreen() {
    val repo = MetronApp.repository
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val transactions by repo.transactions.collectAsState()
    val categories by repo.categories.collectAsState()
    val accounts by repo.accounts.collectAsState()
    val budgets by repo.budgets.collectAsState()
    val recurringItems by repo.recurringItems.collectAsState()
    val currencySymbol by repo.currencySymbol.collectAsState()
    val currencyCode by repo.currencyCode.collectAsState()
    val themeMode by repo.themeMode.collectAsState()
    val isCalmMode by repo.isCalmMode.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.SANCTUARY) }
    var showQuickAddSheet by remember { mutableStateOf(false) }

    val spendingStory = remember(transactions, categories, currencySymbol, budgets) {
        repo.generateGreekSpendingStory()
    }

    val recentMerchants = remember(transactions) {
        repo.getRecentMerchants()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Tab 1: Sanctuary (Home)
                NavigationBarItem(
                    selected = currentTab == MainTab.SANCTUARY,
                    onClick = { currentTab = MainTab.SANCTUARY },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Sanctuary") },
                    label = { Text("Sanctuary", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.SANCTUARY) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                )

                // Tab 2: Ledger (Transactions)
                NavigationBarItem(
                    selected = currentTab == MainTab.LEDGER,
                    onClick = { currentTab = MainTab.LEDGER },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger") },
                    label = { Text("Ledger", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.LEDGER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                )

                // Tab 3: Oracle (Analytics)
                NavigationBarItem(
                    selected = currentTab == MainTab.ORACLE,
                    onClick = { currentTab = MainTab.ORACLE },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Oracle") },
                    label = { Text("Oracle", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.ORACLE) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                )

                // Tab 4: Pillars (Budgets & Recurring)
                NavigationBarItem(
                    selected = currentTab == MainTab.PILLARS,
                    onClick = { currentTab = MainTab.PILLARS },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Pillars") },
                    label = { Text("Pillars", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.PILLARS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                )

                // Tab 5: Vault (Treasury / Settings)
                NavigationBarItem(
                    selected = currentTab == MainTab.VAULT,
                    onClick = { currentTab = MainTab.VAULT },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Vault") },
                    label = { Text("Vault", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.VAULT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldPrimary,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = GoldPrimary,
                contentColor = DarkBackground,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Record",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tabFade") { tab ->
                when (tab) {
                    MainTab.SANCTUARY -> {
                        HomeScreen(
                            transactions = transactions,
                            categories = categories,
                            accounts = accounts,
                            budgets = budgets,
                            currencySymbol = currencySymbol,
                            isCalmMode = isCalmMode,
                            onToggleCalmMode = { repo.setCalmMode(!isCalmMode) },
                            onSearchClick = { currentTab = MainTab.LEDGER },
                            onAddClick = { showQuickAddSheet = true },
                            onDeleteTransaction = { id ->
                                repo.deleteTransaction(id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        repo.undoDeleteTransaction()
                                    }
                                }
                            },
                            onDuplicateTransaction = { id ->
                                repo.duplicateTransaction(id)
                                scope.launch { snackbarHostState.showSnackbar("Transaction duplicated") }
                            },
                            onViewAllTransactions = { currentTab = MainTab.LEDGER }
                        )
                    }
                    MainTab.LEDGER -> {
                        TransactionsScreen(
                            transactions = transactions,
                            categories = categories,
                            accounts = accounts,
                            currencySymbol = currencySymbol,
                            onDeleteTransaction = { id ->
                                repo.deleteTransaction(id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        repo.undoDeleteTransaction()
                                    }
                                }
                            },
                            onDuplicateTransaction = { id ->
                                repo.duplicateTransaction(id)
                                scope.launch { snackbarHostState.showSnackbar("Transaction duplicated") }
                            }
                        )
                    }
                    MainTab.ORACLE -> {
                        AnalyticsScreen(
                            transactions = transactions,
                            categories = categories,
                            currencySymbol = currencySymbol,
                            spendingStory = spendingStory
                        )
                    }
                    MainTab.PILLARS -> {
                        BudgetsScreen(
                            budgets = budgets,
                            transactions = transactions,
                            categories = categories,
                            accounts = accounts,
                            recurringItems = recurringItems,
                            currencySymbol = currencySymbol,
                            onSetOverallBudget = { repo.setOverallMonthlyBudget(it) },
                            onSetCategoryBudget = { catId, amt -> repo.setCategoryBudget(catId, amt) },
                            onDeleteBudget = { repo.deleteBudget(it) },
                            onAddRecurringItem = { repo.addRecurringItem(it) },
                            onToggleRecurringItem = { id, active -> repo.toggleRecurringItem(id, active) },
                            onDeleteRecurringItem = { repo.deleteRecurringItem(it) },
                            onMarkRecurringPaid = {
                                repo.markRecurringPaid(it)
                                scope.launch { snackbarHostState.showSnackbar("Recorded recurring payment for ${it.name}") }
                            }
                        )
                    }
                    MainTab.VAULT -> {
                        VaultScreen(
                            accounts = accounts,
                            currencySymbol = currencySymbol,
                            currencyCode = currencyCode,
                            themeMode = themeMode,
                            isCalmMode = isCalmMode,
                            onSetCurrency = { code, sym -> repo.setCurrency(code, sym) },
                            onSetThemeMode = { repo.setThemeMode(it) },
                            onToggleCalmMode = { repo.setCalmMode(!isCalmMode) },
                            onAddAccount = { repo.addAccount(it) },
                            onExportJson = { repo.exportToJson() },
                            onImportJson = { repo.importFromJson(it) },
                            onExportCsv = { repo.exportToCsv() },
                            onGenerateDemoData = { repo.generateDemoData() },
                            onClearAllData = { repo.clearAllData() }
                        )
                    }
                }
            }

            // Zero-Friction Quick Expense Add Sheet
            if (showQuickAddSheet) {
                QuickAddSheet(
                    currencySymbol = currencySymbol,
                    categories = categories,
                    accounts = accounts,
                    recentMerchants = recentMerchants,
                    onSaveTransaction = { tx ->
                        repo.addTransaction(tx)
                        scope.launch {
                            snackbarHostState.showSnackbar("Measure recorded • ${tx.merchant}")
                        }
                    },
                    onSuggestCategory = { repo.getSuggestedCategoryForMerchant(it) },
                    onSuggestAccount = { repo.getSuggestedAccountForMerchant(it) },
                    onDismiss = { showQuickAddSheet = false }
                )
            }
        }
    }
}
