package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_claims")
data class DailyClaimEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val claimDate: String, // YYYY-MM-DD
    val streakCount: Int = 1,
    val rewardAmount: Double = 0.25,
    val multiplier: Double = 1.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String,
    val referrerId: String,
    val referredUserId: String,
    val referredUsername: String,
    val status: String = "REGISTERED", // REGISTERED, VERIFIED_EARNER, REWARDED
    val rewardAmount: Double = 2.00,
    val milestoneRequirement: String = "Earn $5.00 from verified activities",
    val milestoneProgress: Int = 20, // 0 to 100 percentage
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userDisplayName: String = "User",
    val amount: Double,
    val fee: Double = 0.0,
    val netAmount: Double = amount,
    val currency: String = "USD",
    val method: String, // PAYPAL, CRYPTO_USDC, BANK_ACH, AMAZON_CARD
    val destinationMasked: String,
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, APPROVED, PROCESSING, COMPLETED, REJECTED, CANCELLED
    val riskScore: Int = 15,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // REWARD_RECEIVED, WITHDRAWAL_UPDATE, DAILY_BONUS_READY, REFERRAL_VERIFIED, SECURITY_ALERT, ANNOUNCEMENT
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userDisplayName: String,
    val subject: String,
    val category: String, // WITHDRAWAL, ACTIVITY_ISSUE, ACCOUNT, BILLING, OTHER
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val status: String = "OPEN", // OPEN, IN_PROGRESS, WAITING_USER, RESOLVED, CLOSED
    val lastMessage: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // USER, ADMIN, SUPPORT
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fraud_events")
data class FraudEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val type: String, // RAPID_COMPLETIONS, SUSPICIOUS_IP, DEVICE_MISMATCH, DUPLICATE_EVENT_ATTEMPT, WITHDRAWAL_ANOMALY
    val riskScore: Int,
    val metadata: String,
    val status: String = "FLAGGED", // FLAGGED, UNDER_REVIEW, RESOLVED, DISMISSED
    val reviewedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
    @PrimaryKey val id: String,
    val adminId: String,
    val adminName: String,
    val action: String, // APPROVE_WITHDRAWAL, REJECT_WITHDRAWAL, SUSPEND_USER, RESTORE_USER, ADJUST_WALLET, CREATE_ACTIVITY, UPDATE_CAMPAIGN, REVIEW_FRAUD
    val entityType: String,
    val entityId: String,
    val metadata: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val budget: Double,
    val budgetSpent: Double = 0.0,
    val rewardPerCompletion: Double,
    val targetAudience: String = "All Verified Users",
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + 86400000L * 14,
    val completionsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ads_accounts")
data class AdsAccountEntity(
    @PrimaryKey val id: String,
    val networkName: String, // Google AdMob, Unity Ads, AppLovin MAX, Start.io, IronSource, Custom
    val accountLabel: String,
    val appId: String,
    val rewardedUnitId: String,
    val interstitialUnitId: String = "",
    val bannerUnitId: String = "",
    val rewardPerAd: Double = 0.50,
    val isEnabled: Boolean = true,
    val testMode: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

