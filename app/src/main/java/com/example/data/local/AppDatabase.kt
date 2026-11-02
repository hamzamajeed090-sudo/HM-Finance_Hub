package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        LoanEntity::class,
        LoanRepaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun loanDao(): LoanDao
    abstract fun loanRepaymentDao(): LoanRepaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintrack_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            // Seed Categories
            val categories = listOf(
                CategoryEntity(1, "Food & Dining", "کھانا پینا", "restaurant", false, "#EF4444", 25000.0, 400.0),
                CategoryEntity(2, "Groceries", "راشن و گروسری", "shopping_cart", false, "#F97316", 35000.0, 500.0),
                CategoryEntity(3, "Utilities & Bills", "بلز اور یوٹیلیٹیز", "bolt", false, "#EAB308", 20000.0, 300.0),
                CategoryEntity(4, "Rent & Housing", "کرایہ اور رہائش", "home", false, "#8B5CF6", 45000.0, 1200.0),
                CategoryEntity(5, "Transport & Fuel", "ٹرانسپورٹ اور پیٹرول", "directions_car", false, "#3B82F6", 18000.0, 250.0),
                CategoryEntity(6, "Health & Medical", "صحت و ادویات", "medical_services", false, "#EC4899", 10000.0, 150.0),
                CategoryEntity(7, "Remittance & Family", "گھر کی رقم و ترسیلات", "send", false, "#06B6D4", 50000.0, 2000.0),
                CategoryEntity(8, "Charity & Zakat", "زکوٰۃ و صدقات", "volunteer_activism", false, "#10B981", 15000.0, 200.0),
                CategoryEntity(9, "Shopping & Clothes", "خریداری اور لباس", "checkroom", false, "#6366F1", 12000.0, 200.0),
                CategoryEntity(10, "Other Expense", "دیگر اخراجات", "more_horiz", false, "#64748B", 10000.0, 150.0),
                // Income Categories
                CategoryEntity(11, "Salary / Wages", "تنخواہ و مشاہرہ", "work", true, "#10B981", 0.0, 0.0),
                CategoryEntity(12, "Business & Sales", "کاروبار و منافع", "storefront", true, "#059669", 0.0, 0.0),
                CategoryEntity(13, "Freelance & Remittance", "فری لانس و بیرون ملک ترسیل", "payments", true, "#047857", 0.0, 0.0),
                CategoryEntity(14, "Rental Income", "کرایہ کی آمدن", "apartment", true, "#065F46", 0.0, 0.0),
                CategoryEntity(15, "Other Income", "دیگر آمدنی", "add_card", true, "#0D9488", 0.0, 0.0)
            )
            database.categoryDao().insertCategories(categories)

            // Seed Multi-Accounts (ABL, UPaisa, Cash, Bank Accounts in PKR & SAR)
            val accounts = listOf(
                AccountEntity(1, "ABL (Allied Bank)", "BANK", "PKR", 185000.0, "#0F766E", "PK64ABPA001234567890"),
                AccountEntity(2, "UPaisa Wallet", "WALLET", "PKR", 24500.0, "#D97706", "03001234567"),
                AccountEntity(3, "Cash in Hand (PKR)", "CASH", "PKR", 15000.0, "#16A34A", "Physical Cash"),
                AccountEntity(4, "Al Rajhi Bank", "BANK", "SAR", 4200.0, "#2563EB", "SA0380000000608010167519"),
                AccountEntity(5, "Cash in Hand (SAR)", "CASH", "SAR", 850.0, "#059669", "SAR Cash")
            )
            database.accountDao().insertAccounts(accounts)

            // Seed Historical Data Archive (July 2026 to October 2026 + current records)
            seedHistoricalArchive(database)
        }

        suspend fun seedHistoricalArchive(database: AppDatabase) {
            val transactions = mutableListOf<TransactionEntity>()

            // Helper to get millis for year, month (0-indexed), day
            fun date(year: Int, month: Int, day: Int): Long {
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 12, 0, 0)
                return cal.timeInMillis
            }

            // July 2026 Records (PKR & SAR)
            transactions.add(TransactionEntity(0, date(2026, 6, 1), "INCOME", 160000.0, "PKR", 11, 1, null, 1.0, "July Salary (ABL)"))
            transactions.add(TransactionEntity(0, date(2026, 6, 2), "INCOME", 4500.0, "SAR", 11, 4, null, 1.0, "July SAR Professional Earnings"))
            transactions.add(TransactionEntity(0, date(2026, 6, 5), "EXPENSE", 42000.0, "PKR", 4, 1, null, 1.0, "July House Rent"))
            transactions.add(TransactionEntity(0, date(2026, 6, 10), "EXPENSE", 18500.0, "PKR", 2, 2, null, 1.0, "Monthly Grocery (UPaisa)"))
            transactions.add(TransactionEntity(0, date(2026, 6, 15), "EXPENSE", 14500.0, "PKR", 3, 1, null, 1.0, "Electricity & Gas Bills"))
            transactions.add(TransactionEntity(0, date(2026, 6, 20), "EXPENSE", 850.0, "SAR", 7, 4, null, 74.0, "Family Remittance to Pakistan"))

            // August 2026 Records
            transactions.add(TransactionEntity(0, date(2026, 7, 1), "INCOME", 165000.0, "PKR", 11, 1, null, 1.0, "August Salary (ABL)"))
            transactions.add(TransactionEntity(0, date(2026, 7, 3), "INCOME", 4500.0, "SAR", 11, 4, null, 1.0, "August SAR Earnings"))
            transactions.add(TransactionEntity(0, date(2026, 7, 5), "EXPENSE", 42000.0, "PKR", 4, 1, null, 1.0, "August House Rent"))
            transactions.add(TransactionEntity(0, date(2026, 7, 12), "EXPENSE", 22000.0, "PKR", 2, 2, null, 1.0, "Grocery & Supplies (UPaisa)"))
            transactions.add(TransactionEntity(0, date(2026, 7, 18), "EXPENSE", 16200.0, "PKR", 3, 1, null, 1.0, "Electric & Internet Bills"))
            transactions.add(TransactionEntity(0, date(2026, 7, 24), "EXPENSE", 950.0, "SAR", 7, 4, null, 74.2, "August Family Support"))

            // September 2026 Records
            transactions.add(TransactionEntity(0, date(2026, 8, 1), "INCOME", 170000.0, "PKR", 11, 1, null, 1.0, "September Salary"))
            transactions.add(TransactionEntity(0, date(2026, 8, 3), "INCOME", 5000.0, "SAR", 11, 4, null, 1.0, "September Consulting Fee"))
            transactions.add(TransactionEntity(0, date(2026, 8, 5), "EXPENSE", 42000.0, "PKR", 4, 1, null, 1.0, "September House Rent"))
            transactions.add(TransactionEntity(0, date(2026, 8, 11), "EXPENSE", 19500.0, "PKR", 2, 2, null, 1.0, "Grocery (UPaisa)"))
            transactions.add(TransactionEntity(0, date(2026, 8, 16), "EXPENSE", 12400.0, "PKR", 5, 3, null, 1.0, "Vehicle Fuel & Maintenance"))
            transactions.add(TransactionEntity(0, date(2026, 8, 22), "EXPENSE", 1000.0, "SAR", 7, 4, null, 74.2, "Remittance via Al Rajhi"))

            // October 2026 Records (Month prior to current November 2026)
            transactions.add(TransactionEntity(0, date(2026, 9, 1), "INCOME", 175000.0, "PKR", 11, 1, null, 1.0, "October Salary"))
            transactions.add(TransactionEntity(0, date(2026, 9, 2), "INCOME", 4800.0, "SAR", 11, 4, null, 1.0, "October SAR Income"))
            transactions.add(TransactionEntity(0, date(2026, 9, 5), "EXPENSE", 42000.0, "PKR", 4, 1, null, 1.0, "October House Rent"))
            transactions.add(TransactionEntity(0, date(2026, 9, 10), "EXPENSE", 24000.0, "PKR", 2, 2, null, 1.0, "Grocery and Home Essentials"))
            transactions.add(TransactionEntity(0, date(2026, 9, 14), "EXPENSE", 15800.0, "PKR", 3, 1, null, 1.0, "Electricity, Water & Internet Bills"))
            transactions.add(TransactionEntity(0, date(2026, 9, 20), "EXPENSE", 900.0, "SAR", 7, 4, null, 74.2, "October Remittance to Pakistan"))
            transactions.add(TransactionEntity(0, date(2026, 9, 25), "TRANSFER", 10000.0, "PKR", 0, 1, 2, 1.0, "Transfer ABL to UPaisa Wallet"))

            // November 2026 (Current Active Month)
            transactions.add(TransactionEntity(0, date(2026, 10, 1), "INCOME", 175000.0, "PKR", 11, 1, null, 1.0, "November Salary"))
            transactions.add(TransactionEntity(0, date(2026, 10, 1), "EXPENSE", 42000.0, "PKR", 4, 1, null, 1.0, "November House Rent"))
            transactions.add(TransactionEntity(0, date(2026, 10, 2), "EXPENSE", 8200.0, "PKR", 2, 2, null, 1.0, "Fresh Grocery & Fruits (UPaisa)"))

            database.transactionDao().insertTransactions(transactions)

            // Seed Historical Loans & Debt Management
            // 1. Loan Given to Ahmed Khan (Lending: Ahmed owes me 50,000 PKR, partially repaid)
            val loan1Id = database.loanDao().insertLoan(
                LoanEntity(
                    id = 1,
                    personName = "Ahmed Khan (Business Partner)",
                    phone = "+92 301 9876543",
                    direction = "GIVEN",
                    principalAmount = 50000.0,
                    currency = "PKR",
                    startDateMillis = date(2026, 6, 15),
                    dueDateMillis = date(2026, 10, 20), // Nov 20, 2026
                    notes = "Emergency inventory support loan",
                    isCompleted = false
                )
            )
            // Ahmed repayments
            database.loanRepaymentDao().insertRepayments(listOf(
                LoanRepaymentEntity(0, loan1Id, date(2026, 7, 20), 15000.0, "First installment via UPaisa", 2),
                LoanRepaymentEntity(0, loan1Id, date(2026, 8, 25), 15000.0, "Second installment in Cash", 3)
            ))

            // 2. Loan Taken from Tariq Mahmood (Borrowing: I borrowed 30,000 PKR from Tariq for car repair)
            val loan2Id = database.loanDao().insertLoan(
                LoanEntity(
                    id = 2,
                    personName = "Tariq Mahmood (Uncle)",
                    phone = "+92 321 4567890",
                    direction = "TAKEN",
                    principalAmount = 30000.0,
                    currency = "PKR",
                    startDateMillis = date(2026, 7, 10),
                    dueDateMillis = date(2026, 11, 15), // Dec 15, 2026
                    notes = "Borrowed for sudden vehicle maintenance",
                    isCompleted = false
                )
            )
            database.loanRepaymentDao().insertRepayments(listOf(
                LoanRepaymentEntity(0, loan2Id, date(2026, 8, 15), 10000.0, "Partial repayment from ABL", 1)
            ))

            // 3. SAR Loan: Saad Al-Ghamdi (Lending in Saudi Arabia: 1500 SAR given)
            database.loanDao().insertLoan(
                LoanEntity(
                    id = 3,
                    personName = "Saad Al-Ghamdi (Riyadh)",
                    phone = "+966 50 123 4567",
                    direction = "GIVEN",
                    principalAmount = 1500.0,
                    currency = "SAR",
                    startDateMillis = date(2026, 8, 1),
                    dueDateMillis = date(2026, 10, 30),
                    notes = "Short-term SAR loan",
                    isCompleted = false
                )
            )
        }
    }
}
