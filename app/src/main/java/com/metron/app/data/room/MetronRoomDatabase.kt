package com.metron.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.metron.app.model.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        RecurringEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MetronRoomDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        const val DATABASE_NAME = "metron_room.db"

        @Volatile
        private var INSTANCE: MetronRoomDatabase? = null

        fun getDatabase(context: Context): MetronRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetronRoomDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ── Model to Entity Conversion Extensions ──────────────────────────────────────────

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    amount = this.amount,
    type = this.type.name,
    categoryId = this.categoryId,
    accountId = this.accountId,
    toAccountId = this.toAccountId,
    timestamp = this.timestamp,
    merchant = this.merchant,
    notes = this.notes,
    tag = this.tag,
    currency = this.currency,
    receiptPath = this.receiptPath,
    createdAt = this.createdAt
)

fun TransactionEntity.toModel(): Transaction = Transaction(
    id = this.id,
    amount = this.amount,
    type = try { TransactionType.valueOf(this.type) } catch (e: Exception) { TransactionType.EXPENSE },
    categoryId = this.categoryId,
    accountId = this.accountId,
    toAccountId = this.toAccountId,
    timestamp = this.timestamp,
    merchant = this.merchant,
    notes = this.notes,
    tag = this.tag,
    currency = this.currency,
    receiptPath = this.receiptPath,
    createdAt = this.createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = this.id,
    name = this.name,
    iconName = this.iconName,
    colorHex = this.colorHex,
    type = this.type.name,
    isDefault = this.isDefault,
    displayOrder = this.displayOrder
)

fun CategoryEntity.toModel(): Category = Category(
    id = this.id,
    name = this.name,
    iconName = this.iconName,
    colorHex = this.colorHex,
    type = try { CategoryType.valueOf(this.type) } catch (e: Exception) { CategoryType.EXPENSE },
    isDefault = this.isDefault,
    displayOrder = this.displayOrder
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = this.id,
    name = this.name,
    type = this.type.name,
    initialBalance = this.initialBalance,
    colorHex = this.colorHex,
    iconName = this.iconName
)

fun AccountEntity.toModel(): Account = Account(
    id = this.id,
    name = this.name,
    type = try { AccountType.valueOf(this.type) } catch (e: Exception) { AccountType.CASH },
    initialBalance = this.initialBalance,
    colorHex = this.colorHex,
    iconName = this.iconName
)
