package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyHelper
import com.example.data.repository.MonthComparison
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import kotlin.math.abs

@Composable
fun MonthOverMonthCard(
    comparison: MonthComparison,
    currency: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val pct = abs(comparison.percentageChange)
    val pctStr = String.format(java.util.Locale.US, "%.1f%%", pct)

    val isDecrease = comparison.isExpenseDecreased
    val isFlat = comparison.percentageChange == 0.0

    val badgeBg = if (isDecrease) IncomeGreen.copy(alpha = 0.15f)
    else if (isFlat) Color.Gray.copy(alpha = 0.15f)
    else ExpenseRed.copy(alpha = 0.15f)

    val badgeColor = if (isDecrease) IncomeGreen
    else if (isFlat) Color.DarkGray
    else ExpenseRed

    val icon = if (isDecrease) Icons.Default.ArrowDownward
    else if (isFlat) Icons.Default.TrendingFlat
    else Icons.Default.ArrowUpward

    val description = if (isDecrease) {
        String.format(AppStrings.get("expense_decreased", language), pctStr)
    } else if (isFlat) {
        AppStrings.get("expense_same", language)
    } else {
        String.format(AppStrings.get("expense_increased", language), pctStr)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("mom_comparison", language),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pctStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "This Month Exp.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyHelper.formatAmount(comparison.currentMonthExpense, currency),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Last Month Exp.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyHelper.formatAmount(comparison.previousMonthExpense, currency),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
