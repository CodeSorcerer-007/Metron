package com.metron.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.metron.app.model.*

class MetronDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "metron_offline.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_ACCOUNTS = "accounts"
        const val TABLE_BUDGETS = "budgets"
        const val TABLE_RECURRING = "recurring"
        const val TABLE_PREFERENCES = "preferences"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Categories table
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                icon_name TEXT NOT NULL,
                color_hex TEXT NOT NULL,
                type TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0,
                display_order INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Accounts table
        db.execSQL("""
            CREATE TABLE $TABLE_ACCOUNTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                initial_balance REAL NOT NULL DEFAULT 0.0,
                color_hex TEXT NOT NULL,
                icon_name TEXT NOT NULL
            )
        """.trimIndent())

        // Transactions table
        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                to_account_id INTEGER,
                timestamp INTEGER NOT NULL,
                merchant TEXT NOT NULL,
                notes TEXT NOT NULL DEFAULT '',
                tag TEXT NOT NULL DEFAULT '',
                currency TEXT NOT NULL DEFAULT 'INR',
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        // Budgets table
        db.execSQL("""
            CREATE TABLE $TABLE_BUDGETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER,
                amount REAL NOT NULL,
                period TEXT NOT NULL,
                month_year TEXT NOT NULL DEFAULT 'ALL'
            )
        """.trimIndent())

        // Recurring table
        db.execSQL("""
            CREATE TABLE $TABLE_RECURRING (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                frequency TEXT NOT NULL,
                next_due_date INTEGER NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // Preferences table
        db.execSQL("""
            CREATE TABLE $TABLE_PREFERENCES (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())

        // Create performance indices
        db.execSQL("CREATE INDEX idx_trans_timestamp ON $TABLE_TRANSACTIONS(timestamp DESC)")
        db.execSQL("CREATE INDEX idx_trans_category ON $TABLE_TRANSACTIONS(category_id)")
        db.execSQL("CREATE INDEX idx_trans_account ON $TABLE_TRANSACTIONS(account_id)")
        db.execSQL("CREATE INDEX idx_trans_type ON $TABLE_TRANSACTIONS(type)")

        // Seed default categories
        seedCategories(db)

        // Seed default accounts
        seedAccounts(db)

        // Seed initial preferences
        seedPreferences(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations if schema evolves
    }

    private fun seedCategories(db: SQLiteDatabase) {
        val categories = listOf(
            // Expense Categories
            Triple("Food & Dining", "Restaurant", "#F59E0B"),
            Triple("Groceries", "ShoppingCart", "#10B981"),
            Triple("Transport", "DirectionsCar", "#38BDF8"),
            Triple("Shopping", "ShoppingBag", "#EC4899"),
            Triple("Housing & Rent", "Home", "#8B5CF6"),
            Triple("Bills & Utilities", "ReceiptLong", "#06B6D4"),
            Triple("Entertainment", "ConfirmationNumber", "#F43F5E"),
            Triple("Health & Fitness", "FitnessCenter", "#14B8A6"),
            Triple("Education", "MenuBook", "#6366F1"),
            Triple("Subscriptions", "SmartDisplay", "#E11D48"),
            Triple("Personal Care", "Spa", "#FB7185"),
            Triple("Travel", "Flight", "#0284C7"),
            Triple("Greek Measure / Gifts", "AutoAwesome", "#D4AF37"),
            Triple("Other Expenses", "MoreHoriz", "#64748B")
        )

        categories.forEachIndexed { index, (name, icon, color) ->
            val cv = ContentValues().apply {
                put("name", name)
                put("icon_name", icon)
                put("color_hex", color)
                put("type", CategoryType.EXPENSE.name)
                put("is_default", 1)
                put("display_order", index)
            }
            db.insert(TABLE_CATEGORIES, null, cv)
        }

        val incomeCategories = listOf(
            Triple("Salary & Wages", "Payments", "#10B981"),
            Triple("Freelance", "Work", "#3B82F6"),
            Triple("Investments", "TrendingUp", "#D4AF37"),
            Triple("Gifts & Grants", "CardGiftcard", "#EC4899"),
            Triple("Other Income", "Savings", "#059669")
        )

        incomeCategories.forEachIndexed { index, (name, icon, color) ->
            val cv = ContentValues().apply {
                put("name", name)
                put("icon_name", icon)
                put("color_hex", color)
                put("type", CategoryType.INCOME.name)
                put("is_default", 1)
                put("display_order", index + 50)
            }
            db.insert(TABLE_CATEGORIES, null, cv)
        }
    }

    private fun seedAccounts(db: SQLiteDatabase) {
        val accounts = listOf(
            Account(name = "Cash", type = AccountType.CASH, initialBalance = 1500.0, colorHex = "#10B981", iconName = "AccountBalanceWallet"),
            Account(name = "Main Bank", type = AccountType.BANK, initialBalance = 35000.0, colorHex = "#38BDF8", iconName = "AccountBalance"),
            Account(name = "UPI / Wallet", type = AccountType.UPI, initialBalance = 6500.0, colorHex = "#8B5CF6", iconName = "QrCodeScanner"),
            Account(name = "Credit Card", type = AccountType.CREDIT_CARD, initialBalance = 0.0, colorHex = "#F43F5E", iconName = "CreditCard")
        )

        for (acc in accounts) {
            val cv = ContentValues().apply {
                put("name", acc.name)
                put("type", acc.type.name)
                put("initial_balance", acc.initialBalance)
                put("color_hex", acc.colorHex)
                put("icon_name", acc.iconName)
            }
            db.insert(TABLE_ACCOUNTS, null, cv)
        }
    }

    private fun seedPreferences(db: SQLiteDatabase) {
        val prefs = mapOf(
            "currency_code" to "INR",
            "currency_symbol" to "₹",
            "theme_mode" to "AEGEAN_DARK",
            "calm_mode" to "0",
            "biometric_enabled" to "0",
            "onboarding_completed" to "1"
        )
        for ((k, v) in prefs) {
            val cv = ContentValues().apply {
                put("key", k)
                put("value", v)
            }
            db.insert(TABLE_PREFERENCES, null, cv)
        }

        // Add default overall monthly budget of ₹30,000
        val budgetCv = ContentValues().apply {
            putNull("category_id")
            put("amount", 30000.0)
            put("period", BudgetPeriod.MONTHLY.name)
            put("month_year", "ALL")
        }
        db.insert(TABLE_BUDGETS, null, budgetCv)
    }
}
