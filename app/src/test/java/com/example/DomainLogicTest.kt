package com.example

import com.example.domain.engine.*
import com.example.domain.model.Permission
import com.example.domain.model.Role
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DomainLogicTest {

    @Test
    fun rbacPolicyEngine_verifies_role_permissions() {
        val rbac = RbacPolicyEngine()

        // Super Admin has all permissions
        assertTrue(rbac.hasPermission(Role.SUPER_ADMIN, Permission.USERS_READ))
        assertTrue(rbac.hasPermission(Role.SUPER_ADMIN, Permission.WITHDRAWALS_APPROVE))
        assertTrue(rbac.hasPermission(Role.SUPER_ADMIN, Permission.SETTINGS_UPDATE))

        // Support role can read and reply to tickets, but cannot adjust balances or approve withdrawals
        assertTrue(rbac.hasPermission(Role.SUPPORT, Permission.SUPPORT_REPLY))
        assertFalse(rbac.hasPermission(Role.SUPPORT, Permission.REWARDS_ADJUST))
        assertFalse(rbac.hasPermission(Role.SUPPORT, Permission.WITHDRAWALS_APPROVE))

        // Finance role can review and approve withdrawals
        assertTrue(rbac.hasPermission(Role.FINANCE, Permission.WITHDRAWALS_APPROVE))
        assertFalse(rbac.hasPermission(Role.FINANCE, Permission.USERS_SUSPEND))
    }

    @Test
    fun walletLedgerService_calculates_credit_with_idempotency_and_audit_hash() {
        val ledger = WalletLedgerService()
        val result = ledger.calculateActivityCredit(
            currentBalance = 10.50,
            lifetimeEarned = 25.00,
            rewardAmount = 1.25,
            activityId = "act_ad_01",
            providerEventId = "evt_998811"
        )

        assertEquals(11.75, result.newBalance, 0.001)
        assertEquals(26.25, result.newLifetimeEarned, 0.001)
        assertEquals("tx_act_evt_998811", result.idempotencyKey)
        assertNotNull(result.auditHash)
        assertTrue(result.auditHash.isNotEmpty())
    }

    @Test
    fun dailyStreakEngine_evaluates_consecutive_and_reset_streaks() {
        val streakEngine = DailyStreakEngine()
        val cal = Calendar.getInstance()

        // Claimed yesterday -> streak increments
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        val consecutiveEval = streakEngine.evaluateClaim(
            lastClaimDateStr = yesterdayStr,
            currentStreakCount = 3
        )
        assertEquals(4, consecutiveEval.newStreak)
        assertTrue(consecutiveEval.multiplier >= 1.0)
        assertTrue(consecutiveEval.finalReward > 0.0)

        // Claimed 3 days ago -> missed days -> streak resets to 1
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val threeDaysAgoStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        val brokenStreakEval = streakEngine.evaluateClaim(
            lastClaimDateStr = threeDaysAgoStr,
            currentStreakCount = 5
        )
        assertEquals(1, brokenStreakEval.newStreak)
        assertEquals(1.0, brokenStreakEval.multiplier, 0.001)
    }

    @Test
    fun referralDomainEngine_evaluates_tiers_and_commissions() {
        val referralEngine = ReferralDomainEngine()

        // 6 referrals should put user into Silver tier
        val progress = referralEngine.evaluateTier(referralCount = 6, totalEarned = 12.50)
        assertEquals(ReferralTier.SILVER, progress.currentTier)
        assertEquals(ReferralTier.GOLD, progress.nextTier)
        assertEquals(4, progress.referralsToNextTier) // 10 - 6 = 4

        // Commission on $2.00 reward at Silver tier (7.5%)
        val commission = referralEngine.evaluateCommission(
            activityRewardAmount = 2.00,
            referrerTier = ReferralTier.SILVER
        )
        assertEquals(0.15, commission, 0.001)

        // Self-referral attempt should be rejected
        val selfReferralCheck = referralEngine.evaluateQualification(
            referrerId = "user_123",
            refereeId = "user_123",
            refereeActivitiesCompleted = 5,
            isEmailVerified = true,
            hasSharedDeviceFingerprint = false
        )
        assertFalse(selfReferralCheck.isQualified)
        assertEquals("SELF_REFERRAL_FORBIDDEN", selfReferralCheck.rejectionReason)
    }

    @Test
    fun campaignBudgetEngine_evaluates_reservation_and_depletion() {
        val campaignEngine = CampaignBudgetEngine()

        // Normal active campaign reservation
        val reservation = campaignEngine.evaluateClaimEligibility(
            campaignStatus = CampaignStatus.ACTIVE,
            totalBudget = 100.0,
            spentBudget = 99.50,
            unitReward = 0.50,
            userPriorClaims = 0,
            maxPerUserClaims = 1,
            campaignExpiryTimestampMs = System.currentTimeMillis() + 86400000L
        )

        assertTrue(reservation.isAllowed)
        assertEquals(0.50, reservation.reservedReward, 0.001)
        assertEquals(0.0, reservation.remainingBudget, 0.001)
        assertEquals(CampaignStatus.DEPLETED, reservation.newCampaignStatus)

        // User cap reached should reject
        val cappedReservation = campaignEngine.evaluateClaimEligibility(
            campaignStatus = CampaignStatus.ACTIVE,
            totalBudget = 100.0,
            spentBudget = 10.0,
            unitReward = 0.50,
            userPriorClaims = 2,
            maxPerUserClaims = 2,
            campaignExpiryTimestampMs = System.currentTimeMillis() + 86400000L
        )
        assertFalse(cappedReservation.isAllowed)
    }

    @Test
    fun payoutCalculationEngine_computes_fees_and_limits() {
        val payoutEngine = PayoutCalculationEngine()

        // PayPal fee test ($20 gross) -> Flat $0.25 + 1.5% ($0.30) = $0.55 fee -> Net $19.45
        val paypalResult = payoutEngine.calculatePayout(
            method = PayoutMethod.PAYPAL,
            grossAmount = 20.0,
            availableBalance = 50.0
        )
        assertTrue(paypalResult.isValid)
        assertEquals(0.55, paypalResult.totalFee, 0.001)
        assertEquals(19.45, paypalResult.netAmount, 0.001)

        // Under minimum threshold should reject
        val underMinResult = payoutEngine.calculatePayout(
            method = PayoutMethod.BANK_TRANSFER,
            grossAmount = 15.0, // Bank minimum is $25
            availableBalance = 50.0
        )
        assertFalse(underMinResult.isValid)
        assertTrue(underMinResult.errorMessage!!.contains("Minimum withdrawal"))
    }

    @Test
    fun activityVerificationEngine_rejects_sub_duration_and_accepts_valid_payload() {
        val verificationEngine = ActivityVerificationEngine()

        // Reject if duration is too short (e.g. 5s for 30s ad)
        val shortDurationResult = verificationEngine.verifyProviderCallback(
            ActivityVerificationPayload(
                eventId = "evt_001",
                userId = "usr_001",
                activityId = "act_ad_30s",
                rewardAmount = 0.50,
                durationSeconds = 5,
                minRequiredDurationSeconds = 30,
                timestampMs = System.currentTimeMillis(),
                signature = "sig_valid_001"
            )
        )
        assertFalse(shortDurationResult.isValid)
        assertEquals("ERR_DURATION_TOO_SHORT", shortDurationResult.errorCode)

        // Accept valid payload with proper duration
        val validResult = verificationEngine.verifyProviderCallback(
            ActivityVerificationPayload(
                eventId = "evt_002",
                userId = "usr_001",
                activityId = "act_ad_30s",
                rewardAmount = 0.50,
                durationSeconds = 32,
                minRequiredDurationSeconds = 30,
                timestampMs = System.currentTimeMillis(),
                signature = "sig_valid_002"
            )
        )
        assertTrue(validResult.isValid)
        assertEquals(0.50, validResult.verifiedAmount, 0.001)
        assertNotNull(validResult.auditHash)
    }
}
