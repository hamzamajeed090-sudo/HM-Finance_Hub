package com.example.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelReportGenerator {

    fun generateAndShareExcelReport(
        context: Context,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
        loans: List<LoanWithDetails>,
        currency: String
    ): File? {
        try {
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US)
            val fileName = "FinTrack_Report_${dateFormat.format(Date())}.csv"
            val file = File(cacheDir, fileName)

            val catMap = categories.associateBy { it.id }
            val accMap = accounts.associateBy { it.id }
            val displayDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            val fos = FileOutputStream(file)
            // UTF-8 BOM to ensure Excel opens with correct character encoding for English, Urdu & Arabic symbols
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val writer = fos.bufferedWriter(Charsets.UTF_8)

            // Header Section
            writer.write("FinTrack Pro - Financial Ledger & Loan Report\n")
            writer.write("Generated on,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
            writer.write("Active Currency View,$currency\n\n")

            // Transactions Section
            writer.write("--- TRANSACTIONS LEDGER ---\n")
            writer.write("Transaction ID,Date,Type,Amount,Currency,Category,Source Account,Destination Account,Exchange Rate,Notes\n")

            for (tx in transactions) {
                val catName = catMap[tx.categoryId]?.nameEn ?: "General"
                val sourceAcc = accMap[tx.accountId]?.name ?: "Account #${tx.accountId}"
                val destAcc = if (tx.destAccountId != null) accMap[tx.destAccountId]?.name ?: "Account #${tx.destAccountId}" else "N/A"
                val cleanNotes = tx.notes.replace("\"", "\"\"").replace("\n", " ")

                writer.write("${tx.id},${displayDateFormat.format(Date(tx.dateMillis))},${tx.type},${tx.amount},${tx.currency},\"$catName\",\"$sourceAcc\",\"$destAcc\",${tx.exchangeRate},\"$cleanNotes\"\n")
            }

            writer.write("\n--- LOANS & DEBTS LEDGER ---\n")
            writer.write("Loan ID,Person Name,Direction,Principal Amount,Currency,Total Repaid,Remaining Balance,Due Date,Status,Notes\n")

            for (item in loans) {
                val l = item.loan
                val directionText = if (l.direction == "GIVEN") "Lending (I am Owed)" else "Borrowing (I Owe)"
                val statusText = if (item.remainingBalance <= 0) "Fully Repaid" else if (item.isOverdue) "OVERDUE" else "Active"
                val cleanNotes = l.notes.replace("\"", "\"\"").replace("\n", " ")

                writer.write("${l.id},\"${l.personName}\",\"$directionText\",${l.principalAmount},${l.currency},${item.totalRepaid},${item.remainingBalance},${displayDateFormat.format(Date(l.dueDateMillis))},\"$statusText\",\"$cleanNotes\"\n")
            }

            writer.flush()
            writer.close()
            fos.close()

            // Trigger Share Intent
            shareFile(context, file, "text/csv", "Share Financial Excel/CSV Report")
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
