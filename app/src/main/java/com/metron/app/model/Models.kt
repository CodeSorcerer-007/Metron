package com.metron.app.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val merchant: String = "",
    val notes: String = "",
    val tag: String = "",
    val currency: String = "INR",
    val receiptPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String = "Category",
    val colorHex: String = "#E5C07B",
    val type: CategoryType = CategoryType.EXPENSE,
    val isDefault: Boolean = false,
    val displayOrder: Int = 0
)

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType = AccountType.CASH,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val colorHex: String = "#38BDF8",
    val iconName: String = "AccountBalance"
)

data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null, // null = overall monthly budget
    val amount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val monthYear: String = "ALL" // "2026-10" or "ALL"
)

data class RecurringItem(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long,
    val accountId: Long,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

data class SpendingInsight(
    val title: String,
    val description: String,
    val iconName: String,
    val isHighlight: Boolean = false
)
