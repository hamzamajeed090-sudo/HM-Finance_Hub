package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.FinanceViewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val rawAccounts by viewModel.rawAccounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var type by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(displayCurrency) }
    var selectedAccId by remember { mutableStateOf(rawAccounts.firstOrNull()?.id ?: 1L) }
    var selectedCatId by remember {
        mutableStateOf(categories.filter { !it.isIncome }.firstOrNull()?.id ?: 1L)
    }
    var notes by remember { mutableStateOf("") }

    val filteredCategories = remember(categories, type) {
        if (type == "INCOME") categories.filter { it.isIncome }
        else categories.filter { !it.isIncome }
    }

    LaunchedEffect(type) {
        val firstMatch = filteredCategories.firstOrNull()?.id
        if (firstMatch != null) selectedCatId = firstMatch
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("add_transaction", language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type Switcher (Income vs Expense)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Expense (خرچ)", color = if (type == "EXPENSE") ExpenseRed else MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Income (آمدنی)", color = if (type == "INCOME") IncomeGreen else MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount & Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(AppStrings.get("amount", language)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    FilterChip(
                        selected = currency == "PKR",
                        onClick = { currency = if (currency == "PKR") "SAR" else "PKR" },
                        label = { Text(currency) },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Account Selection
                Text("Select Account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rawAccounts.take(3).forEach { acc ->
                        FilterChip(
                            selected = selectedAccId == acc.id,
                            onClick = {
                                selectedAccId = acc.id
                                currency = acc.currency
                            },
                            label = { Text(acc.name.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }

                // Category Selection
                Text("Select Category:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filteredCategories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(if (language == AppLanguage.UR && cat.nameUr.isNotEmpty()) cat.nameUr else cat.nameEn.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.get("notes", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.addTransaction(
                            TransactionEntity(
                                dateMillis = System.currentTimeMillis(),
                                type = type,
                                amount = amt,
                                currency = currency,
                                categoryId = selectedCatId,
                                accountId = selectedAccId,
                                notes = notes
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(AppStrings.get("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )
}
