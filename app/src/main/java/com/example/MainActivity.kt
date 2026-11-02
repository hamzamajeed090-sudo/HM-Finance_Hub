package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.notification.AppNotificationHelper
import com.example.ui.FinanceViewModel
import com.example.ui.components.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

enum class NavigationScreen {
    DASHBOARD,
    TRANSACTIONS,
    ACCOUNTS,
    LOANS,
    ANALYTICS,
    EXPORT,
    SETTINGS,
    DHIKR
}

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppNotificationHelper.initChannel(this)

        setContent {
            MyApplicationTheme {
                val language by viewModel.appLanguage.collectAsState()
                val layoutDirection = if (language == AppLanguage.UR) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    val showSplash by viewModel.showSplashScreen.collectAsState()
                    val isPinEnabled by viewModel.isPinLockEnabled.collectAsState()
                    val isUnlocked by viewModel.isAppUnlocked.collectAsState()
                    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

                    if (showSplash) {
                        SplashScreen(
                            onDismiss = {
                                viewModel.showSplashScreen.value = false
                                if (!isPinEnabled) {
                                    viewModel.isAppUnlocked.value = true
                                }
                            }
                        )
                    } else if (isPinEnabled && !isUnlocked) {
                        PinLockScreen(
                            correctPin = viewModel.getSavedPin(),
                            isBiometricAvailable = isBiometricEnabled,
                            language = language,
                            onUnlockSuccess = {
                                viewModel.isAppUnlocked.value = true
                            }
                        )
                    } else {
                        MainAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddTransferDialog by remember { mutableStateOf(false) }

    // Custom back navigation handler
    BackHandler(enabled = currentScreen != NavigationScreen.DASHBOARD) {
        currentScreen = NavigationScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentScreen != NavigationScreen.DHIKR) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.DASHBOARD,
                        onClick = { currentScreen = NavigationScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(AppStrings.get("dashboard", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.TRANSACTIONS,
                        onClick = { currentScreen = NavigationScreen.TRANSACTIONS },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text(AppStrings.get("transactions", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.ACCOUNTS,
                        onClick = { currentScreen = NavigationScreen.ACCOUNTS },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Accounts") },
                        label = { Text(AppStrings.get("accounts", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.LOANS,
                        onClick = { currentScreen = NavigationScreen.LOANS },
                        icon = { Icon(Icons.Default.Handshake, contentDescription = "Loans") },
                        label = { Text(AppStrings.get("loans", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.ANALYTICS,
                        onClick = { currentScreen = NavigationScreen.ANALYTICS },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Analytics") },
                        label = { Text(AppStrings.get("analytics", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                    NavigationBarItem(
                        selected = currentScreen == NavigationScreen.SETTINGS || currentScreen == NavigationScreen.EXPORT,
                        onClick = { currentScreen = NavigationScreen.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "More") },
                        label = { Text(AppStrings.get("settings", language)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldPrimary)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavigationScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentScreen = NavigationScreen.TRANSACTIONS },
                        onNavigateToAccounts = { currentScreen = NavigationScreen.ACCOUNTS },
                        onNavigateToLoans = { currentScreen = NavigationScreen.LOANS },
                        onNavigateToAnalytics = { currentScreen = NavigationScreen.ANALYTICS },
                        onOpenAddTransaction = { showAddTransactionDialog = true },
                        onOpenAddTransfer = { showAddTransferDialog = true }
                    )
                }
                NavigationScreen.TRANSACTIONS -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onOpenAddTransaction = { showAddTransactionDialog = true }
                    )
                }
                NavigationScreen.ACCOUNTS -> {
                    AccountsScreen(
                        viewModel = viewModel,
                        onOpenAddTransfer = { showAddTransferDialog = true }
                    )
                }
                NavigationScreen.LOANS -> {
                    LoansScreen(viewModel = viewModel)
                }
                NavigationScreen.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                NavigationScreen.EXPORT -> {
                    ExportScreen(viewModel = viewModel)
                }
                NavigationScreen.SETTINGS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top tabs for Settings vs Export
                        var activeSubTab by remember { mutableStateOf(0) }
                        TabRow(selectedTabIndex = activeSubTab) {
                            Tab(
                                selected = activeSubTab == 0,
                                onClick = { activeSubTab = 0 },
                                text = { Text(AppStrings.get("settings", language)) }
                            )
                            Tab(
                                selected = activeSubTab == 1,
                                onClick = { activeSubTab = 1 },
                                text = { Text(AppStrings.get("export", language)) }
                            )
                        }

                        if (activeSubTab == 0) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onOpenDhikrTool = { currentScreen = NavigationScreen.DHIKR }
                            )
                        } else {
                            ExportScreen(viewModel = viewModel)
                        }
                    }
                }
                NavigationScreen.DHIKR -> {
                    DhikrCounterScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = NavigationScreen.SETTINGS }
                    )
                }
            }
        }
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddTransactionDialog = false }
        )
    }

    if (showAddTransferDialog) {
        AddTransferDialog(
            viewModel = viewModel,
            onDismiss = { showAddTransferDialog = false }
        )
    }
}
