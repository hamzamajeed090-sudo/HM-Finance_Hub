package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.CurrencyHelper
import com.example.ui.FinanceViewModel
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.MonthlyTrendChart
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel
) {
    val language by viewModel.appLanguage.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val sarToPkrRate by viewModel.sarToPkrRate.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()

    var categoryToEditBudget by remember { mutableStateOf<CategoryExpenseSummary?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Monthly Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5-Month Trend Analysis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Income vs. Expense over July - November 2026",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MonthlyTrendChart(
                        transactions = transactions,
                        currency = displayCurrency,
                        sarToPkrRate = sarToPkrRate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section: Category Expense Distribution (Donut Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Expense Distribution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryDonutChart(
                        categoriesSummary = categoryBudgets,
                        currency = displayCurrency,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        }

        // Section: Budget Tracking & Progress Bars
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.get("budgets", language),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Threshold Alerts: Warning at 80%, Alert at 100%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(categoryBudgets, key = { it.category.id }) { item ->
            val cat = item.category
            val isExceeded = item.percentageUsed >= 1.0f
            val isWarning = item.percentageUsed >= 0.8f && !isExceeded

            val statusColor = if (isExceeded) ExpenseRed
            else if (isWarning) WarningAmber
            else IncomeGreen

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryToEditBudget = item },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.UR && cat.nameUr.isNotEmpty()) cat.nameUr else cat.nameEn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Percentage or alert badge
                        if (isExceeded) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ExpenseRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = AppStrings.get("budget_exceeded", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isWarning) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = AppStrings.get("budget_warning", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${(item.percentageUsed * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { item.percentageUsed.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = statusColor,
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${CurrencyHelper.formatAmount(item.totalSpent, displayCurrency)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Limit: ${CurrencyHelper.formatAmount(item.budgetLimit, displayCurrency)} (Tap to edit)",
                            fontSize = 11.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }
    }

    if (categoryToEditBudget != null) {
        val item = categoryToEditBudget!!
        var newLimit by remember { mutableStateOf(item.budgetLimit.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { categoryToEditBudget = null },
            title = { Text("Set Monthly Budget Limit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category: ${item.category.nameEn}")
                    OutlinedTextField(
                        value = newLimit,
                        onValueChange = { newLimit = it },
                        label = { Text("Limit in $displayCurrency") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newLimit.toDoubleOrNull() ?: item.budgetLimit
                        if (displayCurrency == "SAR") {
                            viewModel.updateCategoryBudget(item.category.id, limit * sarToPkrRate, limit)
                        } else {
                            viewModel.updateCategoryBudget(item.category.id, limit, if (sarToPkrRate > 0) limit / sarToPkrRate else 0.0)
                        }
                        categoryToEditBudget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(AppStrings.get("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEditBudget = null }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }
}
