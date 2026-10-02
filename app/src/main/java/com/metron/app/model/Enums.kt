package com.metron.app.model

enum class TransactionType(val title: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}

enum class CategoryType {
    EXPENSE,
    INCOME,
    BOTH
}

enum class AccountType(val title: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    CREDIT_CARD("Credit Card"),
    UPI("UPI / Digital Wallet"),
    SAVINGS("Savings"),
    OTHER("Other")
}

enum class BudgetPeriod {
    MONTHLY,
    WEEKLY
}

enum class RecurringFrequency(val title: String, val daysInterval: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365)
}

enum class TimeFilter(val title: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

enum class SortOrder(val title: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}
