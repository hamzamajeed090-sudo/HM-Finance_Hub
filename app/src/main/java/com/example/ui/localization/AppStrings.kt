package com.example.ui.localization

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    UR("ur", "اردو")
}

object AppStrings {
    private val strings = mapOf(
        "app_title" to mapOf("en" to "FinTrack Pro", "ur" to "فِن ٹریک پرو"),
        "app_subtitle" to mapOf("en" to "Personal Finance & Loan Tracker", "ur" to "ذاتی مالیات اور قرض ٹریکر"),
        "dashboard" to mapOf("en" to "Dashboard", "ur" to "ڈیش بورڈ"),
        "transactions" to mapOf("en" to "Transactions", "ur" to "لین دین"),
        "accounts" to mapOf("en" to "Accounts", "ur" to "اکاؤنٹس"),
        "loans" to mapOf("en" to "Loans & Debts", "ur" to "قرضے اور واجبات"),
        "analytics" to mapOf("en" to "Analytics", "ur" to "تجزیات"),
        "budgets" to mapOf("en" to "Budgets", "ur" to "بجٹ"),
        "export" to mapOf("en" to "Export & Import", "ur" to "برآمد اور درآمد"),
        "settings" to mapOf("en" to "Settings", "ur" to "ترتیبات"),
        "total_balance" to mapOf("en" to "Total Net Worth", "ur" to "کل مالیاتی مالیت"),
        "income" to mapOf("en" to "Income", "ur" to "آمدنی"),
        "expense" to mapOf("en" to "Expense", "ur" to "اخراجات"),
        "transfer" to mapOf("en" to "Transfer", "ur" to "منتقلی"),
        "loans_given" to mapOf("en" to "Lending (I am Owed)", "ur" to "دیے گئے قرضے (واپس لینے ہیں)"),
        "loans_taken" to mapOf("en" to "Borrowing (I Owe)", "ur" to "لیے گئے قرضے (واپس کرنے ہیں)"),
        "remaining" to mapOf("en" to "Remaining", "ur" to "بقایا"),
        "due_date" to mapOf("en" to "Due Date", "ur" to "آخری تاریخ"),
        "overdue" to mapOf("en" to "Overdue", "ur" to "تاخیر شدہ"),
        "active" to mapOf("en" to "Active", "ur" to "جاری"),
        "repaid" to mapOf("en" to "Repaid", "ur" to "ادا شدہ"),
        "add_transaction" to mapOf("en" to "Add Transaction", "ur" to "نیا اندراج کریں"),
        "add_account" to mapOf("en" to "Add Account", "ur" to "نیا اکاؤنٹ شامل کریں"),
        "add_loan" to mapOf("en" to "Add Loan / Debt", "ur" to "نیا قرض شامل کریں"),
        "record_repayment" to mapOf("en" to "Record Repayment", "ur" to "ادائیگی درج کریں"),
        "exchange_rate" to mapOf("en" to "Exchange Rate (SAR to PKR)", "ur" to "شرح مبادلہ (ریال تا روپے)"),
        "current_currency" to mapOf("en" to "Display Currency", "ur" to "ظاہری کرنسی"),
        "mom_comparison" to mapOf("en" to "Month-over-Month Comparison", "ur" to "ماہانہ موازنہ"),
        "expense_decreased" to mapOf("en" to "Expenses decreased by %s vs last month!", "ur" to "گزشتہ ماہ کے مقابلے اخراجات میں %s کمی!"),
        "expense_increased" to mapOf("en" to "Expenses increased by %s vs last month", "ur" to "گزشتہ ماہ کے مقابلے اخراجات میں %s اضافہ"),
        "expense_same" to mapOf("en" to "Expenses steady with last month", "ur" to "اخراجات پچھلے ماہ کے برابر ہیں"),
        "budget_limit" to mapOf("en" to "Budget Limit", "ur" to "بجٹ کی حد"),
        "budget_warning" to mapOf("en" to "Warning: 80% budget reached!", "ur" to "انتباہ: بجٹ کا 80٪ خرچ ہو چکا ہے!"),
        "budget_exceeded" to mapOf("en" to "Alert: Budget exceeded!", "ur" to "انتباہ: بجٹ کی حد پار ہو گئی ہے!"),
        "pin_lock" to mapOf("en" to "PIN Security Lock", "ur" to "پن سیکیورٹی لاک"),
        "enter_pin" to mapOf("en" to "Enter 4-Digit Security PIN", "ur" to "۴ ہندسوں کا سیکیورٹی پن درج کریں"),
        "wrong_pin" to mapOf("en" to "Incorrect PIN, please try again", "ur" to "غلط پن، دوبارہ کوشش کریں"),
        "biometric_unlock" to mapOf("en" to "Biometric Authentication", "ur" to "بائیو میٹرک تصدیق"),
        "use_biometric" to mapOf("en" to "Unlock with Fingerprint", "ur" to "فنگر پرنٹ سے انلاک کریں"),
        "export_pdf" to mapOf("en" to "Export PDF Report", "ur" to "پی ڈی ایف رپورٹ بنائیں"),
        "export_excel" to mapOf("en" to "Export Excel / CSV", "ur" to "ایکسل رپورٹ برآمد کریں"),
        "import_historical" to mapOf("en" to "Load Historical Archive (Jul-Oct 2026)", "ur" to "تاریخی ڈیٹا درآمد کریں (جولائی-اکتوبر ۲۰۲۶)"),
        "historical_loaded" to mapOf("en" to "Historical records loaded successfully!", "ur" to "تاریخی ریکارڈ کامیابی کے ساتھ لوڈ ہو گیا!"),
        "tasbih_tool" to mapOf("en" to "Digital Dhikr Counter", "ur" to "ڈیجیٹل ذکر تسبیح"),
        "pray_parents" to mapOf("en" to "Pray for My Parents", "ur" to "والدین کی مغفرت کی دعا فرمائیں"),
        "created_by" to mapOf("en" to "Created by Hamza Majeed", "ur" to "حمزہ مجید کی تخلیق"),
        "search_hint" to mapOf("en" to "Search transactions, notes, people...", "ur" to "تلاش کریں: نوٹس، زمرہ، افراد..."),
        "all_accounts" to mapOf("en" to "All Accounts", "ur" to "تمام اکاؤنٹس"),
        "all_categories" to mapOf("en" to "All Categories", "ur" to "تمام زمرہ جات"),
        "date_range" to mapOf("en" to "Date Range", "ur" to "تاریخ کی حد"),
        "this_month" to mapOf("en" to "This Month", "ur" to "یہ مہینہ"),
        "last_month" to mapOf("en" to "Last Month", "ur" to "پچھلا مہینہ"),
        "all_time" to mapOf("en" to "All Time", "ur" to "ہمیشہ کا ریکارڈ"),
        "save" to mapOf("en" to "Save", "ur" to "محفوظ کریں"),
        "cancel" to mapOf("en" to "Cancel", "ur" to "منسوخ"),
        "amount" to mapOf("en" to "Amount", "ur" to "رقم"),
        "category" to mapOf("en" to "Category", "ur" to "زمرہ"),
        "account" to mapOf("en" to "Account", "ur" to "اکاؤنٹ"),
        "date" to mapOf("en" to "Date", "ur" to "تاریخ"),
        "notes" to mapOf("en" to "Notes", "ur" to "تفصیل / نوٹس"),
        "source_account" to mapOf("en" to "From Account", "ur" to "ارسال کنندہ اکاؤنٹ"),
        "dest_account" to mapOf("en" to "To Account", "ur" to "وصول کنندہ اکاؤنٹ"),
        "person_name" to mapOf("en" to "Person Name", "ur" to "شخص کا نام"),
        "phone" to mapOf("en" to "Phone (Optional)", "ur" to "فون نمبر")
    )

    fun get(key: String, lang: AppLanguage): String {
        return strings[key]?.get(lang.code) ?: strings[key]?.get("en") ?: key
    }
}
