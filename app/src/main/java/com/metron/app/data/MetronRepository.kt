package com.metron.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.metron.app.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MetronRepository(val context: Context) {

    private val dbHelper = MetronDatabaseHelper(context.applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _recurringItems = MutableStateFlow<List<RecurringItem>>(emptyList())
    val recurringItems: StateFlow<List<RecurringItem>> = _recurringItems.asStateFlow()

    private val _currencySymbol = MutableStateFlow("₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _currencyCode = MutableStateFlow("INR")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _themeMode = MutableStateFlow("AEGEAN_DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isCalmMode = MutableStateFlow(false)
    val isCalmMode: StateFlow<Boolean> = _isCalmMode.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _dailyReminderHour = MutableStateFlow(20)
    val dailyReminderHour: StateFlow<Int> = _dailyReminderHour.asStateFlow()

    private val _dailyReminderMinute = MutableStateFlow(30)
    val dailyReminderMinute: StateFlow<Int> = _dailyReminderMinute.asStateFlow()

    private var lastDeletedTransaction: Transaction? = null

    init {
        reloadAll()
    }

    fun reloadAll() {
        scope.launch {
            loadPreferences()
            loadCategories()
            loadAccounts()
            loadTransactions()
            loadBudgets()
            loadRecurring()
        }
    }

    private fun loadPreferences() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(MetronDatabaseHelper.TABLE_PREFERENCES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndexOrThrow("key"))
                val value = it.getString(it.getColumnIndexOrThrow("value"))
                when (key) {
                    "currency_symbol" -> _currencySymbol.value = value
                    "currency_code" -> _currencyCode.value = value
                    "theme_mode" -> _themeMode.value = value
                    "calm_mode" -> _isCalmMode.value = (value == "1")
                    "onboarding_completed" -> _isOnboardingCompleted.value = (value == "1")
                    "haptics_enabled" -> _isHapticsEnabled.value = (value == "1")
                    "notifications_enabled" -> _isNotificationsEnabled.value = (value == "1")
                    "daily_reminder_hour" -> _dailyReminderHour.value = value.toIntOrNull() ?: 20
                    "daily_reminder_minute" -> _dailyReminderMinute.value = value.toIntOrNull() ?: 30
                }
            }
        }
    }

    private fun loadCategories() {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Category>()
        val cursor = db.query(
            MetronDatabaseHelper.TABLE_CATEGORIES,
            null, null, null, null, null,
            "display_order ASC, name ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Category(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        iconName = it.getString(it.getColumnIndexOrThrow("icon_name")),
                        colorHex = it.getString(it.getColumnIndexOrThrow("color_hex")),
                        type = CategoryType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        isDefault = it.getInt(it.getColumnIndexOrThrow("is_default")) == 1,
                        displayOrder = it.getInt(it.getColumnIndexOrThrow("display_order"))
                    )
                )
            }
        }
        _categories.value = list
    }

    private fun loadAccounts() {
        val db = dbHelper.readableDatabase
        val rawAccounts = mutableListOf<Account>()
        val cursor = db.query(
            MetronDatabaseHelper.TABLE_ACCOUNTS,
            null, null, null, null, null,
            "id ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                rawAccounts.add(
                    Account(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        type = AccountType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        initialBalance = it.getDouble(it.getColumnIndexOrThrow("initial_balance")),
                        currentBalance = it.getDouble(it.getColumnIndexOrThrow("initial_balance")),
                        colorHex = it.getString(it.getColumnIndexOrThrow("color_hex")),
                        iconName = it.getString(it.getColumnIndexOrThrow("icon_name"))
                    )
                )
            }
        }

        // Calculate current balances from transactions
        val calculated = rawAccounts.map { acc ->
            val balance = calculateAccountBalance(db, acc.id, acc.initialBalance)
            acc.copy(currentBalance = balance)
        }
        _accounts.value = calculated
    }

    private fun calculateAccountBalance(db: android.database.sqlite.SQLiteDatabase, accountId: Long, initial: Double): Double {
        var balance = initial
        val cursor = db.rawQuery(
            "SELECT type, amount, account_id, to_account_id FROM ${MetronDatabaseHelper.TABLE_TRANSACTIONS} WHERE account_id = ? OR to_account_id = ?",
            arrayOf(accountId.toString(), accountId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                val type = it.getString(0)
                val amount = it.getDouble(1)
                val fromId = it.getLong(2)
                val toId = if (!it.isNull(3)) it.getLong(3) else null

                when (type) {
                    TransactionType.INCOME.name -> {
                        if (fromId == accountId) balance += amount
                    }
                    TransactionType.EXPENSE.name -> {
                        if (fromId == accountId) balance -= amount
                    }
                    TransactionType.TRANSFER.name -> {
                        if (fromId == accountId) balance -= amount
                        if (toId == accountId) balance += amount
                    }
                }
            }
        }
        return balance
    }

    private fun loadTransactions() {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Transaction>()
        val cursor = db.query(
            MetronDatabaseHelper.TABLE_TRANSACTIONS,
            null, null, null, null, null,
            "timestamp DESC, id DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val receiptIdx = it.getColumnIndex("receipt_path")
                val receipt = if (receiptIdx != -1 && !it.isNull(receiptIdx)) it.getString(receiptIdx) else null
                list.add(
                    Transaction(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        type = TransactionType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        categoryId = it.getLong(it.getColumnIndexOrThrow("category_id")),
                        accountId = it.getLong(it.getColumnIndexOrThrow("account_id")),
                        toAccountId = if (it.isNull(it.getColumnIndexOrThrow("to_account_id"))) null else it.getLong(it.getColumnIndexOrThrow("to_account_id")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        merchant = it.getString(it.getColumnIndexOrThrow("merchant")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes")),
                        tag = it.getString(it.getColumnIndexOrThrow("tag")),
                        currency = it.getString(it.getColumnIndexOrThrow("currency")),
                        receiptPath = receipt,
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        _transactions.value = list
    }

    private fun loadBudgets() {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Budget>()
        val cursor = db.query(MetronDatabaseHelper.TABLE_BUDGETS, null, null, null, null, null, "id ASC")
        cursor.use {
            while (it.moveToNext()) {
                val catId = if (it.isNull(it.getColumnIndexOrThrow("category_id"))) null else it.getLong(it.getColumnIndexOrThrow("category_id"))
                list.add(
                    Budget(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        categoryId = catId,
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        period = BudgetPeriod.valueOf(it.getString(it.getColumnIndexOrThrow("period"))),
                        monthYear = it.getString(it.getColumnIndexOrThrow("month_year"))
                    )
                )
            }
        }
        _budgets.value = list
    }

    private fun loadRecurring() {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<RecurringItem>()
        val cursor = db.query(MetronDatabaseHelper.TABLE_RECURRING, null, null, null, null, null, "next_due_date ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    RecurringItem(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("amount")),
                        type = TransactionType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        categoryId = it.getLong(it.getColumnIndexOrThrow("category_id")),
                        accountId = it.getLong(it.getColumnIndexOrThrow("account_id")),
                        frequency = RecurringFrequency.valueOf(it.getString(it.getColumnIndexOrThrow("frequency"))),
                        nextDueDate = it.getLong(it.getColumnIndexOrThrow("next_due_date")),
                        isActive = it.getInt(it.getColumnIndexOrThrow("is_active")) == 1
                    )
                )
            }
        }
        _recurringItems.value = list
    }

    // ---------------- TRANSACTION CRUD ----------------

    fun addTransaction(tx: Transaction): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("amount", tx.amount)
            put("type", tx.type.name)
            put("category_id", tx.categoryId)
            put("account_id", tx.accountId)
            if (tx.toAccountId != null) put("to_account_id", tx.toAccountId) else putNull("to_account_id")
            put("timestamp", tx.timestamp)
            put("merchant", tx.merchant.trim())
            put("notes", tx.notes.trim())
            put("tag", tx.tag.trim())
            put("currency", _currencyCode.value)
            if (tx.receiptPath != null) put("receipt_path", tx.receiptPath) else putNull("receipt_path")
            put("created_at", System.currentTimeMillis())
        }
        val id = db.insert(MetronDatabaseHelper.TABLE_TRANSACTIONS, null, cv)
        reloadAll()
        return id
    }

    fun updateTransaction(tx: Transaction) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("amount", tx.amount)
            put("type", tx.type.name)
            put("category_id", tx.categoryId)
            put("account_id", tx.accountId)
            if (tx.toAccountId != null) put("to_account_id", tx.toAccountId) else putNull("to_account_id")
            put("timestamp", tx.timestamp)
            put("merchant", tx.merchant.trim())
            put("notes", tx.notes.trim())
            put("tag", tx.tag.trim())
            if (tx.receiptPath != null) put("receipt_path", tx.receiptPath) else putNull("receipt_path")
        }
        db.update(MetronDatabaseHelper.TABLE_TRANSACTIONS, cv, "id = ?", arrayOf(tx.id.toString()))
        reloadAll()
    }

    fun deleteTransaction(id: Long) {
        val item = _transactions.value.find { it.id == id }
        lastDeletedTransaction = item
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_TRANSACTIONS, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    fun undoDeleteTransaction(): Boolean {
        val item = lastDeletedTransaction ?: return false
        addTransaction(item)
        lastDeletedTransaction = null
        return true
    }

    fun duplicateTransaction(id: Long) {
        val item = _transactions.value.find { it.id == id } ?: return
        addTransaction(item.copy(id = 0, timestamp = System.currentTimeMillis(), createdAt = System.currentTimeMillis()))
    }

    // ---------------- CATEGORY CRUD ----------------

    fun addCategory(category: Category): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", category.name.trim())
            put("icon_name", category.iconName)
            put("color_hex", category.colorHex)
            put("type", category.type.name)
            put("is_default", if (category.isDefault) 1 else 0)
            put("display_order", _categories.value.size)
        }
        val id = db.insert(MetronDatabaseHelper.TABLE_CATEGORIES, null, cv)
        reloadAll()
        return id
    }

    fun updateCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", category.name.trim())
            put("icon_name", category.iconName)
            put("color_hex", category.colorHex)
            put("type", category.type.name)
        }
        db.update(MetronDatabaseHelper.TABLE_CATEGORIES, cv, "id = ?", arrayOf(category.id.toString()))
        reloadAll()
    }

    fun deleteCategory(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_CATEGORIES, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    // ---------------- ACCOUNT CRUD ----------------

    fun addAccount(account: Account): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", account.name.trim())
            put("type", account.type.name)
            put("initial_balance", account.initialBalance)
            put("color_hex", account.colorHex)
            put("icon_name", account.iconName)
        }
        val id = db.insert(MetronDatabaseHelper.TABLE_ACCOUNTS, null, cv)
        reloadAll()
        return id
    }

    fun updateAccount(account: Account) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", account.name.trim())
            put("type", account.type.name)
            put("initial_balance", account.initialBalance)
            put("color_hex", account.colorHex)
            put("icon_name", account.iconName)
        }
        db.update(MetronDatabaseHelper.TABLE_ACCOUNTS, cv, "id = ?", arrayOf(account.id.toString()))
        reloadAll()
    }

    fun deleteAccount(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_ACCOUNTS, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    // ---------------- BUDGETS ----------------

    fun setOverallMonthlyBudget(amount: Double) {
        val db = dbHelper.writableDatabase
        val existing = _budgets.value.find { it.categoryId == null }
        val cv = ContentValues().apply {
            putNull("category_id")
            put("amount", amount)
            put("period", BudgetPeriod.MONTHLY.name)
            put("month_year", "ALL")
        }
        if (existing != null) {
            db.update(MetronDatabaseHelper.TABLE_BUDGETS, cv, "id = ?", arrayOf(existing.id.toString()))
        } else {
            db.insert(MetronDatabaseHelper.TABLE_BUDGETS, null, cv)
        }
        reloadAll()
    }

    fun setCategoryBudget(categoryId: Long, amount: Double) {
        val db = dbHelper.writableDatabase
        val existing = _budgets.value.find { it.categoryId == categoryId }
        val cv = ContentValues().apply {
            put("category_id", categoryId)
            put("amount", amount)
            put("period", BudgetPeriod.MONTHLY.name)
            put("month_year", "ALL")
        }
        if (existing != null) {
            db.update(MetronDatabaseHelper.TABLE_BUDGETS, cv, "id = ?", arrayOf(existing.id.toString()))
        } else {
            db.insert(MetronDatabaseHelper.TABLE_BUDGETS, null, cv)
        }
        reloadAll()
    }

    fun deleteBudget(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_BUDGETS, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    // ---------------- RECURRING ITEMS ----------------

    fun addRecurringItem(item: RecurringItem): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", item.name.trim())
            put("amount", item.amount)
            put("type", item.type.name)
            put("category_id", item.categoryId)
            put("account_id", item.accountId)
            put("frequency", item.frequency.name)
            put("next_due_date", item.nextDueDate)
            put("is_active", if (item.isActive) 1 else 0)
        }
        val id = db.insert(MetronDatabaseHelper.TABLE_RECURRING, null, cv)
        reloadAll()
        return id
    }

    fun toggleRecurringItem(id: Long, active: Boolean) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_active", if (active) 1 else 0)
        }
        db.update(MetronDatabaseHelper.TABLE_RECURRING, cv, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    fun deleteRecurringItem(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_RECURRING, "id = ?", arrayOf(id.toString()))
        reloadAll()
    }

    fun markRecurringPaid(item: RecurringItem) {
        // Record as an actual transaction
        addTransaction(
            Transaction(
                amount = item.amount,
                type = item.type,
                categoryId = item.categoryId,
                accountId = item.accountId,
                timestamp = System.currentTimeMillis(),
                merchant = item.name,
                notes = "Auto-recorded recurring cycle (${item.frequency.title})"
            )
        )
        // Advance next due date
        val nextDue = item.nextDueDate + (item.frequency.daysInterval.toLong() * 24L * 60L * 60L * 1000L)
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("next_due_date", nextDue)
        }
        db.update(MetronDatabaseHelper.TABLE_RECURRING, cv, "id = ?", arrayOf(item.id.toString()))
        reloadAll()
    }

    // ---------------- PREFERENCES ----------------

    fun setCurrency(code: String, symbol: String) {
        setPreference("currency_code", code)
        setPreference("currency_symbol", symbol)
        _currencyCode.value = code
        _currencySymbol.value = symbol
    }

    fun setThemeMode(mode: String) {
        setPreference("theme_mode", mode)
        _themeMode.value = mode
    }

    fun setCalmMode(enabled: Boolean) {
        setPreference("calm_mode", if (enabled) "1" else "0")
        _isCalmMode.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        setPreference("haptics_enabled", if (enabled) "1" else "0")
        _isHapticsEnabled.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        setPreference("notifications_enabled", if (enabled) "1" else "0")
        _isNotificationsEnabled.value = enabled
        if (enabled) {
            com.metron.app.notification.MetronNotificationManager.scheduleDailyReminder(
                context, _dailyReminderHour.value, _dailyReminderMinute.value
            )
        } else {
            com.metron.app.notification.MetronNotificationManager.cancelDailyReminder(context)
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        setPreference("daily_reminder_hour", hour.toString())
        setPreference("daily_reminder_minute", minute.toString())
        _dailyReminderHour.value = hour
        _dailyReminderMinute.value = minute
        if (_isNotificationsEnabled.value) {
            com.metron.app.notification.MetronNotificationManager.scheduleDailyReminder(
                context, hour, minute
            )
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        setPreference("onboarding_completed", if (completed) "1" else "0")
        _isOnboardingCompleted.value = completed
    }

    fun saveReceiptImage(inputStream: java.io.InputStream): String? {
        return try {
            val dir = java.io.File(context.filesDir, "receipts")
            if (!dir.exists()) dir.mkdirs()
            val file = java.io.File(dir, "receipt_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                inputStream.copyTo(out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteReceiptImage(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = java.io.File(path)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setPreference(key: String, value: String) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(
            MetronDatabaseHelper.TABLE_PREFERENCES,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ---------------- LOCAL INTELLIGENCE ----------------

    fun getSuggestedCategoryForMerchant(merchant: String): Category? {
        if (merchant.isBlank()) return null
        val txs = _transactions.value.filter { it.merchant.equals(merchant.trim(), ignoreCase = true) }
        if (txs.isEmpty()) return null
        val mostFrequentCatId = txs.groupBy { it.categoryId }.maxByOrNull { it.value.size }?.key ?: return null
        return _categories.value.find { it.id == mostFrequentCatId }
    }

    fun getSuggestedAccountForMerchant(merchant: String): Account? {
        if (merchant.isBlank()) return null
        val txs = _transactions.value.filter { it.merchant.equals(merchant.trim(), ignoreCase = true) }
        if (txs.isEmpty()) return null
        val mostFrequentAccId = txs.groupBy { it.accountId }.maxByOrNull { it.value.size }?.key ?: return null
        return _accounts.value.find { it.id == mostFrequentAccId }
    }

    fun getRecentMerchants(limit: Int = 8): List<String> {
        return _transactions.value
            .map { it.merchant.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(limit)
    }

    fun generateGreekSpendingStory(): String {
        val allTx = _transactions.value
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthTxs = allTx.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }

        val totalSpent = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalEarned = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        if (monthTxs.isEmpty() || totalSpent == 0.0) {
            return "In ancient Greece, 'Métron' taught that measure and awareness are the foundations of true wealth. You are at the start of your journey — begin recording your transactions to illuminate your spending story."
        }

        val expensesByCategory = monthTxs.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val topCategoryEntry = expensesByCategory.maxByOrNull { it.value }
        val topCategory = _categories.value.find { it.id == topCategoryEntry?.key }

        val topPercent = if (totalSpent > 0 && topCategoryEntry != null) {
            ((topCategoryEntry.value / totalSpent) * 100).toInt()
        } else 0

        val sym = _currencySymbol.value
        val monthlyBudget = _budgets.value.find { it.categoryId == null }?.amount ?: 0.0

        val budgetStatus = when {
            monthlyBudget <= 0 -> "You have tracked $sym${String.format("%.0f", totalSpent)} in total expenses."
            totalSpent <= monthlyBudget * 0.7 -> "You are demonstrating exemplary restraint, comfortably within your monthly measure ($sym${String.format("%.0f", monthlyBudget - totalSpent)} remaining)."
            totalSpent <= monthlyBudget -> "You have utilized $sym${String.format("%.0f", totalSpent)} of your $sym${String.format("%.0f", monthlyBudget)} budget. Maintain your discipline as the month closes."
            else -> "Expenses have slightly exceeded your planned measure ($sym${String.format("%.0f", totalSpent)} vs $sym${String.format("%.0f", monthlyBudget)}). Review recurring or discretionary expenses to restore equilibrium."
        }

        val topInfo = if (topCategory != null) {
            "Your largest focus was ${topCategory.name}, representing $topPercent% ($sym${String.format("%.0f", topCategoryEntry?.value ?: 0.0)}) of your outgoings."
        } else ""

        val cashflowInfo = if (totalEarned > 0) {
            val savingsRate = (((totalEarned - totalSpent) / totalEarned) * 100).toInt().coerceAtLeast(0)
            "With total inflow of $sym${String.format("%.0f", totalEarned)}, your net savings rate stands at $savingsRate%."
        } else ""

        return "As Cleobulus of Lindos proclaimed: 'Métron áriston' — Measure in all things. This month, $budgetStatus $topInfo $cashflowInfo"
    }

    // ---------------- DATA EXPORT & IMPORT & DEMO ----------------

    fun exportToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("appName", "Metron")

        // Categories
        val catsArray = JSONArray()
        for (c in _categories.value) {
            val o = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("iconName", c.iconName)
                put("colorHex", c.colorHex)
                put("type", c.type.name)
                put("isDefault", c.isDefault)
                put("displayOrder", c.displayOrder)
            }
            catsArray.put(o)
        }
        root.put("categories", catsArray)

        // Accounts
        val accsArray = JSONArray()
        for (a in _accounts.value) {
            val o = JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("type", a.type.name)
                put("initialBalance", a.initialBalance)
                put("colorHex", a.colorHex)
                put("iconName", a.iconName)
            }
            accsArray.put(o)
        }
        root.put("accounts", accsArray)

        // Transactions
        val txsArray = JSONArray()
        for (t in _transactions.value) {
            val o = JSONObject().apply {
                put("id", t.id)
                put("amount", t.amount)
                put("type", t.type.name)
                put("categoryId", t.categoryId)
                put("accountId", t.accountId)
                if (t.toAccountId != null) put("toAccountId", t.toAccountId)
                put("timestamp", t.timestamp)
                put("merchant", t.merchant)
                put("notes", t.notes)
                put("tag", t.tag)
                put("currency", t.currency)
                if (t.receiptPath != null) put("receiptPath", t.receiptPath)
            }
            txsArray.put(o)
        }
        root.put("transactions", txsArray)

        // Budgets
        val bArray = JSONArray()
        for (b in _budgets.value) {
            val o = JSONObject().apply {
                put("id", b.id)
                if (b.categoryId != null) put("categoryId", b.categoryId)
                put("amount", b.amount)
                put("period", b.period.name)
                put("monthYear", b.monthYear)
            }
            bArray.put(o)
        }
        root.put("budgets", bArray)

        // Recurring
        val recArray = JSONArray()
        for (r in _recurringItems.value) {
            val o = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("amount", r.amount)
                put("type", r.type.name)
                put("categoryId", r.categoryId)
                put("accountId", r.accountId)
                put("frequency", r.frequency.name)
                put("nextDueDate", r.nextDueDate)
                put("isActive", r.isActive)
            }
            recArray.put(o)
        }
        root.put("recurring", recArray)

        return root.toString(2)
    }

    fun exportToCsv(): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Category,Merchant,Amount,Currency,Account,Notes,Tag\n")
        val catMap = _categories.value.associateBy { it.id }
        val accMap = _accounts.value.associateBy { it.id }
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        for (t in _transactions.value) {
            val dateStr = sdfDate.format(Date(t.timestamp))
            val timeStr = sdfTime.format(Date(t.timestamp))
            val catName = catMap[t.categoryId]?.name ?: "Unknown"
            val accName = accMap[t.accountId]?.name ?: "Unknown"
            val merchantSafe = t.merchant.replace("\"", "\"\"")
            val notesSafe = t.notes.replace("\"", "\"\"")
            val tagSafe = t.tag.replace("\"", "\"\"")

            sb.append("${t.id},")
            sb.append("$dateStr,")
            sb.append("$timeStr,")
            sb.append("${t.type.name},")
            sb.append("\"$catName\",")
            sb.append("\"$merchantSafe\",")
            sb.append("${t.amount},")
            sb.append("${t.currency},")
            sb.append("\"$accName\",")
            sb.append("\"$notesSafe\",")
            sb.append("\"$tagSafe\"\n")
        }
        return sb.toString()
    }

    fun importFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                // Clear existing
                db.delete(MetronDatabaseHelper.TABLE_TRANSACTIONS, null, null)
                db.delete(MetronDatabaseHelper.TABLE_CATEGORIES, null, null)
                db.delete(MetronDatabaseHelper.TABLE_ACCOUNTS, null, null)
                db.delete(MetronDatabaseHelper.TABLE_BUDGETS, null, null)
                db.delete(MetronDatabaseHelper.TABLE_RECURRING, null, null)

                // Restore categories
                val cats = root.optJSONArray("categories")
                if (cats != null) {
                    for (i in 0 until cats.length()) {
                        val o = cats.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", o.getLong("id"))
                            put("name", o.getString("name"))
                            put("icon_name", o.getString("iconName"))
                            put("color_hex", o.getString("colorHex"))
                            put("type", o.getString("type"))
                            put("is_default", if (o.optBoolean("isDefault", false)) 1 else 0)
                            put("display_order", o.optInt("displayOrder", i))
                        }
                        db.insert(MetronDatabaseHelper.TABLE_CATEGORIES, null, cv)
                    }
                }

                // Restore accounts
                val accs = root.optJSONArray("accounts")
                if (accs != null) {
                    for (i in 0 until accs.length()) {
                        val o = accs.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", o.getLong("id"))
                            put("name", o.getString("name"))
                            put("type", o.getString("type"))
                            put("initial_balance", o.getDouble("initialBalance"))
                            put("color_hex", o.getString("colorHex"))
                            put("icon_name", o.getString("iconName"))
                        }
                        db.insert(MetronDatabaseHelper.TABLE_ACCOUNTS, null, cv)
                    }
                }

                // Restore transactions
                val txs = root.optJSONArray("transactions")
                if (txs != null) {
                    for (i in 0 until txs.length()) {
                        val o = txs.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", o.getLong("id"))
                            put("amount", o.getDouble("amount"))
                            put("type", o.getString("type"))
                            put("category_id", o.getLong("categoryId"))
                            put("account_id", o.getLong("accountId"))
                            if (o.has("toAccountId")) put("to_account_id", o.getLong("toAccountId")) else putNull("to_account_id")
                            put("timestamp", o.getLong("timestamp"))
                            put("merchant", o.getString("merchant"))
                            put("notes", o.optString("notes", ""))
                            put("tag", o.optString("tag", ""))
                            put("currency", o.optString("currency", "INR"))
                            if (o.has("receiptPath")) put("receipt_path", o.getString("receiptPath")) else putNull("receipt_path")
                            put("created_at", System.currentTimeMillis())
                        }
                        db.insert(MetronDatabaseHelper.TABLE_TRANSACTIONS, null, cv)
                    }
                }

                // Restore budgets
                val buds = root.optJSONArray("budgets")
                if (buds != null) {
                    for (i in 0 until buds.length()) {
                        val o = buds.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", o.getLong("id"))
                            if (o.has("categoryId")) put("category_id", o.getLong("categoryId")) else putNull("category_id")
                            put("amount", o.getDouble("amount"))
                            put("period", o.getString("period"))
                            put("month_year", o.optString("monthYear", "ALL"))
                        }
                        db.insert(MetronDatabaseHelper.TABLE_BUDGETS, null, cv)
                    }
                }

                // Restore recurring
                val recs = root.optJSONArray("recurring")
                if (recs != null) {
                    for (i in 0 until recs.length()) {
                        val o = recs.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", o.getLong("id"))
                            put("name", o.getString("name"))
                            put("amount", o.getDouble("amount"))
                            put("type", o.getString("type"))
                            put("category_id", o.getLong("categoryId"))
                            put("account_id", o.getLong("accountId"))
                            put("frequency", o.getString("frequency"))
                            put("next_due_date", o.getLong("nextDueDate"))
                            put("is_active", if (o.optBoolean("isActive", true)) 1 else 0)
                        }
                        db.insert(MetronDatabaseHelper.TABLE_RECURRING, null, cv)
                    }
                }

                db.setTransactionSuccessful()
                true
            } finally {
                db.endTransaction()
                reloadAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearAllData() {
        val db = dbHelper.writableDatabase
        db.delete(MetronDatabaseHelper.TABLE_TRANSACTIONS, null, null)
        db.delete(MetronDatabaseHelper.TABLE_BUDGETS, null, null)
        db.delete(MetronDatabaseHelper.TABLE_RECURRING, null, null)
        reloadAll()
    }

    fun generateDemoData() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Find categories
            val cats = _categories.value
            val accs = _accounts.value
            if (cats.isEmpty() || accs.isEmpty()) return

            val foodCat = cats.find { it.name.contains("Food", true) } ?: cats[0]
            val groceryCat = cats.find { it.name.contains("Groceries", true) } ?: cats[0]
            val transCat = cats.find { it.name.contains("Transport", true) } ?: cats[0]
            val shopCat = cats.find { it.name.contains("Shopping", true) } ?: cats[0]
            val utilCat = cats.find { it.name.contains("Bills", true) } ?: cats[0]
            val entCat = cats.find { it.name.contains("Entertainment", true) } ?: cats[0]
            val salaryCat = cats.find { it.name.contains("Salary", true) } ?: cats[0]
            val subCat = cats.find { it.name.contains("Subscriptions", true) } ?: cats[0]

            val cashAcc = accs.find { it.type == AccountType.CASH } ?: accs[0]
            val bankAcc = accs.find { it.type == AccountType.BANK } ?: accs[0]
            val upiAcc = accs.find { it.type == AccountType.UPI } ?: accs[0]
            val cardAcc = accs.find { it.type == AccountType.CREDIT_CARD } ?: accs[0]

            val now = System.currentTimeMillis()
            val dayMs = 24L * 60L * 60L * 1000L

            val demoTxs = listOf(
                // Today
                Transaction(amount = 240.0, type = TransactionType.EXPENSE, categoryId = foodCat.id, accountId = upiAcc.id, timestamp = now - 3600000L * 2, merchant = "Blue Tokai Coffee", notes = "Morning flat white"),
                Transaction(amount = 180.0, type = TransactionType.EXPENSE, categoryId = transCat.id, accountId = upiAcc.id, timestamp = now - 3600000L * 4, merchant = "Metro Card Recharge", notes = "Weekly transit"),
                Transaction(amount = 650.0, type = TransactionType.EXPENSE, categoryId = foodCat.id, accountId = cardAcc.id, timestamp = now - 3600000L * 6, merchant = "Swiggy Gourmet", notes = "Greek salad & pita"),

                // Yesterday
                Transaction(amount = 1450.0, type = TransactionType.EXPENSE, categoryId = groceryCat.id, accountId = bankAcc.id, timestamp = now - dayMs - 3600000L * 3, merchant = "Nature's Basket", notes = "Organic fruits, olive oil"),
                Transaction(amount = 90.0, type = TransactionType.EXPENSE, categoryId = transCat.id, accountId = cashAcc.id, timestamp = now - dayMs - 3600000L * 8, merchant = "Auto Rickshaw", notes = "Commute to studio"),

                // 2 days ago
                Transaction(amount = 499.0, type = TransactionType.EXPENSE, categoryId = subCat.id, accountId = cardAcc.id, timestamp = now - dayMs * 2, merchant = "Spotify Premium", notes = "Monthly subscription"),
                Transaction(amount = 1200.0, type = TransactionType.EXPENSE, categoryId = entCat.id, accountId = upiAcc.id, timestamp = now - dayMs * 2 - 3600000L * 5, merchant = "PVR IMAX", notes = "Weekend film tickets"),

                // 3 days ago
                Transaction(amount = 3200.0, type = TransactionType.EXPENSE, categoryId = shopCat.id, accountId = cardAcc.id, timestamp = now - dayMs * 3, merchant = "Uniqlo", notes = "Linen shirts"),
                Transaction(amount = 350.0, type = TransactionType.EXPENSE, categoryId = foodCat.id, accountId = cashAcc.id, timestamp = now - dayMs * 3 - 3600000L * 4, merchant = "Artisan Bakery", notes = "Sourdough loaf"),

                // 5 days ago
                Transaction(amount = 1850.0, type = TransactionType.EXPENSE, categoryId = utilCat.id, accountId = bankAcc.id, timestamp = now - dayMs * 5, merchant = "Airtel Fiber Gigabit", notes = "High-speed broadband"),

                // 7 days ago
                Transaction(amount = 58000.0, type = TransactionType.INCOME, categoryId = salaryCat.id, accountId = bankAcc.id, timestamp = now - dayMs * 7, merchant = "Metron Technologies", notes = "Monthly salary credit"),

                // 10 days ago
                Transaction(amount = 2100.0, type = TransactionType.EXPENSE, categoryId = groceryCat.id, accountId = upiAcc.id, timestamp = now - dayMs * 10, merchant = "Blinkit Essentials", notes = "Weekly household supplies"),

                // 14 days ago
                Transaction(amount = 850.0, type = TransactionType.EXPENSE, categoryId = foodCat.id, accountId = upiAcc.id, timestamp = now - dayMs * 14, merchant = "Olive & Feta Bistro", notes = "Dinner with friends")
            )

            for (tx in demoTxs) {
                val cv = ContentValues().apply {
                    put("amount", tx.amount)
                    put("type", tx.type.name)
                    put("category_id", tx.categoryId)
                    put("account_id", tx.accountId)
                    put("timestamp", tx.timestamp)
                    put("merchant", tx.merchant)
                    put("notes", tx.notes)
                    put("tag", "Demo")
                    put("currency", _currencyCode.value)
                    put("created_at", tx.timestamp)
                }
                db.insert(MetronDatabaseHelper.TABLE_TRANSACTIONS, null, cv)
            }

            // Also seed a couple of recurring subscriptions
            val rec1 = ContentValues().apply {
                put("name", "Netflix 4K")
                put("amount", 649.0)
                put("type", TransactionType.EXPENSE.name)
                put("category_id", subCat.id)
                put("account_id", cardAcc.id)
                put("frequency", RecurringFrequency.MONTHLY.name)
                put("next_due_date", now + dayMs * 8)
                put("is_active", 1)
            }
            db.insert(MetronDatabaseHelper.TABLE_RECURRING, null, rec1)

            val rec2 = ContentValues().apply {
                put("name", "Gym & Pool Membership")
                put("amount", 2500.0)
                put("type", TransactionType.EXPENSE.name)
                put("category_id", cats.find { it.name.contains("Health", true) }?.id ?: cats[0].id)
                put("account_id", upiAcc.id)
                put("frequency", RecurringFrequency.MONTHLY.name)
                put("next_due_date", now + dayMs * 15)
                put("is_active", 1)
            }
            db.insert(MetronDatabaseHelper.TABLE_RECURRING, null, rec2)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            reloadAll()
        }
    }
}
