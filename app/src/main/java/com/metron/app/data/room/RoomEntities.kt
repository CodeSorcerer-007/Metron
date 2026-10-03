package com.metron.app.data.room

import androidx.room.*
import com.metron.app.model.AccountType
import com.metron.app.model.CategoryType
import com.metron.app.model.RecurringFrequency
import com.metron.app.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index("timestamp"),
        Index("category_id"),
        Index("account_id"),
        Index("type")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "to_account_id") val toAccountId: Long? = null,
    val timestamp: Long,
    val merchant: String,
    val notes: String = "",
    val tag: String = "",
    val currency: String = "INR",
    @ColumnInfo(name = "receipt_path") val receiptPath: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "icon_name") val iconName: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    val type: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "display_order") val displayOrder: Int = 0
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    @ColumnInfo(name = "initial_balance") val initialBalance: Double = 0.0,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "icon_name") val iconName: String
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    val amount: Double,
    val period: String,
    @ColumnInfo(name = "month_year") val monthYear: String = "ALL"
)

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val type: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    val frequency: String,
    @ColumnInfo(name = "next_due_date") val nextDueDate: Long,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)

object RoomMappers {
    fun toDomain(entity: TransactionEntity): com.metron.app.model.Transaction =
        com.metron.app.model.Transaction(
            id = entity.id,
            amount = entity.amount,
            type = com.metron.app.model.TransactionType.valueOf(entity.type),
            categoryId = entity.categoryId,
            accountId = entity.accountId,
            toAccountId = entity.toAccountId,
            timestamp = entity.timestamp,
            merchant = entity.merchant,
            notes = entity.notes,
            tag = entity.tag,
            currency = entity.currency,
            receiptPath = entity.receiptPath
        )

    fun toEntity(domain: com.metron.app.model.Transaction): TransactionEntity =
        TransactionEntity(
            id = domain.id,
            amount = domain.amount,
            type = domain.type.name,
            categoryId = domain.categoryId,
            accountId = domain.accountId,
            toAccountId = domain.toAccountId,
            timestamp = domain.timestamp,
            merchant = domain.merchant,
            notes = domain.notes,
            tag = domain.tag,
            currency = domain.currency,
            receiptPath = domain.receiptPath
        )
}

