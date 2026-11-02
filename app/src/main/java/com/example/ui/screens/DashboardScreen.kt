package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.FinanceViewModel
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.MonthOverMonthCard
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenAddTransfer: () -> Unit
) {
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val sarToPkrRate by viewModel.sarToPkrRate.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val monthComparison by viewModel.monthComparison.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val accMap = remember(accounts) { accounts.associateBy { it.account.id } }

    // Total net worth converted to active display currency
    val totalBalance = remember(accounts, displayCurrency, sarToPkrRate) {
        accounts.sumOf { acc ->
            CurrencyHelper.convert(acc.currentBalance, acc.account.currency, displayCurrency, sarToPkrRate)
        }
    }

    // Equivalent in other currency
    val otherCurrency = if (displayCurrency == "PKR") "SAR" else "PKR"
    val convertedEquivalent = remember(totalBalance, displayCurrency, sarToPkrRate) {
        CurrencyHelper.convert(totalBalance, displayCurrency, otherCurrency, sarToPkrRate)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.get("app_title", language),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = AppStrings.get("app_subtitle", language),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Currency Toggle Pill (PKR <-> SAR)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = EmeraldPrimary,
                        modifier = Modifier
                            .testTag("currency_toggle_button")
                            .clickable {
                                viewModel.setDisplayCurrency(if (displayCurrency == "PKR") "SAR" else "PKR")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = "Toggle Currency",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayCurrency,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Language Toggle (EN <-> UR)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .testTag("language_toggle_button")
                            .clickable {
                                viewModel.setAppLanguage(if (language == AppLanguage.EN) AppLanguage.UR else AppLanguage.EN)
                            }
                    ) {
                        Text(
                            text = if (language == AppLanguage.EN) "اردو" else "EN",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Hero Total Net Worth Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Slate900,
                                    Color(0xFF0F3B40),
                                    EmeraldPrimary
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.get("total_balance", language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "1 SAR ≈ ${String.format(Locale.US, "%.2f", sarToPkrRate)} PKR",
                                fontSize = 11.sp,
                                color = GoldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = CurrencyHelper.formatAmount(totalBalance, displayCurrency),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "≈ ${CurrencyHelper.formatAmount(convertedEquivalent, otherCurrency)} ($otherCurrency Linked)",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Monthly Income vs Expense Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Pill
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.get("income", language),
                                            fontSize = 10.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Text(
                                            text = CurrencyHelper.formatAmount(monthComparison.currentMonthIncome, displayCurrency),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Expense Pill
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.get("expense", language),
                                            fontSize = 10.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Text(
                                            text = CurrencyHelper.formatAmount(monthComparison.currentMonthExpense, displayCurrency),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddTransaction,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_transaction_action"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = AppStrings.get("add_transaction", language), fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenAddTransfer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transfer_action"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = AppStrings.get("transfer", language), fontSize = 12.sp)
                }
            }
        }

        // Month-over-Month Comparison Card
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                MonthOverMonthCard(
                    comparison = monthComparison,
                    currency = displayCurrency,
                    language = language
                )
            }
        }

        // Accounts Section Header & Horizontal Carousel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("accounts", language),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "View All →",
                    fontSize = 13.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.clickable { onNavigateToAccounts() }
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { item ->
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { onNavigateToAccounts() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.account.currency,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = item.account.type,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.account.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = CurrencyHelper.formatAmount(item.currentBalance, item.account.currency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Category Breakdown Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clickable { onNavigateToAnalytics() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expense Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analytics →",
                            fontSize = 12.sp,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "All Transactions →",
                    fontSize = 13.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }
        }

        val recentTx = transactions.take(6)
        if (recentTx.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions yet. Add your first record!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(recentTx) { tx ->
                val cat = catMap[tx.categoryId]
                val acc = accMap[tx.accountId]
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.type == "INCOME") IncomeGreen.copy(alpha = 0.15f)
                                        else if (tx.type == "EXPENSE") ExpenseRed.copy(alpha = 0.15f)
                                        else TransferBlue.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.type == "INCOME") Icons.Default.ArrowDownward
                                    else if (tx.type == "EXPENSE") Icons.Default.ArrowUpward
                                    else Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = if (tx.type == "INCOME") IncomeGreen else if (tx.type == "EXPENSE") ExpenseRed else TransferBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (language == AppLanguage.UR && cat?.nameUr != null) cat.nameUr else cat?.nameEn ?: (if (tx.type == "TRANSFER") "Transfer" else "General"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${acc?.account?.name ?: "Account"} • ${dateFormat.format(Date(tx.dateMillis))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = (if (tx.type == "INCOME") "+" else if (tx.type == "EXPENSE") "-" else "") + CurrencyHelper.formatAmount(tx.amount, tx.currency),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.type == "INCOME") IncomeGreen else if (tx.type == "EXPENSE") ExpenseRed else TransferBlue
                        )
                    }
                }
            }
        }
    }
}
