package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.repository.RewardlyRepository
import com.example.ui.components.ActivityVerificationModal
import com.example.ui.components.AdminPasswordDialog
import com.example.ui.components.RewardlyBottomBar
import com.example.ui.components.RewardlyTopBar
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.earn.EarnScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.referrals.ReferralsScreen
import com.example.ui.screens.support.SupportScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.screens.withdraw.WithdrawScreen
import com.example.ui.theme.RewardlyTheme
import com.example.ui.viewmodel.ADMIN_MASTER_PASSWORD
import com.example.ui.viewmodel.AdminSubTab
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.RewardlyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: RewardlyRepository
    private lateinit var viewModel: RewardlyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getInstance(applicationContext)
        repository = RewardlyRepository(database)
        viewModel = RewardlyViewModel(repository)

        // Seed initial demo data safely
        lifecycleScope.launch {
            DatabaseInitializer.seedIfEmpty(database)
            viewModel.refreshDailyStreak()
        }

        setContent {
            RewardlyTheme {
                RewardlyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RewardlyApp(viewModel: RewardlyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val adminSubTab by viewModel.adminSubTab.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle(initialValue = null)
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeActivities by viewModel.activeActivities.collectAsStateWithLifecycle(initialValue = emptyList())
    val userReferrals by viewModel.userReferrals.collectAsStateWithLifecycle(initialValue = emptyList())
    val userWithdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle(initialValue = emptyList())
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadNotifs by viewModel.unreadNotifCount.collectAsStateWithLifecycle(initialValue = 0)
    val userTickets by viewModel.userTickets.collectAsStateWithLifecycle(initialValue = emptyList())
    val isDailyClaimed by viewModel.isDailyClaimedToday.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Ads Accounts stream
    val allAdsAccounts by viewModel.allAdsAccounts.collectAsStateWithLifecycle(initialValue = emptyList())

    // Admin streams
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle(initialValue = emptyList())
    val allActivities by viewModel.allActivities.collectAsStateWithLifecycle(initialValue = emptyList())
    val allCampaigns by viewModel.allCampaigns.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFraudEvents by viewModel.allFraudEvents.collectAsStateWithLifecycle(initialValue = emptyList())
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalRewardsIssued by viewModel.totalRewardsIssued.collectAsStateWithLifecycle(initialValue = 0.0)

    val selectedTicketId by viewModel.selectedTicketId.collectAsStateWithLifecycle()
    val ticketMessages by viewModel.selectedTicketMessages.collectAsStateWithLifecycle(initialValue = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }

    // Handle Toast snackbar messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Handle back button on secondary screens
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.AUTH) {
        if (currentScreen == AppScreen.SUPPORT && selectedTicketId != null) {
            viewModel.selectTicket(null)
        } else {
            viewModel.setScreen(AppScreen.HOME)
        }
    }

    val isAdminUser = currentUser?.role in listOf("SUPER_ADMIN", "ADMIN")
    val isAuthScreen = currentScreen == AppScreen.AUTH

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isAuthScreen && currentScreen != AppScreen.NOTIFICATIONS && currentScreen != AppScreen.SUPPORT) {
                RewardlyTopBar(
                    user = currentUser,
                    wallet = currentWallet,
                    unreadNotifs = unreadNotifs,
                    currentScreen = currentScreen,
                    language = currentLanguage,
                    onNotificationsClick = { viewModel.setScreen(AppScreen.NOTIFICATIONS) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onLogoutClick = { viewModel.logout() },
                    onRoleSwitchClick = {
                        if (currentUser?.role == "SUPER_ADMIN" || currentUser?.role == "ADMIN") {
                            viewModel.switchUserRole("USER")
                        } else {
                            showAdminPasswordDialog = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen && currentScreen != AppScreen.NOTIFICATIONS && currentScreen != AppScreen.SUPPORT) {
                RewardlyBottomBar(
                    currentScreen = currentScreen,
                    isAdminUser = isAdminUser,
                    language = currentLanguage,
                    onTabSelected = { screen -> viewModel.setScreen(screen) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isAuthScreen) PaddingValues(0.dp) else innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.AUTH -> {
                    AuthScreen(
                        language = currentLanguage,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onLoginDemoUser = { viewModel.loginAsDemoUser() },
                        onLoginUser = { identifier -> viewModel.loginUser(identifier) },
                        onRegisterUser = { name, email, uname, ref -> viewModel.registerUser(name, email, uname, ref) },
                        onLoginAdmin = { pwd -> viewModel.loginAdminWithPassword(pwd) }
                    )
                }
                AppScreen.HOME -> {
                    HomeScreen(
                        user = currentUser,
                        wallet = currentWallet,
                        activities = activeActivities,
                        transactions = transactions,
                        isDailyClaimed = isDailyClaimed,
                        currentStreak = currentStreak,
                        onClaimDaily = { viewModel.claimDailyBonus() },
                        onNavigate = { screen -> viewModel.setScreen(screen) },
                        onStartActivity = { act -> viewModel.startActivity(act) }
                    )
                }
                AppScreen.EARN -> {
                    EarnScreen(
                        activities = activeActivities,
                        onStartActivity = { act -> viewModel.startActivity(act) }
                    )
                }
                AppScreen.WALLET -> {
                    WalletScreen(
                        wallet = currentWallet,
                        transactions = transactions,
                        onWithdrawClick = { viewModel.setScreen(AppScreen.WITHDRAW) }
                    )
                }
                AppScreen.WITHDRAW -> {
                    WithdrawScreen(
                        wallet = currentWallet,
                        withdrawals = userWithdrawals,
                        onSubmitWithdrawal = { amount, method, destination ->
                            viewModel.submitWithdrawal(amount, method, destination) {
                                viewModel.setScreen(AppScreen.WALLET)
                            }
                        }
                    )
                }
                AppScreen.REFERRALS -> {
                    ReferralsScreen(
                        user = currentUser,
                        referrals = userReferrals,
                        onShowToast = { msg -> viewModel.showToast(msg) }
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        language = currentLanguage,
                        onNavigate = { screen -> viewModel.setScreen(screen) },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onLogout = { viewModel.logout() },
                        onSwitchRole = { role ->
                            if (role == "ADMIN") {
                                showAdminPasswordDialog = true
                            } else {
                                viewModel.switchUserRole("USER")
                            }
                        }
                    )
                }
                AppScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = userNotifications,
                        onBackClick = { viewModel.setScreen(AppScreen.HOME) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                }
                AppScreen.SUPPORT -> {
                    SupportScreen(
                        tickets = userTickets,
                        selectedTicketId = selectedTicketId,
                        messages = ticketMessages,
                        onSelectTicket = { id -> viewModel.selectTicket(id) },
                        onCreateTicket = { subject, category, message ->
                            viewModel.createSupportTicket(subject, category, message) {}
                        },
                        onSendMessage = { ticketId, msg -> viewModel.replyToTicket(ticketId, msg) },
                        onBackClick = { viewModel.setScreen(AppScreen.PROFILE) }
                    )
                }
                AppScreen.ADMIN_PANEL -> {
                    if (currentUser != null) {
                        AdminScreen(
                            adminUser = currentUser!!,
                            currentTab = adminSubTab,
                            users = allUsers,
                            withdrawals = allWithdrawals,
                            activities = allActivities,
                            campaigns = allCampaigns,
                            fraudEvents = allFraudEvents,
                            auditLogs = allAuditLogs,
                            totalRewardsIssued = totalRewardsIssued ?: 0.0,
                            adsAccounts = allAdsAccounts,
                            onTabSelected = { tab -> viewModel.setAdminSubTab(tab) },
                            onApproveWithdrawal = { id -> viewModel.adminApproveWithdrawal(id, currentUser!!) },
                            onRejectWithdrawal = { id, reason -> viewModel.adminRejectWithdrawal(id, reason, currentUser!!) },
                            onSuspendUser = { id, reason -> viewModel.adminSuspendUser(id, currentUser!!, reason) },
                            onRestoreUser = { id -> viewModel.adminRestoreUser(id, currentUser!!) },
                            onAdjustBalance = { id, amt, reason -> viewModel.adminAdjustBalance(id, amt, reason, currentUser!!) },
                            onCreateActivity = { act -> viewModel.adminCreateActivity(act, currentUser!!) },
                            onCreateCampaign = { camp -> viewModel.adminCreateCampaign(camp, currentUser!!) },
                            onResolveFraud = { id -> viewModel.adminResolveFraud(id, currentUser!!) },
                            onAddAdsAccount = { net, label, app, rew, inter, rate, enabled ->
                                viewModel.addAdsAccount(net, label, app, rew, inter, rate, enabled)
                            },
                            onToggleAdsAccount = { id, enabled -> viewModel.toggleAdsAccount(id, enabled) },
                            onDeleteAdsAccount = { id -> viewModel.deleteAdsAccount(id) },
                            onShowToast = { msg -> viewModel.showToast(msg) }
                        )
                    }
                }
            }

            // Interactive Activity Verification Modal
            activeSession?.let { session ->
                ActivityVerificationModal(
                    session = session,
                    onVerifyClick = { viewModel.completeAndVerifyActivity() },
                    onDismiss = { viewModel.dismissActivityModal() }
                )
            }

            // Admin Master Password Dialog
            if (showAdminPasswordDialog) {
                AdminPasswordDialog(
                    language = currentLanguage,
                    onDismiss = { showAdminPasswordDialog = false },
                    onSuccess = {
                        showAdminPasswordDialog = false
                        viewModel.loginAdminWithPassword(ADMIN_MASTER_PASSWORD)
                    }
                )
            }
        }
    }
}
