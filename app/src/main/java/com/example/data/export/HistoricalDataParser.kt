package com.example.data.export

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoricalDataParser {

    /**
     * Parses lines of CSV / tabular text:
     * Format supported: Date, Type, Amount, Currency, Category, Account, Notes
     * or standard spreadsheet export format.
     */
    fun parseCsvToTransactions(
        csvText: String,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>
    ): List<TransactionEntity> {
        val result = mutableListOf<TransactionEntity>()
        val lines = csvText.lines()
        val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US)
        )

        val defaultAcc = accounts.firstOrNull()?.id ?: 1L
        val defaultCat = categories.firstOrNull()?.id ?: 1L

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("Transaction") || trimmed.startsWith("Date") || trimmed.startsWith("#") || trimmed.startsWith("-")) {
                continue
            }

            // Split by comma, respecting quotes
            val tokens = splitCsvLine(trimmed)
            if (tokens.size >= 3) {
                // Determine which token is date, amount, type
                var dateMillis = System.currentTimeMillis()
                for (fmt in dateFormats) {
                    try {
                        val d = fmt.parse(tokens[0].trim())
                        if (d != null) {
                            dateMillis = d.time
                            break
                        }
                    } catch (_: Exception) {}
                }

                val typeRaw = tokens.getOrNull(1)?.trim()?.uppercase(Locale.US) ?: "EXPENSE"
                val type = if (typeRaw.contains("INC")) "INCOME" else if (typeRaw.contains("TRAN")) "TRANSFER" else "EXPENSE"

                val amountStr = tokens.getOrNull(2)?.replace(",", "")?.replace("$", "")?.replace("₨", "")?.replace("﷼", "")?.trim() ?: "0"
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                val currency = tokens.getOrNull(3)?.trim()?.uppercase(Locale.US)?.takeIf { it == "SAR" || it == "PKR" } ?: "PKR"
                val catName = tokens.getOrNull(4)?.trim() ?: ""
                val accName = tokens.getOrNull(5)?.trim() ?: ""
                val notes = tokens.getOrNull(6)?.trim() ?: (if (tokens.size > 7) tokens.subList(6, tokens.size).joinToString(" ") else "")

                // Find matching category
                val catId = categories.find { it.nameEn.equals(catName, ignoreCase = true) || it.nameUr.equals(catName, ignoreCase = true) }?.id ?: defaultCat
                // Find matching account
                val accId = accounts.find { it.name.contains(accName, ignoreCase = true) }?.id ?: defaultAcc

                if (amount > 0) {
                    result.add(
                        TransactionEntity(
                            id = 0,
                            dateMillis = dateMillis,
                            type = type,
                            amount = amount,
                            currency = currency,
                            categoryId = catId,
                            accountId = accId,
                            notes = notes
                        )
                    )
                }
            }
        }
        return result
    }

    private fun splitCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb = StringBuilder()
            } else {
                sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
