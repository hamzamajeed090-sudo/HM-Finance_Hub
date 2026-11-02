package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

enum class AppCurrency(val code: String, val symbol: String, val displayName: String) {
    PKR("PKR", "₨", "PKR (Pakistani Rupee)"),
    SAR("SAR", "﷼", "SAR (Saudi Riyal)")
}

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class LoanDirection {
    GIVEN, // Lending - money I gave to someone (owed to me)
    TAKEN  // Borrowing - money I took from someone (I owe)
}

enum class AccountType {
    BANK,
    WALLET,
    CASH,
    SAVINGS
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // BANK, WALLET, CASH, SAVINGS
    val currency: String, // PKR, SAR
    val initialBalance: Double = 0.0,
    val colorHex: String = "#0F766E",
    val accountNumber: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val amount: Double,
    val currency: String, // PKR, SAR
    val categoryId: Long,
    val accountId: Long, // Source account
    val destAccountId: Long? = null, // Destination account if TRANSFER
    val exchangeRate: Double = 1.0, // If cross-currency transfer or conversion
    val notes: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameUr: String,
    val iconName: String,
    val isIncome: Boolean,
    val colorHex: String,
    val budgetLimitPkr: Double = 0.0,
    val budgetLimitSar: Double = 0.0
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val phone: String = "",
    val direction: String, // GIVEN, TAKEN
    val principalAmount: Double,
    val currency: String, // PKR, SAR
    val startDateMillis: Long,
    val dueDateMillis: Long,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "loan_repayments")
data class LoanRepaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val dateMillis: Long,
    val amount: Double,
    val notes: String = "",
    val accountId: Long? = null
)

// UI Aggregate / Helper models
data class LoanWithDetails(
    val loan: LoanEntity,
    val repayments: List<LoanRepaymentEntity>,
    val totalRepaid: Double,
    val remainingBalance: Double,
    val isOverdue: Boolean
)

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double
)

data class CategoryExpenseSummary(
    val category: CategoryEntity,
    val totalSpent: Double,
    val budgetLimit: Double,
    val percentageUsed: Float
)

object CurrencyHelper {
    fun formatAmount(amount: Double, currency: String): String {
        val symbol = if (currency == "SAR") "﷼ " else "₨ "
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        return "$symbol${formatter.format(amount)}"
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String, sarToPkrRate: Double): Double {
        if (fromCurrency == toCurrency) return amount
        return if (fromCurrency == "SAR" && toCurrency == "PKR") {
            amount * sarToPkrRate
        } else if (fromCurrency == "PKR" && toCurrency == "SAR") {
            if (sarToPkrRate > 0) amount / sarToPkrRate else 0.0
        } else {
            amount
        }
    }
}
