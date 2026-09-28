package com.example.domain.engine

/**
 * Result data from a provider-verified activity completion.
 */
data class ProviderVerificationResult(
    val isValid: Boolean,
    val providerEventId: String,
    val activityId: String,
    val userId: String,
    val verifiedRewardAmount: Double,
    val signature: String,
    val failureReason: String? = null,
    val completionTimestamp: Long = System.currentTimeMillis()
)

data class AdPlacement(
    val placementId: String,
    val title: String,
    val description: String,
    val rewardAmount: Double,
    val estimatedDurationSeconds: Int,
    val providerName: String
)

/**
 * Standard abstraction adapter for legitimate rewarded ad networks
 * (e.g., Google AdMob SSV, Unity Ads, AppLovin MAX, Ironsource).
 */
interface AdProviderAdapter {
    val providerId: String
    val providerDisplayName: String
    val isDevelopmentMode: Boolean

    suspend fun initialize(): Boolean
    suspend fun getAvailableAds(): List<AdPlacement>
    suspend fun startRewardedAd(activityId: String, userId: String): String // Returns session token
    suspend fun verifyReward(sessionToken: String, proofToken: String): ProviderVerificationResult
    suspend fun handleWebhook(payload: Map<String, String>, signature: String): ProviderVerificationResult
}
