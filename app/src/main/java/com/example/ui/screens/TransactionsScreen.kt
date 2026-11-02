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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.DateFilterRange
import com.example.ui.FinanceViewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onOpenAddTransaction: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val rawAccounts by viewModel.rawAccounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedAccId by viewModel.selectedAccountFilter.collectAsState()
    val selectedCatId by viewModel.selectedCategoryFilter.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val selectedRange by viewModel.selectedDateRange.collectAsState()

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val accMap = remember(rawAccounts) { rawAccounts.associateBy { it.id } }

    var showDeleteConfirmDialog by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("transactions_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("transaction_search_input"),
                placeholder = { Text(AppStrings.get("search_hint", language), fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Date Range Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRange == DateFilterRange.THIS_MONTH,
                        onClick = { viewModel.setDateRange(DateFilterRange.THIS_MONTH) },
                        label = { Text("This Month", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedRange == DateFilterRange.LAST_MONTH,
                        onClick = { viewModel.setDateRange(DateFilterRange.LAST_MONTH) },
                        label = { Text("Last Month", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedRange == DateFilterRange.LAST_3_MONTHS,
                        onClick = { viewModel.setDateRange(DateFilterRange.LAST_3_MONTHS) },
                        label = { Text("Last 3 Months", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedRange == DateFilterRange.ALL_TIME,
                        onClick = { viewModel.setDateRange(DateFilterRange.ALL_TIME) },
                        label = { Text("All Time", fontSize = 12.sp) }
                    )
                }
            }

            // Type Filter Chips (All, Income, Expense, Transfer)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("All Types", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == "INCOME",
                        onClick = { viewModel.setTypeFilter(if (selectedType == "INCOME") null else "INCOME") },
                        label = { Text("Income", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == "EXPENSE",
                        onClick = { viewModel.setTypeFilter(if (selectedType == "EXPENSE") null else "EXPENSE") },
                        label = { Text("Expense", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == "TRANSFER",
                        onClick = { viewModel.setTypeFilter(if (selectedType == "TRANSFER") null else "TRANSFER") },
                        label = { Text("Transfer", fontSize = 12.sp) }
                    )
                }

                // Account Filter Dropdown / Chips
                items(rawAccounts) { acc ->
                    FilterChip(
                        selected = selectedAccId == acc.id,
                        onClick = { viewModel.setAccountFilter(if (selectedAccId == acc.id) null else acc.id) },
                        label = { Text(acc.name, fontSize = 12.sp) }
                    )
                }
            }

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions found matching filters.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        val cat = catMap[tx.categoryId]
                        val acc = accMap[tx.accountId]
                        val destAcc = if (tx.destAccountId != null) accMap[tx.destAccountId] else null
                        val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.US) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
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
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        val title = if (tx.type == "TRANSFER") {
                                            "Transfer to ${destAcc?.name ?: "Account"}"
                                        } else {
                                            if (language == AppLanguage.UR && cat?.nameUr != null) cat.nameUr else cat?.nameEn ?: "General"
                                        }
                                        Text(
                                            text = title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(
                                            text = "${acc?.name ?: "Account"} • ${dateFormat.format(Date(tx.dateMillis))}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (tx.notes.isNotEmpty()) {
                                            Text(
                                                text = tx.notes,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = (if (tx.type == "INCOME") "+" else if (tx.type == "EXPENSE") "-" else "") + CurrencyHelper.formatAmount(tx.amount, tx.currency),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.type == "INCOME") IncomeGreen else if (tx.type == "EXPENSE") ExpenseRed else TransferBlue
                                    )

                                    IconButton(
                                        onClick = { showDeleteConfirmDialog = tx },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog != null) {
        val tx = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this ${tx.currency} ${tx.amount} record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
