package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransferDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()
    val rawAccounts by viewModel.rawAccounts.collectAsState()
    val sarToPkrRate by viewModel.sarToPkrRate.collectAsState()

    var sourceAccId by remember { mutableStateOf(rawAccounts.firstOrNull()?.id ?: 1L) }
    var destAccId by remember {
        mutableStateOf(rawAccounts.getOrNull(1)?.id ?: (rawAccounts.firstOrNull()?.id ?: 1L))
    }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val sourceAcc = rawAccounts.find { it.id == sourceAccId }
    val destAcc = rawAccounts.find { it.id == destAccId }
    val isCrossCurrency = sourceAcc?.currency != destAcc?.currency && sourceAcc != null && destAcc != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("transfer", language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transfer Funds Between Accounts",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Source Account
                Text("From Account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rawAccounts.take(3).forEach { acc ->
                        FilterChip(
                            selected = sourceAccId == acc.id,
                            onClick = { sourceAccId = acc.id },
                            label = { Text("${acc.name.substringBefore(" ")} (${acc.currency})", fontSize = 11.sp) }
                        )
                    }
                }

                // Destination Account
                Text("To Account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rawAccounts.take(3).forEach { acc ->
                        FilterChip(
                            selected = destAccId == acc.id,
                            onClick = { destAccId = acc.id },
                            label = { Text("${acc.name.substringBefore(" ")} (${acc.currency})", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Transfer Amount (${sourceAcc?.currency ?: "PKR"})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (isCrossCurrency) {
                    Text(
                        text = "Cross Currency Rate applied: 1 SAR = $sarToPkrRate PKR",
                        fontSize = 11.sp,
                        color = EmeraldPrimary
                    )
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
                    if (amt != null && amt > 0 && sourceAccId != destAccId) {
                        viewModel.addTransaction(
                            TransactionEntity(
                                dateMillis = System.currentTimeMillis(),
                                type = "TRANSFER",
                                amount = amt,
                                currency = sourceAcc?.currency ?: "PKR",
                                categoryId = 0L,
                                accountId = sourceAccId,
                                destAccountId = destAccId,
                                exchangeRate = sarToPkrRate,
                                notes = notes.ifEmpty { "Transfer from ${sourceAcc?.name} to ${destAcc?.name}" }
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
