package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.ExcelReportGenerator
import com.example.data.export.HistoricalDataParser
import com.example.data.export.PdfReportGenerator
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.data.repository.MonthComparison
import com.example.notification.AppNotificationHelper
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class DateFilterRange {
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    ALL_TIME
}

data class FilterCriteria(
    val query: String = "",
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val type: String? = null,
    val range: DateFilterRange = DateFilterRange.THIS_MONTH
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    val repository = FinanceRepository(application)

    // Persistent Settings
    val displayCurrency = repository.settings.displayCurrency
    val sarToPkrRate = repository.settings.sarToPkrRate
    val appLanguage = repository.settings.appLanguage
    val isPinLockEnabled = repository.settings.isPinLockEnabled
    val isBiometricEnabled = repository.settings.isBiometricEnabled
    val hijriOffsetDays = repository.settings.hijriOffsetDays

    // Core Data Streams
    val accounts = repository.accountsWithBalance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rawAccounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loansWithDetails = repository.loansWithDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Search & Filters
    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria.asStateFlow()

    val searchQuery = _filterCriteria.map { it.query }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val selectedAccountFilter = _filterCriteria.map { it.accountId }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val selectedCategoryFilter = _filterCriteria.map { it.categoryId }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val selectedTypeFilter = _filterCriteria.map { it.type }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val selectedDateRange = _filterCriteria.map { it.range }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DateFilterRange.THIS_MONTH)

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _filterCriteria
    ) { txList: List<TransactionEntity>, filter: FilterCriteria ->
        val cal = Calendar.getInstance()

        val rangeStartMillis: Long = when (filter.range) {
            DateFilterRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            DateFilterRange.LAST_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, -1)
                cal.timeInMillis
            }
            DateFilterRange.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                cal.timeInMillis
            }
            DateFilterRange.ALL_TIME -> 0L
        }

        val rangeEndMillis: Long = when (filter.range) {
            DateFilterRange.LAST_MONTH -> {
                val c = Calendar.getInstance()
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.timeInMillis
            }
            else -> Long.MAX_VALUE
        }

        txList.filter { tx ->
            val matchesQuery = filter.query.isEmpty() || tx.notes.contains(filter.query, ignoreCase = true)
            val matchesAccount = filter.accountId == null || tx.accountId == filter.accountId || tx.destAccountId == filter.accountId
            val matchesCategory = filter.categoryId == null || tx.categoryId == filter.categoryId
            val matchesType = filter.type == null || tx.type == filter.type
            val matchesDate = tx.dateMillis in rangeStartMillis..rangeEndMillis
            matchesQuery && matchesAccount && matchesCategory && matchesType && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month-over-Month comparison state
    val monthComparison: StateFlow<MonthComparison> = combine(transactions, displayCurrency, sarToPkrRate) { txList, curr, rate ->
        repository.calculateMonthOverMonth(txList, curr, rate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthComparison(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true))

    // Category Budgets
    val categoryBudgets: StateFlow<List<CategoryExpenseSummary>> = combine(categories, transactions, displayCurrency, sarToPkrRate) { cats, txs, curr, rate ->
        repository.calculateCategoryBudgets(cats, txs, curr, rate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Startup & Security State
    val isAppUnlocked = MutableStateFlow(false)
    val showSplashScreen = MutableStateFlow(true)

    // Dhikr Counter State (bonus feature requested / honoring Hamza Majeed)
    val dhikrCount = MutableStateFlow(0)
    val dhikrTarget = MutableStateFlow(33)
    val dhikrPhrase = MutableStateFlow("SubhanAllah (سبحان الله)")

    init {
        // Check for upcoming loan due dates and budget alerts
        viewModelScope.launch {
            loansWithDetails.collect { list ->
                val urgentLoans = list.filter { it.isOverdue || (!it.loan.isCompleted && it.loan.dueDateMillis - System.currentTimeMillis() in 0..(3L * 86400000L)) }
                urgentLoans.forEach {
                    if (it.isOverdue) {
                        AppNotificationHelper.showNotification(
                            getApplication(),
                            it.loan.id.toInt(),
                            "Loan Payment Overdue: ${it.loan.personName}",
                            "Remaining balance: ${it.loan.currency} ${it.remainingBalance.toInt()} was due!"
                        )
                    }
                }
            }
        }
    }

    fun setDisplayCurrency(currency: String) = repository.settings.setDisplayCurrency(currency)
    fun setSarToPkrRate(rate: Double) = repository.settings.setSarToPkrRate(rate)
    fun setAppLanguage(language: AppLanguage) = repository.settings.setAppLanguage(language)
    fun setPinLockEnabled(enabled: Boolean) = repository.settings.setPinLockEnabled(enabled)
    fun setBiometricEnabled(enabled: Boolean) = repository.settings.setBiometricEnabled(enabled)
    fun getSavedPin(): String = repository.settings.getPin()
    fun setSavedPin(pin: String) = repository.settings.setPin(pin)
    fun setHijriOffset(days: Int) = repository.settings.setHijriOffset(days)

    fun setSearchQuery(query: String) { _filterCriteria.update { it.copy(query = query) } }
    fun setAccountFilter(accId: Long?) { _filterCriteria.update { it.copy(accountId = accId) } }
    fun setCategoryFilter(catId: Long?) { _filterCriteria.update { it.copy(categoryId = catId) } }
    fun setTypeFilter(type: String?) { _filterCriteria.update { it.copy(type = type) } }
    fun setDateRange(range: DateFilterRange) { _filterCriteria.update { it.copy(range = range) } }

    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(tx)
            checkBudgetAlerts(tx)
        }
    }

    private suspend fun checkBudgetAlerts(tx: TransactionEntity) {
        if (tx.type == "EXPENSE") {
            val budgets = repository.calculateCategoryBudgets(
                categories.value,
                transactions.value,
                displayCurrency.value,
                sarToPkrRate.value
            )
            val catBudget = budgets.find { it.category.id == tx.categoryId }
            if (catBudget != null) {
                if (catBudget.percentageUsed >= 1.0f) {
                    AppNotificationHelper.showNotification(
                        getApplication(),
                        (2000 + tx.categoryId).toInt(),
                        "Budget Exceeded: ${catBudget.category.nameEn}",
                        "You have spent 100% of your allocated budget!"
                    )
                } else if (catBudget.percentageUsed >= 0.8f) {
                    AppNotificationHelper.showNotification(
                        getApplication(),
                        (1000 + tx.categoryId).toInt(),
                        "Budget Warning: ${catBudget.category.nameEn}",
                        "You have reached 80% of your monthly budget limit."
                    )
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(tx) }
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch { repository.addAccount(account) }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { repository.deleteAccount(account) }
    }

    fun addLoan(loan: LoanEntity) {
        viewModelScope.launch { repository.addLoan(loan) }
    }

    fun updateLoan(loan: LoanEntity) {
        viewModelScope.launch { repository.updateLoan(loan) }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch { repository.deleteLoan(loan) }
    }

    fun addRepayment(repayment: LoanRepaymentEntity) {
        viewModelScope.launch { repository.addRepayment(repayment) }
    }

    fun reloadHistoricalData() {
        viewModelScope.launch { repository.reloadHistoricalArchive() }
    }

    fun importCsvData(csvText: String) {
        viewModelScope.launch {
            val parsed = HistoricalDataParser.parseCsvToTransactions(
                csvText,
                categories.value,
                rawAccounts.value
            )
            for (tx in parsed) {
                repository.addTransaction(tx)
            }
        }
    }

    fun exportExcelReport(): File? {
        return ExcelReportGenerator.generateAndShareExcelReport(
            getApplication(),
            transactions.value,
            categories.value,
            rawAccounts.value,
            loansWithDetails.value,
            displayCurrency.value
        )
    }

    fun exportPdfReport(): File? {
        return PdfReportGenerator.generateAndSharePdfReport(
            getApplication(),
            transactions.value,
            categories.value,
            rawAccounts.value,
            loansWithDetails.value,
            displayCurrency.value
        )
    }

    fun updateCategoryBudget(categoryId: Long, pkr: Double, sar: Double) {
        viewModelScope.launch {
            repository.updateCategoryBudget(categoryId, pkr, sar)
        }
    }

    fun incrementDhikr() {
        dhikrCount.value += 1
    }

    fun resetDhikr() {
        dhikrCount.value = 0
    }
}
