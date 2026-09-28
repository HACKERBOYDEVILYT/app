package com.example.domain.engine

enum class ReferralTier(
    val tierName: String,
    val minReferrals: Int,
    val commissionPercent: Double,
    val bonusAmount: Double,
    val badgeLabel: String
) {
    BRONZE("Bronze", 0, 5.0, 0.0, "Bronze Ambassador"),
    SILVER("Silver", 5, 7.5, 2.0, "Silver Ambassador"),
    GOLD("Gold", 10, 10.0, 5.0, "Gold Ambassador"),
    PLATINUM("Platinum", 25, 12.5, 15.0, "Platinum VIP"),
    DIAMOND("Diamond", 50, 15.0, 35.0, "Diamond Partner");

    companion object {
        fun fromReferralCount(count: Int): ReferralTier {
            return when {
                count >= DIAMOND.minReferrals -> DIAMOND
                count >= PLATINUM.minReferrals -> PLATINUM
                count >= GOLD.minReferrals -> GOLD
                count >= SILVER.minReferrals -> SILVER
                else -> BRONZE
            }
        }

        fun nextTier(current: ReferralTier): ReferralTier? {
            return when (current) {
                BRONZE -> SILVER
                SILVER -> GOLD
                GOLD -> PLATINUM
                PLATINUM -> DIAMOND
                DIAMOND -> null
            }
        }
    }
}

data class ReferralProgressEvaluation(
    val activeReferrals: Int,
    val currentTier: ReferralTier,
    val nextTier: ReferralTier?,
    val referralsToNextTier: Int,
    val progressToNextTier: Float,
    val totalCommissionEarned: Double,
    val pendingBonusUnlock: Double
)

data class ReferralQualificationResult(
    val isQualified: Boolean,
    val referrerBonusAmount: Double,
    val refereeWelcomeBonusAmount: Double,
    val rejectionReason: String? = null
)

class ReferralDomainEngine {

    companion object {
        const val REFERRER_SIGNUP_BONUS = 0.50
        const val REFEREE_WELCOME_BONUS = 0.50
        const val MIN_REFEREE_ACTIVITIES_FOR_UNLOCK = 1
    }

    fun evaluateTier(referralCount: Int, totalEarned: Double): ReferralProgressEvaluation {
        val currentTier = ReferralTier.fromReferralCount(referralCount)
        val nextTier = ReferralTier.nextTier(currentTier)

        val referralsToNext = if (nextTier != null) {
            (nextTier.minReferrals - referralCount).coerceAtLeast(0)
        } else {
            0
        }

        val progress = if (nextTier != null) {
            val tierRange = (nextTier.minReferrals - currentTier.minReferrals).toFloat()
            val currentProgress = (referralCount - currentTier.minReferrals).toFloat()
            if (tierRange > 0) (currentProgress / tierRange).coerceIn(0f, 1f) else 1f
        } else {
            1f
        }

        return ReferralProgressEvaluation(
            activeReferrals = referralCount,
            currentTier = currentTier,
            nextTier = nextTier,
            referralsToNextTier = referralsToNext,
            progressToNextTier = progress,
            totalCommissionEarned = totalEarned,
            pendingBonusUnlock = nextTier?.bonusAmount ?: 0.0
        )
    }

    fun evaluateCommission(
        activityRewardAmount: Double,
        referrerTier: ReferralTier
    ): Double {
        if (activityRewardAmount <= 0.0) return 0.0
        val commission = (activityRewardAmount * (referrerTier.commissionPercent / 100.0))
        return Math.round(commission * 1000.0) / 1000.0
    }

    fun evaluateQualification(
        referrerId: String,
        refereeId: String,
        refereeActivitiesCompleted: Int,
        isEmailVerified: Boolean,
        hasSharedDeviceFingerprint: Boolean
    ): ReferralQualificationResult {
        if (referrerId == refereeId) {
            return ReferralQualificationResult(
                isQualified = false,
                referrerBonusAmount = 0.0,
                refereeWelcomeBonusAmount = 0.0,
                rejectionReason = "SELF_REFERRAL_FORBIDDEN"
            )
        }

        if (hasSharedDeviceFingerprint) {
            return ReferralQualificationResult(
                isQualified = false,
                referrerBonusAmount = 0.0,
                refereeWelcomeBonusAmount = 0.0,
                rejectionReason = "DEVICE_FINGERPRINT_DUPLICATE_SUSPECTED"
            )
        }

        if (!isEmailVerified) {
            return ReferralQualificationResult(
                isQualified = false,
                referrerBonusAmount = 0.0,
                refereeWelcomeBonusAmount = 0.0,
                rejectionReason = "REFEREE_EMAIL_NOT_VERIFIED"
            )
        }

        if (refereeActivitiesCompleted < MIN_REFEREE_ACTIVITIES_FOR_UNLOCK) {
            return ReferralQualificationResult(
                isQualified = false,
                referrerBonusAmount = 0.0,
                refereeWelcomeBonusAmount = 0.0,
                rejectionReason = "REFEREE_MINIMUM_ACTIVITY_NOT_MET"
            )
        }

        return ReferralQualificationResult(
            isQualified = true,
            referrerBonusAmount = REFERRER_SIGNUP_BONUS,
            refereeWelcomeBonusAmount = REFEREE_WELCOME_BONUS,
            rejectionReason = null
        )
    }
}
