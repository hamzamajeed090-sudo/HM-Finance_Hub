package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdfReport(
        context: Context,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
        loans: List<LoanWithDetails>,
        currency: String
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val catMap = categories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Header Background
        paint.color = Color.parseColor("#0F766E")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FinTrack Pro - Financial Statement", 30f, 40f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val genDate = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated on: $genDate  |  Base Currency: $currency", 30f, 65f, paint)

        // Summary Cards
        var totalIncome = 0.0
        var totalExpense = 0.0
        transactions.forEach {
            if (it.type == "INCOME") totalIncome += it.amount
            if (it.type == "EXPENSE") totalExpense += it.amount
        }

        var y = 120f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Executive Summary", 30f, y, paint)

        y += 20f
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#16A34A")
        canvas.drawText("Total Recorded Income: $currency ${String.format(Locale.US, "%,.2f", totalIncome)}", 30f, y, paint)

        y += 18f
        paint.color = Color.parseColor("#DC2626")
        canvas.drawText("Total Recorded Expenses: $currency ${String.format(Locale.US, "%,.2f", totalExpense)}", 30f, y, paint)

        y += 18f
        val net = totalIncome - totalExpense
        paint.color = if (net >= 0) Color.parseColor("#0F766E") else Color.parseColor("#DC2626")
        canvas.drawText("Net Surplus/Deficit: $currency ${String.format(Locale.US, "%,.2f", net)}", 30f, y, paint)

        // Divider
        y += 25f
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, y, 565f, y, paint)

        // Transactions Table Header
        y += 25f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Recent Transactions", 30f, y, paint)

        y += 20f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, y - 14f, 565f, y + 6f, paint)

        paint.color = Color.DKGRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date", 35f, y, paint)
        canvas.drawText("Type", 100f, y, paint)
        canvas.drawText("Category", 160f, y, paint)
        canvas.drawText("Account", 260f, y, paint)
        canvas.drawText("Amount", 480f, y, paint)

        y += 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f

        val displayTx = transactions.take(15) // Fit on page
        for (tx in displayTx) {
            paint.color = Color.BLACK
            canvas.drawText(dateFormat.format(Date(tx.dateMillis)), 35f, y, paint)

            paint.color = if (tx.type == "INCOME") Color.parseColor("#16A34A")
            else if (tx.type == "EXPENSE") Color.parseColor("#DC2626")
            else Color.parseColor("#2563EB")
            canvas.drawText(tx.type, 100f, y, paint)

            paint.color = Color.DKGRAY
            val catName = catMap[tx.categoryId]?.nameEn ?: "General"
            canvas.drawText(if (catName.length > 18) catName.substring(0, 16) + ".." else catName, 160f, y, paint)

            val accName = accMap[tx.accountId]?.name ?: "Account"
            canvas.drawText(if (accName.length > 20) accName.substring(0, 18) + ".." else accName, 260f, y, paint)

            paint.color = if (tx.type == "INCOME") Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
            val amtStr = "${tx.currency} ${String.format(Locale.US, "%,.1f", tx.amount)}"
            canvas.drawText(amtStr, 480f, y, paint)

            y += 18f
        }

        // Loans & Debts Summary
        if (y < 700f && loans.isNotEmpty()) {
            y += 15f
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Active Loans & Debt Ledger", 30f, y, paint)

            y += 20f
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(30f, y - 14f, 565f, y + 6f, paint)

            paint.color = Color.DKGRAY
            paint.textSize = 10f
            canvas.drawText("Person", 35f, y, paint)
            canvas.drawText("Direction", 180f, y, paint)
            canvas.drawText("Principal", 280f, y, paint)
            canvas.drawText("Remaining", 380f, y, paint)
            canvas.drawText("Due Date", 480f, y, paint)

            y += 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f

            for (loan in loans.take(5)) {
                paint.color = Color.BLACK
                val person = loan.loan.personName
                canvas.drawText(if (person.length > 22) person.substring(0, 20) + ".." else person, 35f, y, paint)

                paint.color = if (loan.loan.direction == "GIVEN") Color.parseColor("#16A34A") else Color.parseColor("#D97706")
                canvas.drawText(if (loan.loan.direction == "GIVEN") "Lending" else "Borrowing", 180f, y, paint)

                paint.color = Color.DKGRAY
                canvas.drawText("${loan.loan.currency} ${loan.loan.principalAmount.toInt()}", 280f, y, paint)

                paint.color = if (loan.isOverdue) Color.RED else Color.BLACK
                canvas.drawText("${loan.loan.currency} ${loan.remainingBalance.toInt()}", 380f, y, paint)

                canvas.drawText(dateFormat.format(Date(loan.loan.dueDateMillis)), 480f, y, paint)
                y += 18f
            }
        }

        // Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Created by Hamza Majeed  |  Pray for My Parents  |  Confidential Financial Record", 120f, 820f, paint)

        document.finishPage(page)

        try {
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val fileName = "FinTrack_Statement_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.pdf"
            val file = File(cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            // Trigger Share Intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share PDF Statement").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}
