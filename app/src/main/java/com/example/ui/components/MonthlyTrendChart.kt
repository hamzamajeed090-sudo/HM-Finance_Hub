package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.*

data class MonthTrendData(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

@Composable
fun MonthlyTrendChart(
    transactions: List<TransactionEntity>,
    currency: String,
    sarToPkrRate: Double,
    modifier: Modifier = Modifier
) {
    // Group transactions by month for the last 5 months
    val monthFormat = SimpleDateFormat("MMM", Locale.US)
    val monthsData = mutableListOf<MonthTrendData>()

    for (i in 4 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -i)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)

        c.set(year, month, 1, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        val startMillis = c.timeInMillis

        c.add(Calendar.MONTH, 1)
        val endMillis = c.timeInMillis

        var inc = 0.0
        var exp = 0.0

        for (tx in transactions) {
            if (tx.dateMillis in startMillis until endMillis) {
                val amt = CurrencyHelper.convert(tx.amount, tx.currency, currency, sarToPkrRate)
                if (tx.type == "INCOME") inc += amt
                else if (tx.type == "EXPENSE") exp += amt
            }
        }
        val label = monthFormat.format(Date(startMillis))
        monthsData.add(MonthTrendData(label, inc, exp))
    }

    val maxVal = monthsData.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1000.0) ?: 1000.0

    Column(modifier = modifier) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(IncomeGreen, MaterialTheme.shapes.extraSmall))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier.size(10.dp).background(ExpenseRed, MaterialTheme.shapes.extraSmall))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Expense", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 24.dp.toPx()
            val numMonths = monthsData.size
            val slotWidth = chartWidth / numMonths
            val barWidth = 14.dp.toPx()

            monthsData.forEachIndexed { index, data ->
                val centerX = index * slotWidth + (slotWidth / 2f)

                // Income Bar (Green)
                val incomeBarHeight = ((data.income / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
                val incomeX = centerX - barWidth - 2.dp.toPx()
                val incomeY = chartHeight - incomeBarHeight

                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(incomeX, incomeY),
                    size = Size(barWidth, incomeBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Expense Bar (Red)
                val expenseBarHeight = ((data.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
                val expenseX = centerX + 2.dp.toPx()
                val expenseY = chartHeight - expenseBarHeight

                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(expenseX, expenseY),
                    size = Size(barWidth, expenseBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Baseline
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            monthsData.forEach { data ->
                Text(
                    text = data.monthLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
