package com.example.domain.engine

enum class KycTier(
    val level: Int,
    val title: String,
    val maxPerWithdrawal: Double,
    val maxLifetimeWithdrawal: Double,
    val requiresIdDocument: Boolean
) {
    TIER_0(
        level = 0,
        title = "Basic (Unverified)",
        maxPerWithdrawal = 10.0,
        maxLifetimeWithdrawal = 25.0,
        requiresIdDocument = false
    ),
    TIER_1(
        level = 1,
        title = "Verified Email & Phone",
        maxPerWithdrawal = 50.0,
        maxLifetimeWithdrawal = 250.0,
        requiresIdDocument = false
    ),
    TIER_2(
        level = 2,
        title = "Fully KYC Verified",
        maxPerWithdrawal = 5000.0,
        maxLifetimeWithdrawal = 50000.0,
        requiresIdDocument = true
    );

    companion object {
        fun fromLevel(level: Int): KycTier {
            return when (level) {
                2 -> TIER_2
                1 -> TIER_1
                else -> TIER_0
            }
        }
    }
}

data class ComplianceEvaluation(
    val isCompliant: Boolean,
    val kycTier: KycTier,
    val requiredNextTier: KycTier?,
    val issues: List<String>,
    val actionRequired: String?
)

class ComplianceKycEngine {

    fun evaluateWithdrawalCompliance(
        kycLevel: Int,
        requestedAmount: Double,
        lifetimeWithdrawn: Double,
        isEmailVerified: Boolean,
        payoutAccount: String,
        usedPayoutAccountsBlacklist: Set<String>
    ): ComplianceEvaluation {
        val tier = KycTier.fromLevel(kycLevel)
        val issues = mutableListOf<String>()
        var actionRequired: String? = null
        var requiredNextTier: KycTier? = null

        if (usedPayoutAccountsBlacklist.contains(payoutAccount.trim().lowercase())) {
            issues.add("PAYOUT_DESTINATION_FLAGGED_OR_DUPLICATE")
            actionRequired = "Payout destination is associated with high-risk or duplicate activity."
        }

        if (!isEmailVerified) {
            issues.add("EMAIL_NOT_VERIFIED")
            actionRequired = "Please verify your email address to initiate withdrawals."
        }

        if (requestedAmount > tier.maxPerWithdrawal) {
            issues.add("REQUEST_EXCEEDS_TIER_LIMIT ($$requestedAmount > $${tier.maxPerWithdrawal})")
            requiredNextTier = if (tier == KycTier.TIER_0) KycTier.TIER_1 else KycTier.TIER_2
            actionRequired = "Upgrade to ${requiredNextTier.title} to withdraw amounts above $${tier.maxPerWithdrawal}."
        }

        if ((lifetimeWithdrawn + requestedAmount) > tier.maxLifetimeWithdrawal) {
            issues.add("CUMULATIVE_LIMIT_EXCEEDED")
            requiredNextTier = if (tier == KycTier.TIER_0) KycTier.TIER_1 else KycTier.TIER_2
            actionRequired = "You have reached your cumulative withdrawal threshold. Please complete identity verification."
        }

        return ComplianceEvaluation(
            isCompliant = issues.isEmpty(),
            kycTier = tier,
            requiredNextTier = requiredNextTier,
            issues = issues,
            actionRequired = actionRequired
        )
    }
}
