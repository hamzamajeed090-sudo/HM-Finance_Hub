package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.CurrencyHelper

@Composable
fun CategoryDonutChart(
    categoriesSummary: List<CategoryExpenseSummary>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val nonZero = categoriesSummary.filter { it.totalSpent > 0 }
    val totalExpense = nonZero.sumOf { it.totalSpent }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 36.dp.toPx()
            if (totalExpense <= 0) {
                // Empty state ring
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                var currentAngle = -90f
                nonZero.forEach { item ->
                    val sweep = ((item.totalSpent / totalExpense) * 360f).toFloat()
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(item.category.colorHex))
                    } catch (_: Exception) {
                        Color(0xFF0F766E)
                    }

                    drawArc(
                        color = parsedColor,
                        startAngle = currentAngle,
                        sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    currentAngle += sweep
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Spent",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = CurrencyHelper.formatAmount(totalExpense, currency),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
