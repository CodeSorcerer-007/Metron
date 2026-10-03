package com.metron.app

import com.metron.app.model.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class MetronCoreTest {

    // ── 1. Financial Income, Expense & Net Balance Math ─────────────────────────────

    @Test
    fun testFinancialCalculations_ExpenseAndIncome() {
        val transactions = listOf(
            Transaction(id = 1, amount = 250.0, type = TransactionType.EXPENSE, categoryId = 1, accountId = 1, merchant = "Coffee"),
            Transaction(id = 2, amount = 1450.0, type = TransactionType.EXPENSE, categoryId = 2, accountId = 2, merchant = "Groceries"),
            Transaction(id = 3, amount = 50000.0, type = TransactionType.INCOME, categoryId = 3, accountId = 2, merchant = "Salary")
        )

        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpenses

        assertEquals(1700.0, totalExpenses, 0.01)
        assertEquals(50000.0, totalIncome, 0.01)
        assertEquals(48300.0, netBalance, 0.01)
    }

    // ── 2. Account Transfer Balance Arithmetic ──────────────────────────────────────

    @Test
    fun testAccountTransferBalanceArithmetic() {
        // Initial setup: Bank (10,000), Cash (1,000), UPI (5,000)
        var bankBalance = 10000.0
        var cashBalance = 1000.0
        var upiBalance = 5000.0

        val initialNetWorth = bankBalance + cashBalance + upiBalance
        assertEquals(16000.0, initialNetWorth, 0.01)

        val transactions = listOf(
            // 1. Salary credited to Bank (+40,000)
            Transaction(id = 1, amount = 40000.0, type = TransactionType.INCOME, categoryId = 10, accountId = 1, merchant = "Salary"),
            // 2. Transfer from Bank to Cash: ATM withdrawal (5,000)
            Transaction(id = 2, amount = 5000.0, type = TransactionType.TRANSFER, categoryId = 1, accountId = 1, toAccountId = 2, merchant = "ATM Cash Withdrawal"),
            // 3. Transfer from Bank to UPI (10,000)
            Transaction(id = 3, amount = 10000.0, type = TransactionType.TRANSFER, categoryId = 1, accountId = 1, toAccountId = 3, merchant = "Topup UPI Wallet"),
            // 4. Cash expense: Groceries (-1,200)
            Transaction(id = 4, amount = 1200.0, type = TransactionType.EXPENSE, categoryId = 2, accountId = 2, merchant = "Local Market"),
            // 5. UPI expense: Dining (-850)
            Transaction(id = 5, amount = 850.0, type = TransactionType.EXPENSE, categoryId = 3, accountId = 3, merchant = "Bistro Dinner")
        )

        // Simulate Metron's balance calculation algorithm
        fun computeBalance(accountId: Long, initial: Double): Double {
            var bal = initial
            for (tx in transactions) {
                when (tx.type) {
                    TransactionType.INCOME -> {
                        if (tx.accountId == accountId) bal += tx.amount
                    }
                    TransactionType.EXPENSE -> {
                        if (tx.accountId == accountId) bal -= tx.amount
                    }
                    TransactionType.TRANSFER -> {
                        if (tx.accountId == accountId) bal -= tx.amount
                        if (tx.toAccountId == accountId) bal += tx.amount
                    }
                }
            }
            return bal
        }

        val finalBank = computeBalance(1, bankBalance)
        val finalCash = computeBalance(2, cashBalance)
        val finalUpi = computeBalance(3, upiBalance)

        // Bank: 10,000 + 40,000 - 5,000 - 10,000 = 35,000
        assertEquals(35000.0, finalBank, 0.01)

        // Cash: 1,000 + 5,000 - 1,200 = 4,800
        assertEquals(4800.0, finalCash, 0.01)

        // UPI: 5,000 + 10,000 - 850 = 14,150
        assertEquals(14150.0, finalUpi, 0.01)

        // Total Net Worth check: Initial (16,000) + Income (40,000) - Expenses (2,050) = 53,950
        val finalNetWorth = finalBank + finalCash + finalUpi
        assertEquals(53950.0, finalNetWorth, 0.01)
    }

    // ── 3. JSON Export & Import Round-Trip Serialization ────────────────────────────

    @Test
    fun testJsonExportAndImportRoundTripStructure() {
        val originalCategories = listOf(
            Category(id = 1, name = "Food & Dining", iconName = "Restaurant", colorHex = "#F59E0B", type = CategoryType.EXPENSE, isDefault = true, displayOrder = 0),
            Category(id = 2, name = "Salary", iconName = "Payments", colorHex = "#10B981", type = CategoryType.INCOME, isDefault = true, displayOrder = 50)
        )

        val originalAccounts = listOf(
            Account(id = 1, name = "Cash", type = AccountType.CASH, initialBalance = 500.0, currentBalance = 500.0, colorHex = "#10B981", iconName = "AccountBalanceWallet"),
            Account(id = 2, name = "Main Bank", type = AccountType.BANK, initialBalance = 25000.0, currentBalance = 25000.0, colorHex = "#38BDF8", iconName = "AccountBalance")
        )

        val originalTransactions = listOf(
            Transaction(
                id = 101,
                amount = 450.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                accountId = 1,
                toAccountId = null,
                timestamp = 1727950000000L,
                merchant = "Greek Tavern \"Olympus\"",
                notes = "Dinner with friends, split bill",
                tag = "Social",
                currency = "INR",
                receiptPath = "/data/user/0/com.metron.app/files/receipts/receipt_101.jpg"
            ),
            Transaction(
                id = 102,
                amount = 2000.0,
                type = TransactionType.TRANSFER,
                categoryId = 1,
                accountId = 2,
                toAccountId = 1,
                timestamp = 1727955000000L,
                merchant = "ATM Cash",
                notes = "Weekend cash",
                tag = "Transfer",
                currency = "INR",
                receiptPath = null
            )
        )

        val originalBudgets = listOf(
            Budget(id = 1, categoryId = null, amount = 35000.0, period = BudgetPeriod.MONTHLY, monthYear = "ALL"),
            Budget(id = 2, categoryId = 1, amount = 12000.0, period = BudgetPeriod.MONTHLY, monthYear = "ALL")
        )

        val originalRecurring = listOf(
            RecurringItem(id = 1, name = "Spotify", amount = 179.0, type = TransactionType.EXPENSE, categoryId = 1, accountId = 2, frequency = RecurringFrequency.MONTHLY, nextDueDate = 1728500000000L, isActive = true)
        )

        // 1. Serialize to JSON (matching MetronRepository.exportToJson format)
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Metron")
        root.put("exportDate", System.currentTimeMillis())

        val catsArray = JSONArray()
        for (c in originalCategories) {
            catsArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("iconName", c.iconName)
                put("colorHex", c.colorHex)
                put("type", c.type.name)
                put("isDefault", c.isDefault)
                put("displayOrder", c.displayOrder)
            })
        }
        root.put("categories", catsArray)

        val accsArray = JSONArray()
        for (a in originalAccounts) {
            accsArray.put(JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("type", a.type.name)
                put("initialBalance", a.initialBalance)
                put("colorHex", a.colorHex)
                put("iconName", a.iconName)
            })
        }
        root.put("accounts", accsArray)

        val txsArray = JSONArray()
        for (t in originalTransactions) {
            txsArray.put(JSONObject().apply {
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
            })
        }
        root.put("transactions", txsArray)

        val budsArray = JSONArray()
        for (b in originalBudgets) {
            budsArray.put(JSONObject().apply {
                put("id", b.id)
                if (b.categoryId != null) put("categoryId", b.categoryId)
                put("amount", b.amount)
                put("period", b.period.name)
                put("monthYear", b.monthYear)
            })
        }
        root.put("budgets", budsArray)

        val recArray = JSONArray()
        for (r in originalRecurring) {
            recArray.put(JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("amount", r.amount)
                put("type", r.type.name)
                put("categoryId", r.categoryId)
                put("accountId", r.accountId)
                put("frequency", r.frequency.name)
                put("nextDueDate", r.nextDueDate)
                put("isActive", r.isActive)
            })
        }
        root.put("recurring", recArray)

        val exportedJsonString = root.toString(2)
        assertNotNull(exportedJsonString)
        assertTrue(exportedJsonString.contains("Greek Tavern \\\"Olympus\\\""))

        // 2. Deserialize from JSON (matching MetronRepository.importFromJson logic)
        val parsedRoot = JSONObject(exportedJsonString)
        assertEquals(1, parsedRoot.getInt("version"))
        assertEquals("Metron", parsedRoot.getString("appName"))

        val parsedCats = parsedRoot.getJSONArray("categories")
        assertEquals(2, parsedCats.length())
        assertEquals("Food & Dining", parsedCats.getJSONObject(0).getString("name"))

        val parsedAccs = parsedRoot.getJSONArray("accounts")
        assertEquals(2, parsedAccs.length())
        assertEquals("Main Bank", parsedAccs.getJSONObject(1).getString("name"))

        val parsedTxs = parsedRoot.getJSONArray("transactions")
        assertEquals(2, parsedTxs.length())
        val tx1 = parsedTxs.getJSONObject(0)
        assertEquals(101L, tx1.getLong("id"))
        assertEquals(450.0, tx1.getDouble("amount"), 0.001)
        assertEquals("Greek Tavern \"Olympus\"", tx1.getString("merchant"))
        assertEquals("Social", tx1.getString("tag"))
        assertEquals("/data/user/0/com.metron.app/files/receipts/receipt_101.jpg", tx1.getString("receiptPath"))

        val tx2 = parsedTxs.getJSONObject(1)
        assertEquals(102L, tx2.getLong("id"))
        assertEquals(1L, tx2.getLong("toAccountId"))

        val parsedBuds = parsedRoot.getJSONArray("budgets")
        assertEquals(2, parsedBuds.length())
        assertEquals(35000.0, parsedBuds.getJSONObject(0).getDouble("amount"), 0.001)

        val parsedRec = parsedRoot.getJSONArray("recurring")
        assertEquals(1, parsedRec.length())
        assertEquals("Spotify", parsedRec.getJSONObject(0).getString("name"))
        assertTrue(parsedRec.getJSONObject(0).getBoolean("isActive"))
    }

    // ── 4. Merchant Category & Account Suggestion (Merchant Memory) ─────────────────

    @Test
    fun testMerchantMemorySuggestions() {
        val categories = listOf(
            Category(id = 1, name = "Food & Dining"),
            Category(id = 2, name = "Transport"),
            Category(id = 3, name = "Groceries")
        )
        val accounts = listOf(
            Account(id = 10, name = "Cash"),
            Account(id = 20, name = "UPI"),
            Account(id = 30, name = "Credit Card")
        )

        val transactions = listOf(
            Transaction(amount = 250.0, categoryId = 1, accountId = 20, merchant = "Blue Tokai Coffee"),
            Transaction(amount = 320.0, categoryId = 1, accountId = 20, merchant = "Blue Tokai Coffee"),
            Transaction(amount = 180.0, categoryId = 2, accountId = 20, merchant = "Blue Tokai Coffee"), // Outlier
            Transaction(amount = 1450.0, categoryId = 3, accountId = 30, merchant = "Nature's Basket"),
            Transaction(amount = 90.0, categoryId = 2, accountId = 10, merchant = "Auto Rickshaw")
        )

        // Suggest category logic
        fun suggestCategory(merchant: String): Category? {
            if (merchant.isBlank()) return null
            val matches = transactions.filter { it.merchant.equals(merchant.trim(), ignoreCase = true) }
            if (matches.isEmpty()) return null
            val bestCatId = matches.groupBy { it.categoryId }.maxByOrNull { it.value.size }?.key ?: return null
            return categories.find { it.id == bestCatId }
        }

        // Suggest account logic
        fun suggestAccount(merchant: String): Account? {
            if (merchant.isBlank()) return null
            val matches = transactions.filter { it.merchant.equals(merchant.trim(), ignoreCase = true) }
            if (matches.isEmpty()) return null
            val bestAccId = matches.groupBy { it.accountId }.maxByOrNull { it.value.size }?.key ?: return null
            return accounts.find { it.id == bestAccId }
        }

        // 1. "Blue Tokai Coffee" should resolve to Category 1 (Food & Dining) and Account 20 (UPI)
        val cat1 = suggestCategory("blue tokai coffee")
        val acc1 = suggestAccount("BLUE TOKAI COFFEE")
        assertNotNull(cat1)
        assertEquals("Food & Dining", cat1?.name)
        assertNotNull(acc1)
        assertEquals("UPI", acc1?.name)

        // 2. "Nature's Basket" should resolve to Category 3 (Groceries) and Account 30 (Credit Card)
        val cat2 = suggestCategory("Nature's Basket")
        val acc2 = suggestAccount("Nature's Basket")
        assertEquals("Groceries", cat2?.name)
        assertEquals("Credit Card", acc2?.name)

        // 3. Unknown merchant should return null
        assertNull(suggestCategory("Brand New Unknown Shop"))
        assertNull(suggestAccount("Brand New Unknown Shop"))
    }

    // ── 5. CSV Export Formatting & Escaping ──────────────────────────────────────────

    @Test
    fun testCsvExportFormatAndEscaping() {
        val categoryMap = mapOf(1L to Category(id = 1, name = "Food & Dining"))
        val accountMap = mapOf(10L to Account(id = 10, name = "UPI Wallet"))

        val tx = Transaction(
            id = 42,
            amount = 1250.50,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            accountId = 10,
            timestamp = 1727950000000L,
            merchant = "Cafe, \"The Aristotelian\"",
            notes = "Special dinner, celebrated milestone",
            tag = "Celebration",
            currency = "INR"
        )

        val catName = categoryMap[tx.categoryId]?.name ?: "Unknown"
        val accName = accountMap[tx.accountId]?.name ?: "Unknown"

        val merchantSafe = tx.merchant.replace("\"", "\"\"")
        val notesSafe = tx.notes.replace("\"", "\"\"")
        val tagSafe = tx.tag.replace("\"", "\"\"")

        val csvLine = "${tx.id},${tx.type.name},\"$catName\",\"$merchantSafe\",${tx.amount},${tx.currency},\"$accName\",\"$notesSafe\",\"$tagSafe\""

        assertTrue(csvLine.contains("\"Cafe, \"\"The Aristotelian\"\"\""))
        assertTrue(csvLine.contains("\"Special dinner, celebrated milestone\""))
        assertTrue(csvLine.contains("1250.5"))
    }

    // ── 6. Budget Allowance Calculations & Edge Cases ───────────────────────────────

    @Test
    fun testBudgetAllowanceCalculations() {
        val monthlyBudget = 30000.0
        val spentSoFar = 12000.0
        val daysRemaining = 15

        val remainingBudget = (monthlyBudget - spentSoFar).coerceAtLeast(0.0)
        val dailyAllowance = remainingBudget / daysRemaining
        val progress = (spentSoFar / monthlyBudget).toFloat()

        assertEquals(18000.0, remainingBudget, 0.01)
        assertEquals(1200.0, dailyAllowance, 0.01)
        assertEquals(0.4f, progress, 0.001f)
    }

    @Test
    fun testBudgetAllowance_OverbudgetEdgeCase() {
        val monthlyBudget = 25000.0
        val spentSoFar = 28000.0 // Over budget
        val daysRemaining = 5

        val remainingBudget = (monthlyBudget - spentSoFar).coerceAtLeast(0.0)
        val dailyAllowance = if (daysRemaining > 0) remainingBudget / daysRemaining else 0.0
        val progress = (spentSoFar / monthlyBudget).toFloat()

        assertEquals(0.0, remainingBudget, 0.01)
        assertEquals(0.0, dailyAllowance, 0.01)
        assertTrue(progress > 1.0f)
    }

    // ── 7. Enums & Model Validation ────────────────────────────────────────────────

    @Test
    fun testRecurringFrequencyDaysInterval() {
        assertEquals(1, RecurringFrequency.DAILY.daysInterval)
        assertEquals(7, RecurringFrequency.WEEKLY.daysInterval)
        assertEquals(30, RecurringFrequency.MONTHLY.daysInterval)
        assertEquals(365, RecurringFrequency.YEARLY.daysInterval)
    }

    @Test
    fun testTransactionTypeEnumValues() {
        val types = TransactionType.values().map { it.name }
        assertTrue(types.contains("EXPENSE"))
        assertTrue(types.contains("INCOME"))
        assertTrue(types.contains("TRANSFER"))
    }
}
