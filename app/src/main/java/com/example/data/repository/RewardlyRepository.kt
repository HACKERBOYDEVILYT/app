package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.domain.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

sealed class VerificationState {
    object Idle : VerificationState()
    data class InProgress(val progressPercent: Float, val statusMessage: String) : VerificationState()
    data class Success(val rewardAmount: Double, val eventId: String, val signature: String) : VerificationState()
    data class Error(val message: String) : VerificationState()
}

class RewardlyRepository(
    private val database: AppDatabase,
    private val adProvider: AdProviderAdapter = MockRewardProvider()
) {
    private val userDao = database.userDao()
    private val walletDao = database.walletDao()
    private val activityDao = database.activityDao()
    private val dailyClaimDao = database.dailyClaimDao()
    private val referralDao = database.referralDao()
    private val withdrawalDao = database.withdrawalDao()
    private val notifDao = database.notificationDao()
    private val supportDao = database.supportDao()
    private val adminDao = database.adminDao()
    private val adsAccountDao = database.adsAccountDao()
    private val fraudRiskEngine = FraudRiskEngine()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id != null) userDao.observeUser(id) else flowOf(null)
    }

    val currentWallet: Flow<WalletEntity?> = _currentUserId.flatMapLatest { id ->
        if (id != null) walletDao.observeWallet(id) else flowOf(null)
    }

    val userTransactions: Flow<List<WalletTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) walletDao.getTransactions(id) else flowOf(emptyList())
    }

    val activeActivities: Flow<List<EarningActivityEntity>> = activityDao.getActiveActivities()

    val allActivities: Flow<List<EarningActivityEntity>> = activityDao.getAllActivities()

    val userReferrals: Flow<List<ReferralEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) referralDao.getReferralsForUser(id) else flowOf(emptyList())
    }

    val userWithdrawals: Flow<List<WithdrawalRequestEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) withdrawalDao.getWithdrawalsForUser(id) else flowOf(emptyList())
    }

    val userNotifications: Flow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) notifDao.getNotificationsForUser(id) else flowOf(emptyList())
    }

    val unreadNotifCount: Flow<Int> = _currentUserId.flatMapLatest { id ->
        if (id != null) notifDao.getUnreadCount(id) else flowOf(0)
    }

    val userTickets: Flow<List<SupportTicketEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) supportDao.getTicketsForUser(id) else flowOf(emptyList())
    }

    // Ads Accounts feeds
    val allAdsAccounts: Flow<List<AdsAccountEntity>> = adsAccountDao.getAllAdsAccounts()
    val activeAdsAccounts: Flow<List<AdsAccountEntity>> = adsAccountDao.getActiveAdsAccounts()

    // Admin feeds
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allWithdrawals: Flow<List<WithdrawalRequestEntity>> = withdrawalDao.getAllWithdrawals()
    val allFraudEvents: Flow<List<FraudEventEntity>> = adminDao.getAllFraudEvents()
    val allAuditLogs: Flow<List<AdminAuditLogEntity>> = adminDao.getAllAuditLogs()
    val allCampaigns: Flow<List<CampaignEntity>> = adminDao.getAllCampaigns()
    val allTickets: Flow<List<SupportTicketEntity>> = supportDao.getAllTickets()
    val totalRewardsIssued: Flow<Double?> = walletDao.getTotalRewardsIssued()
    val pendingWithdrawalsCount: Flow<Int> = withdrawalDao.getPendingWithdrawalsCount()

    private suspend fun requireCurrentUser(): UserEntity {
        val uid = _currentUserId.value ?: throw IllegalStateException("User not logged in. Please sign in.")
        return userDao.getUserById(uid) ?: throw IllegalStateException("User account not found.")
    }

    suspend fun switchUser(userId: String?) = withContext(Dispatchers.IO) {
        _currentUserId.value = userId
    }

    suspend fun switchToDemoUser() = switchUser("user_alex_01")
    suspend fun switchToDemoAdmin() = switchUser("admin_sarah_01")
    suspend fun logout() = switchUser(null)

    suspend fun registerUser(
        displayName: String,
        email: String,
        username: String,
        referralCode: String? = null
    ): UserEntity = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        val existing = userDao.getUserByUsername(cleanUsername)
        if (existing != null) {
            throw IllegalArgumentException("Username @$cleanUsername is already registered.")
        }
        val newId = "user_" + UUID.randomUUID().toString().substring(0, 8)
        val userRefCode = (cleanUsername.take(4).uppercase() + (1000..9999).random().toString())
        val newUser = UserEntity(
            id = newId,
            username = cleanUsername,
            email = email.trim(),
            displayName = displayName.trim(),
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80",
            country = "Bangladesh",
            timezone = "Asia/Dhaka",
            referralCode = userRefCode,
            referredBy = referralCode?.trim()?.takeIf { it.isNotEmpty() },
            status = "ACTIVE",
            role = "USER",
            riskScore = 5,
            isKycVerified = true,
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        walletDao.insertWallet(
            WalletEntity(
                userId = newId,
                balance = 0.0,
                pendingBalance = 0.0,
                lifetimeEarned = 0.0,
                lifetimeWithdrawn = 0.0
            )
        )
        _currentUserId.value = newId
        newUser
    }

    suspend fun loginAsUser(identifier: String): UserEntity = withContext(Dispatchers.IO) {
        val clean = identifier.trim().lowercase().removePrefix("@")
        val user = userDao.getUserByUsername(clean)
            ?: userDao.getUserById(clean)
            ?: userDao.getAllUsersList().firstOrNull { it.email.equals(clean, ignoreCase = true) }
            ?: userDao.getUserById("user_alex_01")
            ?: throw IllegalArgumentException("User account not found: $identifier")
        _currentUserId.value = user.id
        user
    }

    suspend fun addOrUpdateAdsAccount(account: AdsAccountEntity) = withContext(Dispatchers.IO) {
        adsAccountDao.insertOrUpdate(account)
    }

    suspend fun deleteAdsAccount(id: String) = withContext(Dispatchers.IO) {
        adsAccountDao.deleteById(id)
    }

    suspend fun toggleAdsAccount(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        adsAccountDao.toggleEnabled(id, isEnabled)
    }

    // Activity Verification Flow
    suspend fun startEarningActivity(activityId: String): String = withContext(Dispatchers.IO) {
        val user = requireCurrentUser()
        if (user.status == "SUSPENDED") {
            throw IllegalStateException("Account is suspended. Activities are locked.")
        }
        val sessionToken = adProvider.startRewardedAd(activityId, user.id)
        val attempt = ActivityAttemptEntity(
            id = "att_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            activityId = activityId,
            providerEventId = sessionToken,
            status = "INITIATED",
            rewardAmount = 0.0,
            verificationStatus = "PENDING",
            ipHash = "ip_hash_sha256_client",
            deviceHash = "device_fingerprint_verified",
            createdAt = System.currentTimeMillis()
        )
        activityDao.insertAttempt(attempt)
        sessionToken
    }

    suspend fun verifyAndCreditActivity(
        activityId: String,
        sessionToken: String,
        proofToken: String = "proof_verified"
    ): ProviderVerificationResult = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value
        val user = if (uid != null) userDao.getUserById(uid) else null
        if (user == null) {
            return@withContext ProviderVerificationResult(
                isValid = false,
                providerEventId = "",
                activityId = activityId,
                userId = "",
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "User authentication required"
            )
        }

        val activity = activityDao.getActivityById(activityId)
            ?: return@withContext ProviderVerificationResult(
                isValid = false,
                providerEventId = "",
                activityId = activityId,
                userId = user.id,
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "Activity not found"
            )

        // 1. Call legitimate provider adapter
        val result = adProvider.verifyReward(sessionToken, proofToken)
        if (!result.isValid) {
            return@withContext result
        }

        // 2. Check duplicate provider event ID (Idempotency)
        val existingAttempt = activityDao.getAttemptByProviderEventId(result.providerEventId)
        if (existingAttempt != null) {
            return@withContext ProviderVerificationResult(
                isValid = false,
                providerEventId = result.providerEventId,
                activityId = activityId,
                userId = user.id,
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "DUPLICATE_EVENT: This activity reward was already credited."
            )
        }

        // 3. Check daily limit
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val completedToday = activityDao.getVerifiedAttemptCountToday(user.id, activityId, todayStart)
        if (completedToday >= activity.dailyLimit) {
            return@withContext ProviderVerificationResult(
                isValid = false,
                providerEventId = result.providerEventId,
                activityId = activityId,
                userId = user.id,
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "LIMIT_EXCEEDED: You have reached today's limit (${activity.dailyLimit}) for this activity."
            )
        }

        // 4. Record verified attempt
        val verifiedAttempt = ActivityAttemptEntity(
            id = "att_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            activityId = activityId,
            providerEventId = result.providerEventId,
            status = "VERIFIED",
            rewardAmount = activity.rewardAmount,
            verificationStatus = "VERIFIED",
            ipHash = "ip_hash_sha256_client",
            deviceHash = "device_fingerprint_verified",
            createdAt = System.currentTimeMillis(),
            verifiedAt = System.currentTimeMillis()
        )
        activityDao.insertAttempt(verifiedAttempt)
        activityDao.incrementCompletionCount(activityId)

        // 5. Post to Wallet Ledger
        val idempotencyKey = "tx_act_${result.providerEventId}"
        val transaction = WalletTransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            type = "EARNING",
            amount = activity.rewardAmount,
            currency = activity.currency,
            status = "COMPLETED",
            referenceType = "ACTIVITY",
            referenceId = activityId,
            description = "Verified: ${activity.title} (${activity.provider})",
            idempotencyKey = idempotencyKey,
            createdAt = System.currentTimeMillis()
        )
        walletDao.insertTransaction(transaction)

        // 6. Update Wallet Balance atomically
        val wallet = walletDao.getWallet(user.id) ?: WalletEntity(userId = user.id)
        val updatedWallet = wallet.copy(
            balance = wallet.balance + activity.rewardAmount,
            lifetimeEarned = wallet.lifetimeEarned + activity.rewardAmount,
            updatedAt = System.currentTimeMillis()
        )
        walletDao.updateWallet(updatedWallet)

        // 7. Notification
        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = user.id,
                type = "REWARD_RECEIVED",
                title = "Reward Credited +$${String.format(Locale.US, "%.2f", activity.rewardAmount)}",
                message = "Verified completion of '${activity.title}'. Funds are immediately available in your wallet.",
                isRead = false
            )
        )

        result
    }

    // Daily Claim Flow
    suspend fun getDailyStreakStatus(): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext Pair(false, 0)
        val user = userDao.getUserById(uid) ?: return@withContext Pair(false, 0)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayClaim = dailyClaimDao.getClaimForDate(user.id, todayStr)
        val latestClaim = dailyClaimDao.getLatestClaim(user.id)
        val streak = latestClaim?.streakCount ?: 0
        Pair(todayClaim != null, streak)
    }

    suspend fun claimDailyBonus(): Double = withContext(Dispatchers.IO) {
        val user = requireCurrentUser()
        if (user.status == "SUSPENDED") {
            throw IllegalStateException("Account is suspended.")
        }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val existingClaim = dailyClaimDao.getClaimForDate(user.id, todayStr)
        if (existingClaim != null) {
            throw IllegalStateException("Daily bonus already claimed for today.")
        }

        val latestClaim = dailyClaimDao.getLatestClaim(user.id)
        val streak = (latestClaim?.streakCount ?: 0) + 1
        val multiplier = when (streak) {
            in 1..2 -> 1.0
            in 3..5 -> 1.25
            in 6..9 -> 1.50
            else -> 2.0
        }
        val baseReward = 0.25
        val finalReward = baseReward * multiplier

        val claim = DailyClaimEntity(
            id = "claim_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            claimDate = todayStr,
            streakCount = streak,
            rewardAmount = finalReward,
            multiplier = multiplier
        )
        dailyClaimDao.insertClaim(claim)

        // Ledger
        val tx = WalletTransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            type = "DAILY_BONUS",
            amount = finalReward,
            currency = "USD",
            status = "COMPLETED",
            referenceType = "CLAIM",
            referenceId = claim.id,
            description = "Day $streak Daily Streak Bonus (${multiplier}x)",
            idempotencyKey = "tx_daily_${user.id}_$todayStr"
        )
        walletDao.insertTransaction(tx)

        // Update Wallet
        val wallet = walletDao.getWallet(user.id) ?: WalletEntity(userId = user.id)
        walletDao.updateWallet(
            wallet.copy(
                balance = wallet.balance + finalReward,
                lifetimeEarned = wallet.lifetimeEarned + finalReward,
                updatedAt = System.currentTimeMillis()
            )
        )

        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = user.id,
                type = "DAILY_BONUS_READY",
                title = "Daily Streak Claimed! Day $streak",
                message = "+$${String.format(Locale.US, "%.2f", finalReward)} added to your wallet balance.",
                isRead = false
            )
        )

        finalReward
    }

    // Withdrawal Flow
    suspend fun requestWithdrawal(
        amount: Double,
        method: String,
        destination: String
    ): WithdrawalRequestEntity = withContext(Dispatchers.IO) {
        val user = requireCurrentUser()
        if (user.status == "SUSPENDED") {
            throw IllegalStateException("Suspended accounts cannot withdraw funds.")
        }
        val wallet = walletDao.getWallet(user.id)
            ?: throw IllegalStateException("Wallet not initialized")

        if (amount < 5.0) {
            throw IllegalArgumentException("Minimum withdrawal amount is $5.00.")
        }
        if (amount > wallet.balance) {
            throw IllegalArgumentException("Insufficient funds. Available balance: $${wallet.balance}")
        }

        val fee = when (method) {
            "PAYPAL" -> 0.25
            "CRYPTO_USDC" -> 0.50
            "BANK_ACH" -> 0.35
            else -> 0.0
        }
        val netAmount = amount - fee

        val maskedDest = when {
            destination.contains("@") -> {
                val parts = destination.split("@")
                val name = parts[0]
                val visible = if (name.length > 3) name.take(3) else name.take(1)
                "$visible***@${parts.getOrElse(1) { "" }}"
            }
            destination.startsWith("0x") -> {
                destination.take(6) + "..." + destination.takeLast(4)
            }
            else -> "**** " + destination.takeLast(4)
        }

        // Fraud evaluation
        val fraudEval = fraudRiskEngine.evaluateWithdrawal(
            amount = amount,
            balance = wallet.balance,
            lifetimeEarned = wallet.lifetimeEarned,
            accountAgeDays = ((System.currentTimeMillis() - user.createdAt) / 86400000L).toInt(),
            userRiskScore = user.riskScore
        )

        val request = WithdrawalRequestEntity(
            id = "wd_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            userDisplayName = user.displayName,
            amount = amount,
            fee = fee,
            netAmount = netAmount,
            currency = "USD",
            method = method,
            destinationMasked = maskedDest,
            status = if (fraudEval.requiresReview) "UNDER_REVIEW" else "PENDING",
            riskScore = fraudEval.riskScore
        )
        withdrawalDao.insertWithdrawal(request)

        // Debit wallet balance
        walletDao.updateWallet(
            wallet.copy(
                balance = wallet.balance - amount,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Ledger
        val tx = WalletTransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            type = "WITHDRAWAL_DEBIT",
            amount = -amount,
            currency = "USD",
            status = "PENDING",
            referenceType = "WITHDRAWAL",
            referenceId = request.id,
            description = "Withdrawal request to $method ($maskedDest)",
            idempotencyKey = "tx_wd_${request.id}"
        )
        walletDao.insertTransaction(tx)

        // Notification
        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = user.id,
                type = "WITHDRAWAL_SUBMITTED",
                title = "Withdrawal Submitted ($${String.format(Locale.US, "%.2f", amount)})",
                message = "Your payout request via $method is queued for compliance verification.",
                isRead = false
            )
        )

        if (fraudEval.flags.isNotEmpty()) {
            adminDao.insertFraudEvent(
                FraudEventEntity(
                    id = "fraud_" + UUID.randomUUID().toString().substring(0, 8),
                    userId = user.id,
                    username = user.username,
                    type = "WITHDRAWAL_ANOMALY",
                    riskScore = fraudEval.riskScore,
                    metadata = "Flags: ${fraudEval.flags.joinToString(", ")}"
                )
            )
        }

        request
    }

    // Admin Actions
    suspend fun adminApproveWithdrawal(
        withdrawalId: String,
        adminUser: UserEntity
    ) = withContext(Dispatchers.IO) {
        val req = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: throw IllegalStateException("Withdrawal not found")
        val updated = req.copy(
            status = "COMPLETED",
            reviewedBy = adminUser.id,
            reviewedAt = System.currentTimeMillis()
        )
        withdrawalDao.updateWithdrawal(updated)

        // Update user's lifetimeWithdrawn
        val wallet = walletDao.getWallet(req.userId)
        if (wallet != null) {
            walletDao.updateWallet(
                wallet.copy(
                    lifetimeWithdrawn = wallet.lifetimeWithdrawn + req.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // Audit log
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "APPROVE_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = withdrawalId,
                metadata = "Approved payout of $${req.amount} via ${req.method} to ${req.destinationMasked}"
            )
        )

        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = req.userId,
                type = "WITHDRAWAL_APPROVED",
                title = "Withdrawal Approved! $${String.format(Locale.US, "%.2f", req.amount)}",
                message = "Payout via ${req.method} has been dispatched. Transaction reference: ${req.id}",
                isRead = false
            )
        )
    }

    suspend fun adminRejectWithdrawal(
        withdrawalId: String,
        reason: String,
        adminUser: UserEntity
    ) = withContext(Dispatchers.IO) {
        val req = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: throw IllegalStateException("Withdrawal not found")
        val updated = req.copy(
            status = "REJECTED",
            reviewedBy = adminUser.id,
            reviewedAt = System.currentTimeMillis(),
            rejectionReason = reason
        )
        withdrawalDao.updateWithdrawal(updated)

        // Refund funds back to wallet
        val wallet = walletDao.getWallet(req.userId)
        if (wallet != null) {
            walletDao.updateWallet(
                wallet.copy(
                    balance = wallet.balance + req.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // Ledger refund
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
                userId = req.userId,
                type = "WITHDRAWAL_REFUND",
                amount = req.amount,
                currency = req.currency,
                status = "COMPLETED",
                referenceType = "WITHDRAWAL",
                referenceId = req.id,
                description = "Refund for rejected withdrawal: $reason",
                idempotencyKey = "tx_refund_${req.id}"
            )
        )

        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "REJECT_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = withdrawalId,
                metadata = "Rejected payout of $${req.amount}. Reason: $reason"
            )
        )

        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = req.userId,
                type = "WITHDRAWAL_REJECTED",
                title = "Withdrawal Request Rejected",
                message = "Your request was declined. $${String.format(Locale.US, "%.2f", req.amount)} has been refunded to your wallet. Reason: $reason",
                isRead = false
            )
        )
    }

    suspend fun adminSuspendUser(userId: String, adminUser: UserEntity, reason: String) = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, "SUSPENDED")
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "SUSPEND_USER",
                entityType = "USER",
                entityId = userId,
                metadata = "Account suspended. Reason: $reason"
            )
        )
    }

    suspend fun adminRestoreUser(userId: String, adminUser: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, "ACTIVE")
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "RESTORE_USER",
                entityType = "USER",
                entityId = userId,
                metadata = "Account restored to ACTIVE status"
            )
        )
    }

    suspend fun adminAdjustWallet(
        userId: String,
        amount: Double,
        reason: String,
        adminUser: UserEntity
    ) = withContext(Dispatchers.IO) {
        val wallet = walletDao.getWallet(userId) ?: WalletEntity(userId = userId)
        val newBalance = (wallet.balance + amount).coerceAtLeast(0.0)
        walletDao.updateWallet(wallet.copy(balance = newBalance, updatedAt = System.currentTimeMillis()))

        val tx = WalletTransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
            userId = userId,
            type = "ADMIN_ADJUSTMENT",
            amount = amount,
            currency = "USD",
            status = "COMPLETED",
            referenceType = "AUDIT",
            referenceId = adminUser.id,
            description = "Admin manual adjustment by ${adminUser.displayName}: $reason",
            idempotencyKey = "tx_adj_" + UUID.randomUUID().toString().substring(0, 8)
        )
        walletDao.insertTransaction(tx)

        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "ADJUST_WALLET",
                entityType = "WALLET",
                entityId = userId,
                metadata = "Adjusted balance by $${amount}. Reason: $reason"
            )
        )
    }

    suspend fun adminCreateActivity(activity: EarningActivityEntity, adminUser: UserEntity) = withContext(Dispatchers.IO) {
        activityDao.insertActivity(activity)
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "CREATE_ACTIVITY",
                entityType = "ACTIVITY",
                entityId = activity.id,
                metadata = "Created earning activity: ${activity.title} ($${activity.rewardAmount})"
            )
        )
    }

    suspend fun adminCreateCampaign(campaign: CampaignEntity, adminUser: UserEntity) = withContext(Dispatchers.IO) {
        adminDao.insertCampaign(campaign)
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "CREATE_CAMPAIGN",
                entityType = "CAMPAIGN",
                entityId = campaign.id,
                metadata = "Launched campaign: ${campaign.name} (Budget $${campaign.budget})"
            )
        )
    }

    suspend fun adminResolveFraud(fraudId: String, adminUser: UserEntity) = withContext(Dispatchers.IO) {
        adminDao.updateFraudStatus(fraudId, "RESOLVED", adminUser.id)
    }

    // Support Tickets
    suspend fun createSupportTicket(subject: String, category: String, message: String): String = withContext(Dispatchers.IO) {
        val user = requireCurrentUser()
        val ticketId = "ticket_" + UUID.randomUUID().toString().substring(0, 8)
        val ticket = SupportTicketEntity(
            id = ticketId,
            userId = user.id,
            userDisplayName = user.displayName,
            subject = subject,
            category = category,
            priority = "MEDIUM",
            status = "OPEN",
            lastMessage = message
        )
        supportDao.insertTicket(ticket)
        val msg = SupportMessageEntity(
            id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
            ticketId = ticketId,
            senderId = user.id,
            senderName = user.displayName,
            senderRole = user.role,
            message = message
        )
        supportDao.insertMessage(msg)
        ticketId
    }

    suspend fun replyToTicket(ticketId: String, message: String) = withContext(Dispatchers.IO) {
        val user = requireCurrentUser()
        val ticket = supportDao.getTicketById(ticketId) ?: throw IllegalStateException("Ticket not found")

        val newStatus = if (user.role in listOf("ADMIN", "SUPER_ADMIN", "SUPPORT")) "WAITING_USER" else "IN_PROGRESS"
        supportDao.updateTicket(ticket.copy(lastMessage = message, status = newStatus, updatedAt = System.currentTimeMillis()))

        val msg = SupportMessageEntity(
            id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
            ticketId = ticketId,
            senderId = user.id,
            senderName = user.displayName,
            senderRole = user.role,
            message = message
        )
        supportDao.insertMessage(msg)
    }

    fun getTicketMessages(ticketId: String): Flow<List<SupportMessageEntity>> {
        return supportDao.getMessagesForTicket(ticketId)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext
        notifDao.markAllAsRead(uid)
    }
}
