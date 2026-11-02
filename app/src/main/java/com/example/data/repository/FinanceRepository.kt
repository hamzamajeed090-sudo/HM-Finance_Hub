package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

class FinanceRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    val settings = SettingsManager(context)

    val accounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    val loans: Flow<List<LoanEntity>> = db.loanDao().getAllLoans()
    val repayments: Flow<List<LoanRepaymentEntity>> = db.loanRepaymentDao().getAllRepayments()

    // Calculated Accounts with real-time balance derived from initial balance + income - expense + transfers
    val accountsWithBalance: Flow<List<AccountWithBalance>> = combine(accounts, transactions) { accList, txList ->
        accList.map { acc ->
            var bal = acc.initialBalance
            txList.forEach { tx ->
                when (tx.type) {
                    "INCOME" -> {
                        if (tx.accountId == acc.id) bal += tx.amount
                    }
                    "EXPENSE" -> {
                        if (tx.accountId == acc.id) bal -= tx.amount
                    }
                    "TRANSFER" -> {
                        if (tx.accountId == acc.id) bal -= tx.amount
                        if (tx.destAccountId == acc.id) {
                            // Account for exchange rate if cross currency
                            val receivedAmount = if (tx.exchangeRate > 0 && tx.currency != acc.currency) {
                                if (tx.currency == "SAR" && acc.currency == "PKR") tx.amount * tx.exchangeRate
                                else if (tx.currency == "PKR" && acc.currency == "SAR") tx.amount / tx.exchangeRate
                                else tx.amount
                            } else {
                                tx.amount
                            }
                            bal += receivedAmount
                        }
                    }
                }
            }
            AccountWithBalance(acc, bal)
        }
    }

    // Loans with full calculation of total repaid, remaining balance, and overdue check
    val loansWithDetails: Flow<List<LoanWithDetails>> = combine(loans, repayments) { loanList, repList ->
        val now = System.currentTimeMillis()
        loanList.map { loan ->
            val loanReps = repList.filter { it.loanId == loan.id }
            val repaid = loanReps.sumOf { it.amount }
            val remaining = (loan.principalAmount - repaid).coerceAtLeast(0.0)
            val isOverdue = !loan.isCompleted && remaining > 0 && loan.dueDateMillis < now
            LoanWithDetails(
                loan = loan,
                repayments = loanReps,
                totalRepaid = repaid,
                remainingBalance = remaining,
                isOverdue = isOverdue
            )
        }
    }

    // Month-over-month calculation for current month vs previous month
    fun calculateMonthOverMonth(
        txList: List<TransactionEntity>,
        currency: String,
        sarToPkrRate: Double
    ): MonthComparison {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) // 0-based

        cal.set(currentYear, currentMonth, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val curMonthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val curMonthEnd = cal.timeInMillis

        // Previous month range
        cal.set(currentYear, currentMonth, 1, 0, 0, 0)
        cal.add(Calendar.MONTH, -1)
        val prevMonthStart = cal.timeInMillis
        val prevMonthEnd = curMonthStart

        var curIncome = 0.0
        var curExpense = 0.0
        var prevIncome = 0.0
        var prevExpense = 0.0

        for (tx in txList) {
            val convertedAmount = CurrencyHelper.convert(tx.amount, tx.currency, currency, sarToPkrRate)
            if (tx.dateMillis in curMonthStart until curMonthEnd) {
                if (tx.type == "INCOME") curIncome += convertedAmount
                else if (tx.type == "EXPENSE") curExpense += convertedAmount
            } else if (tx.dateMillis in prevMonthStart until prevMonthEnd) {
                if (tx.type == "INCOME") prevIncome += convertedAmount
                else if (tx.type == "EXPENSE") prevExpense += convertedAmount
            }
        }

        val expenseDifference = curExpense - prevExpense
        val percentageChange = if (prevExpense > 0) {
            ((curExpense - prevExpense) / prevExpense) * 100.0
        } else 0.0

        return MonthComparison(
            currentMonthIncome = curIncome,
            currentMonthExpense = curExpense,
            previousMonthIncome = prevIncome,
            previousMonthExpense = prevExpense,
            expenseDifference = expenseDifference,
            percentageChange = percentageChange,
            isExpenseDecreased = expenseDifference < 0
        )
    }

    // Category Budget summaries for the active month
    fun calculateCategoryBudgets(
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        currency: String,
        sarToPkrRate: Double
    ): List<CategoryExpenseSummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val thisMonthExpenses = transactions.filter {
            it.type == "EXPENSE" && it.dateMillis >= startOfMonth
        }

        return categories.filter { !it.isIncome }.map { cat ->
            val spent = thisMonthExpenses
                .filter { it.categoryId == cat.id }
                .sumOf { CurrencyHelper.convert(it.amount, it.currency, currency, sarToPkrRate) }

            val rawLimit = if (currency == "SAR") cat.budgetLimitSar else cat.budgetLimitPkr
            val budgetLimit = if (rawLimit <= 0.0) {
                if (currency == "SAR") 300.0 else 20000.0 // Default reasonable limit
            } else rawLimit

            val pct = if (budgetLimit > 0) (spent / budgetLimit).toFloat() else 0f
            CategoryExpenseSummary(
                category = cat,
                totalSpent = spent,
                budgetLimit = budgetLimit,
                percentageUsed = pct
            )
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return db.transactionDao().insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        db.transactionDao().deleteTransaction(transaction)
    }

    suspend fun addAccount(account: AccountEntity): Long {
        return db.accountDao().insertAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        db.accountDao().deleteAccount(account)
    }

    suspend fun addLoan(loan: LoanEntity): Long {
        return db.loanDao().insertLoan(loan)
    }

    suspend fun updateLoan(loan: LoanEntity) {
        db.loanDao().updateLoan(loan)
    }

    suspend fun deleteLoan(loan: LoanEntity) {
        db.loanDao().deleteLoan(loan)
    }

    suspend fun addRepayment(repayment: LoanRepaymentEntity): Long {
        return db.loanRepaymentDao().insertRepayment(repayment)
    }

    suspend fun reloadHistoricalArchive() {
        AppDatabase.seedHistoricalArchive(db)
    }

    suspend fun updateCategoryBudget(categoryId: Long, limitPkr: Double, limitSar: Double) {
        val cat = db.categoryDao().getAllCategories().firstOrNull()?.find { it.id == categoryId }
        if (cat != null) {
            db.categoryDao().updateCategory(cat.copy(budgetLimitPkr = limitPkr, budgetLimitSar = limitSar))
        }
    }
}

data class MonthComparison(
    val currentMonthIncome: Double,
    val currentMonthExpense: Double,
    val previousMonthIncome: Double,
    val previousMonthExpense: Double,
    val expenseDifference: Double,
    val percentageChange: Double,
    val isExpenseDecreased: Boolean
)
