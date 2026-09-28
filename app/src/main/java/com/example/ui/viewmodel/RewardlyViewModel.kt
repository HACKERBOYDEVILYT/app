package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.repository.RewardlyRepository
import com.example.data.repository.VerificationState
import com.example.domain.engine.ProviderVerificationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppScreen {
    HOME,
    EARN,
    WALLET,
    WITHDRAW,
    REFERRALS,
    PROFILE,
    NOTIFICATIONS,
    SUPPORT,
    ADMIN_PANEL
}

enum class AdminSubTab {
    OVERVIEW,
    USERS,
    ACTIVITIES,
    WITHDRAWALS,
    CAMPAIGNS,
    FRAUD,
    AUDIT,
    REPORTS
}

data class ActiveSessionState(
    val activity: EarningActivityEntity,
    val sessionToken: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isCompleted: Boolean = false,
    val isVerifying: Boolean = false,
    val verificationResult: ProviderVerificationResult? = null,
    val error: String? = null
)

class RewardlyViewModel(
    private val repository: RewardlyRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _adminSubTab = MutableStateFlow(AdminSubTab.OVERVIEW)
    val adminSubTab: StateFlow<AdminSubTab> = _adminSubTab.asStateFlow()

    val currentUser = repository.currentUser
    val currentWallet = repository.currentWallet
    val userTransactions = repository.userTransactions
    val activeActivities = repository.activeActivities
    val allActivities = repository.allActivities
    val userReferrals = repository.userReferrals
    val userWithdrawals = repository.userWithdrawals
    val userNotifications = repository.userNotifications
    val unreadNotifCount = repository.unreadNotifCount
    val userTickets = repository.userTickets

    // Admin streams
    val allUsers = repository.allUsers
    val allWithdrawals = repository.allWithdrawals
    val allFraudEvents = repository.allFraudEvents
    val allAuditLogs = repository.allAuditLogs
    val allCampaigns = repository.allCampaigns
    val totalRewardsIssued = repository.totalRewardsIssued
    val pendingWithdrawalsCount = repository.pendingWithdrawalsCount

    // Daily Claim State
    private val _isDailyClaimedToday = MutableStateFlow(false)
    val isDailyClaimedToday: StateFlow<Boolean> = _isDailyClaimedToday.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    // Activity Verification Modal State
    private val _activeSession = MutableStateFlow<ActiveSessionState?>(null)
    val activeSession: StateFlow<ActiveSessionState?> = _activeSession.asStateFlow()
    private var verificationTimerJob: Job? = null

    // UI Feedback Toast/Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Selected ticket for conversation
    private val _selectedTicketId = MutableStateFlow<String?>(null)
    val selectedTicketId: StateFlow<String?> = _selectedTicketId.asStateFlow()

    val selectedTicketMessages: Flow<List<SupportMessageEntity>> = _selectedTicketId.flatMapLatest { id ->
        if (id != null) repository.getTicketMessages(id) else flowOf(emptyList())
    }

    init {
        refreshDailyStreak()
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setAdminSubTab(subTab: AdminSubTab) {
        _adminSubTab.value = subTab
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectTicket(ticketId: String?) {
        _selectedTicketId.value = ticketId
    }

    fun refreshDailyStreak() {
        viewModelScope.launch {
            val (claimed, streak) = repository.getDailyStreakStatus()
            _isDailyClaimedToday.value = claimed
            _currentStreak.value = streak
        }
    }

    fun switchUserRole(role: String) {
        viewModelScope.launch {
            if (role == "ADMIN") {
                repository.switchToDemoAdmin()
                _currentScreen.value = AppScreen.ADMIN_PANEL
                showToast("Switched to Sarah Chen (Super Admin)")
            } else {
                repository.switchToDemoUser()
                _currentScreen.value = AppScreen.HOME
                showToast("Switched to Alex Johnson (Verified Earner)")
            }
            refreshDailyStreak()
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            try {
                val reward = repository.claimDailyBonus()
                _isDailyClaimedToday.value = true
                _currentStreak.value = _currentStreak.value + 1
                showToast("Claimed daily bonus +$${String.format(Locale.US, "%.2f", reward)}!")
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to claim daily bonus")
            }
        }
    }

    // Activity Execution & Verification
    fun startActivity(activity: EarningActivityEntity) {
        viewModelScope.launch {
            try {
                val sessionToken = repository.startEarningActivity(activity.id)
                val duration = activity.durationSeconds.coerceAtLeast(6)
                _activeSession.value = ActiveSessionState(
                    activity = activity,
                    sessionToken = sessionToken,
                    totalSeconds = duration,
                    remainingSeconds = duration
                )

                // Start countdown
                verificationTimerJob?.cancel()
                verificationTimerJob = viewModelScope.launch {
                    var remaining = duration
                    while (remaining > 0) {
                        delay(1000)
                        remaining -= 1
                        _activeSession.update { current ->
                            current?.copy(remainingSeconds = remaining)
                        }
                    }
                    _activeSession.update { current ->
                        current?.copy(isCompleted = true)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Could not start activity")
            }
        }
    }

    fun completeAndVerifyActivity() {
        val current = _activeSession.value ?: return
        viewModelScope.launch {
            _activeSession.update { it?.copy(isVerifying = true) }
            // Small simulated network roundtrip for cryptographic handshake
            delay(1200)

            val result = repository.verifyAndCreditActivity(
                activityId = current.activity.id,
                sessionToken = current.sessionToken
            )

            if (result.isValid) {
                _activeSession.update {
                    it?.copy(
                        isVerifying = false,
                        verificationResult = result
                    )
                }
                showToast("Success! +$${String.format(Locale.US, "%.2f", result.verifiedRewardAmount)} credited.")
            } else {
                _activeSession.update {
                    it?.copy(
                        isVerifying = false,
                        error = result.failureReason ?: "Verification failed"
                    )
                }
            }
        }
    }

    fun dismissActivityModal() {
        verificationTimerJob?.cancel()
        _activeSession.value = null
    }

    // Withdrawal
    fun submitWithdrawal(amount: Double, method: String, destination: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.requestWithdrawal(amount, method, destination)
                showToast("Withdrawal of $${String.format(Locale.US, "%.2f", amount)} submitted successfully.")
                onSuccess()
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to submit withdrawal")
            }
        }
    }

    // Admin Actions
    fun adminApproveWithdrawal(withdrawalId: String, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminApproveWithdrawal(withdrawalId, adminUser)
                showToast("Withdrawal $withdrawalId approved.")
            } catch (e: Exception) {
                showToast(e.message ?: "Approval failed")
            }
        }
    }

    fun adminRejectWithdrawal(withdrawalId: String, reason: String, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminRejectWithdrawal(withdrawalId, reason, adminUser)
                showToast("Withdrawal $withdrawalId rejected and refunded.")
            } catch (e: Exception) {
                showToast(e.message ?: "Rejection failed")
            }
        }
    }

    fun adminSuspendUser(userId: String, adminUser: UserEntity, reason: String) {
        viewModelScope.launch {
            try {
                repository.adminSuspendUser(userId, adminUser, reason)
                showToast("User suspended.")
            } catch (e: Exception) {
                showToast(e.message ?: "Action failed")
            }
        }
    }

    fun adminRestoreUser(userId: String, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminRestoreUser(userId, adminUser)
                showToast("User account restored to active.")
            } catch (e: Exception) {
                showToast(e.message ?: "Action failed")
            }
        }
    }

    fun adminAdjustBalance(userId: String, amount: Double, reason: String, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminAdjustWallet(userId, amount, reason, adminUser)
                showToast("Balance adjusted by $${amount}.")
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to adjust balance")
            }
        }
    }

    fun adminCreateActivity(activity: EarningActivityEntity, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminCreateActivity(activity, adminUser)
                showToast("New earning activity created.")
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to create activity")
            }
        }
    }

    fun adminCreateCampaign(campaign: CampaignEntity, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminCreateCampaign(campaign, adminUser)
                showToast("New campaign launched.")
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to create campaign")
            }
        }
    }

    fun adminResolveFraud(fraudId: String, adminUser: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminResolveFraud(fraudId, adminUser)
                showToast("Fraud alert marked resolved.")
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to resolve fraud alert")
            }
        }
    }

    // Support
    fun createSupportTicket(subject: String, category: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val ticketId = repository.createSupportTicket(subject, category, message)
                _selectedTicketId.value = ticketId
                showToast("Support ticket created.")
                onSuccess()
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to create support ticket")
            }
        }
    }

    fun replyToTicket(ticketId: String, message: String) {
        viewModelScope.launch {
            try {
                repository.replyToTicket(ticketId, message)
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to send message")
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            showToast("Notifications marked as read.")
        }
    }
}
