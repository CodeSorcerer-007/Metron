package com.metron.app

import com.metron.app.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetronCoreTest {

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
