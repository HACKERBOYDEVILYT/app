package com.example.domain.engine

enum class CampaignStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    DEPLETED,
    EXPIRED,
    CANCELLED
}

data class CampaignReservationResult(
    val isAllowed: Boolean,
    val reservedReward: Double,
    val remainingBudget: Double,
    val newCampaignStatus: CampaignStatus,
    val rejectionReason: String? = null
)

data class CampaignPacingStatus(
    val totalBudget: Double,
    val spentBudget: Double,
    val percentageUtilized: Float,
    val remainingBudget: Double,
    val hourlyClaimVelocity: Int,
    val isPacingThrottled: Boolean
)

class CampaignBudgetEngine {

    fun evaluateClaimEligibility(
        campaignStatus: CampaignStatus,
        totalBudget: Double,
        spentBudget: Double,
        unitReward: Double,
        userPriorClaims: Int,
        maxPerUserClaims: Int,
        campaignExpiryTimestampMs: Long,
        currentTimeMs: Long = System.currentTimeMillis()
    ): CampaignReservationResult {
        if (campaignStatus != CampaignStatus.ACTIVE) {
            return CampaignReservationResult(
                isAllowed = false,
                reservedReward = 0.0,
                remainingBudget = (totalBudget - spentBudget).coerceAtLeast(0.0),
                newCampaignStatus = campaignStatus,
                rejectionReason = "CAMPAIGN_NOT_ACTIVE (Status: $campaignStatus)"
            )
        }

        if (currentTimeMs >= campaignExpiryTimestampMs && campaignExpiryTimestampMs > 0) {
            return CampaignReservationResult(
                isAllowed = false,
                reservedReward = 0.0,
                remainingBudget = (totalBudget - spentBudget).coerceAtLeast(0.0),
                newCampaignStatus = CampaignStatus.EXPIRED,
                rejectionReason = "CAMPAIGN_EXPIRED"
            )
        }

        if (userPriorClaims >= maxPerUserClaims) {
            return CampaignReservationResult(
                isAllowed = false,
                reservedReward = 0.0,
                remainingBudget = (totalBudget - spentBudget).coerceAtLeast(0.0),
                newCampaignStatus = campaignStatus,
                rejectionReason = "USER_PER_CAMPAIGN_CAP_REACHED ($userPriorClaims / $maxPerUserClaims)"
            )
        }

        val availableBudget = totalBudget - spentBudget
        if (availableBudget < unitReward) {
            return CampaignReservationResult(
                isAllowed = false,
                reservedReward = 0.0,
                remainingBudget = availableBudget.coerceAtLeast(0.0),
                newCampaignStatus = CampaignStatus.DEPLETED,
                rejectionReason = "CAMPAIGN_BUDGET_DEPLETED"
            )
        }

        val updatedRemaining = availableBudget - unitReward
        val nextStatus = if (updatedRemaining < unitReward) CampaignStatus.DEPLETED else CampaignStatus.ACTIVE

        return CampaignReservationResult(
            isAllowed = true,
            reservedReward = unitReward,
            remainingBudget = Math.round(updatedRemaining * 100.0) / 100.0,
            newCampaignStatus = nextStatus,
            rejectionReason = null
        )
    }

    fun computePacing(
        totalBudget: Double,
        spentBudget: Double,
        claimsLastHour: Int,
        hourlyVelocityMax: Int = 100
    ): CampaignPacingStatus {
        val remaining = (totalBudget - spentBudget).coerceAtLeast(0.0)
        val percent = if (totalBudget > 0) ((spentBudget / totalBudget).toFloat()).coerceIn(0f, 1f) else 1f
        val isThrottled = claimsLastHour >= hourlyVelocityMax

        return CampaignPacingStatus(
            totalBudget = totalBudget,
            spentBudget = spentBudget,
            percentageUtilized = percent,
            remainingBudget = remaining,
            hourlyClaimVelocity = claimsLastHour,
            isPacingThrottled = isThrottled
        )
    }
}
