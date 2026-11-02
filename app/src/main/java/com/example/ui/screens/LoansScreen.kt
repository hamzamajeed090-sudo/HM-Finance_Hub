package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.FinanceViewModel
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: FinanceViewModel
) {
    val language by viewModel.appLanguage.collectAsState()
    val loansWithDetails by viewModel.loansWithDetails.collectAsState()
    val rawAccounts by viewModel.rawAccounts.collectAsState()

    var activeTab by remember { mutableStateOf("GIVEN") } // GIVEN vs TAKEN
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var loanForRepayment by remember { mutableStateOf<LoanWithDetails?>(null) }
    var loanToDelete by remember { mutableStateOf<LoanEntity?>(null) }

    val filteredLoans = remember(loansWithDetails, activeTab) {
        loansWithDetails.filter { it.loan.direction == activeTab }
    }

    val totalPrincipal = remember(filteredLoans) { filteredLoans.sumOf { it.loan.principalAmount } }
    val totalRemaining = remember(filteredLoans) { filteredLoans.sumOf { it.remainingBalance } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLoanDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_loan_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("loans_screen")
        ) {
            // Tab Selector: Given (I am Owed) vs Taken (I Owe)
            TabRow(
                selectedTabIndex = if (activeTab == "GIVEN") 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == "GIVEN",
                    onClick = { activeTab = "GIVEN" },
                    text = {
                        Text(
                            text = AppStrings.get("loans_given", language),
                            fontWeight = if (activeTab == "GIVEN") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == "TAKEN",
                    onClick = { activeTab = "TAKEN" },
                    text = {
                        Text(
                            text = AppStrings.get("loans_taken", language),
                            fontWeight = if (activeTab == "TAKEN") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Summary Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (activeTab == "GIVEN") "Total to Receive" else "Total to Pay",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₨ ${String.format(Locale.US, "%,.0f", totalRemaining)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeTab == "GIVEN") IncomeGreen else ExpenseRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Principal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₨ ${String.format(Locale.US, "%,.0f", totalPrincipal)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Loans List
            if (filteredLoans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeTab == "GIVEN") "No loans given recorded yet." else "No debts/loans taken recorded.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoans, key = { it.loan.id }) { item ->
                        val loan = item.loan
                        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
                        val isFullyPaid = item.remainingBalance <= 0.0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loan.personName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (loan.phone.isNotEmpty()) {
                                            Text(
                                                text = loan.phone,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Status Badge
                                    val statusColor = if (isFullyPaid) IncomeGreen
                                    else if (item.isOverdue) ExpenseRed
                                    else WarningAmber

                                    val statusText = if (isFullyPaid) AppStrings.get("repaid", language)
                                    else if (item.isOverdue) AppStrings.get("overdue", language)
                                    else AppStrings.get("active", language)

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = statusText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Principal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CurrencyHelper.formatAmount(loan.principalAmount, loan.currency),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Repaid",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CurrencyHelper.formatAmount(item.totalRepaid, loan.currency),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = IncomeGreen
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = AppStrings.get("remaining", language),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CurrencyHelper.formatAmount(item.remainingBalance, loan.currency),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isFullyPaid) IncomeGreen else if (item.isOverdue) ExpenseRed else GoldAccent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Progress bar of repayment
                                val progress = if (loan.principalAmount > 0) (item.totalRepaid / loan.principalAmount).toFloat().coerceIn(0f, 1f) else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = IncomeGreen,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Due: ${dateFormat.format(Date(loan.dueDateMillis))}",
                                        fontSize = 11.sp,
                                        color = if (item.isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row {
                                        if (!isFullyPaid) {
                                            TextButton(
                                                onClick = { loanForRepayment = item }
                                            ) {
                                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(AppStrings.get("record_repayment", language), fontSize = 12.sp)
                                            }
                                        }

                                        IconButton(
                                            onClick = { loanToDelete = loan },
                                            modifier = Modifier.size(32.dp)
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

                                // Repayment History dropdown if present
                                if (item.repayments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Repayment History (${item.repayments.size} payments):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    item.repayments.forEach { rep ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${dateFormat.format(Date(rep.dateMillis))} • ${rep.notes.ifEmpty { "Payment" }}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "+${CurrencyHelper.formatAmount(rep.amount, loan.currency)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
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
    }

    // Add Loan Dialog
    if (showAddLoanDialog) {
        var personName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var currency by remember { mutableStateOf("PKR") }
        var direction by remember { mutableStateOf(activeTab) }
        var notes by remember { mutableStateOf("") }
        var dueMonthsAhead by remember { mutableStateOf(1) }

        AlertDialog(
            onDismissRequest = { showAddLoanDialog = false },
            title = { Text(AppStrings.get("add_loan", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Direction Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = direction == "GIVEN",
                            onClick = { direction = "GIVEN" },
                            label = { Text("Lending (Given)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = direction == "TAKEN",
                            onClick = { direction = "TAKEN" },
                            label = { Text("Borrowing (Taken)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text(AppStrings.get("person_name", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppStrings.get("phone", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

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

                    // Due date quick selector (1 month, 2 months, 3 months, 6 months)
                    Text("Due Date Period:", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1 to "1 Mo", 2 to "2 Mo", 3 to "3 Mo", 6 to "6 Mo").forEach { (mo, label) ->
                            FilterChip(
                                selected = dueMonthsAhead == mo,
                                onClick = { dueMonthsAhead = mo },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(AppStrings.get("notes", language)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (personName.isNotEmpty() && amt != null && amt > 0) {
                            val cal = Calendar.getInstance()
                            val startMillis = cal.timeInMillis
                            cal.add(Calendar.MONTH, dueMonthsAhead)
                            val dueMillis = cal.timeInMillis

                            viewModel.addLoan(
                                LoanEntity(
                                    personName = personName,
                                    phone = phone,
                                    direction = direction,
                                    principalAmount = amt,
                                    currency = currency,
                                    startDateMillis = startMillis,
                                    dueDateMillis = dueMillis,
                                    notes = notes
                                )
                            )
                            showAddLoanDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(AppStrings.get("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLoanDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Record Repayment Dialog
    if (loanForRepayment != null) {
        val item = loanForRepayment!!
        var repayAmount by remember { mutableStateOf(item.remainingBalance.toInt().toString()) }
        var repayNotes by remember { mutableStateOf("") }
        var selectedAccId by remember { mutableStateOf(rawAccounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { loanForRepayment = null },
            title = { Text(AppStrings.get("record_repayment", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Loan: ${item.loan.personName} (${item.loan.currency} ${item.remainingBalance.toInt()} remaining)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = repayAmount,
                        onValueChange = { repayAmount = it },
                        label = { Text("Repayment Amount (${item.loan.currency})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = repayNotes,
                        onValueChange = { repayNotes = it },
                        label = { Text("Payment Note / Source") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = repayAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.addRepayment(
                                LoanRepaymentEntity(
                                    loanId = item.loan.id,
                                    dateMillis = System.currentTimeMillis(),
                                    amount = amt,
                                    notes = repayNotes,
                                    accountId = selectedAccId
                                )
                            )
                            loanForRepayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(AppStrings.get("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { loanForRepayment = null }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    if (loanToDelete != null) {
        val l = loanToDelete!!
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("Delete Loan Record") },
            text = { Text("Are you sure you want to delete loan for ${l.personName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLoan(l)
                        loanToDelete = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
