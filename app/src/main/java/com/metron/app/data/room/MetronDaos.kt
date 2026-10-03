package com.metron.app.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY display_order ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring ORDER BY next_due_date ASC")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(item: RecurringEntity): Long

    @Update
    suspend fun updateRecurring(item: RecurringEntity)

    @Query("DELETE FROM recurring WHERE id = :id")
    suspend fun deleteById(id: Long)
}
