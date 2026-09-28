package com.example.domain.engine

import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.util.UUID

/**
 * Local Development & Testing Provider.
 * Simulates genuine server-side verification, timing constraints,
 * and cryptographic proof signatures. Clearly marked as Development Mode.
 */
class MockRewardProvider(
    override val isDevelopmentMode: Boolean = true
) : AdProviderAdapter {

    override val providerId: String = "mock_verified_provider_v1"
    override val providerDisplayName: String = "Rewardly Verified Sandbox Mediation"

    private val activeSessions = mutableMapOf<String, SessionMeta>()

    private data class SessionMeta(
        val sessionId: String,
        val activityId: String,
        val userId: String,
        val rewardAmount: Double,
        val startedAt: Long,
        val requiredDurationSeconds: Int
    )

    override suspend fun initialize(): Boolean {
        return true
    }

    override suspend fun getAvailableAds(): List<AdPlacement> {
        return listOf(
            AdPlacement(
                placementId = "placement_admob_video_01",
                title = "Verified Rewarded Video Ad",
                description = "Watch interactive sponsor ad with server-side validation callback.",
                rewardAmount = 0.50,
                estimatedDurationSeconds = 15,
                providerName = providerDisplayName
            ),
            AdPlacement(
                placementId = "placement_fintech_survey_02",
                title = "Fintech Consumer Survey",
                description = "Complete accredited demographic verification survey.",
                rewardAmount = 1.25,
                estimatedDurationSeconds = 30,
                providerName = "TheoremReach Verified"
            ),
            AdPlacement(
                placementId = "placement_partner_offer_03",
                title = "Digital Banking Partner Verification",
                description = "Verify account creation milestone with partner SDK.",
                rewardAmount = 3.50,
                estimatedDurationSeconds = 45,
                providerName = "TapJoy Verified"
            )
        )
    }

    override suspend fun startRewardedAd(activityId: String, userId: String): String {
        val sessionToken = "ssv_session_" + UUID.randomUUID().toString()
        val duration = when {
            activityId.contains("survey", ignoreCase = true) -> 12
            activityId.contains("offer", ignoreCase = true) -> 15
            else -> 8
        }
        val reward = when {
            activityId.contains("survey", ignoreCase = true) -> 1.25
            activityId.contains("offer", ignoreCase = true) -> 3.50
            else -> 0.50
        }

        activeSessions[sessionToken] = SessionMeta(
            sessionId = sessionToken,
            activityId = activityId,
            userId = userId,
            rewardAmount = reward,
            startedAt = System.currentTimeMillis(),
            requiredDurationSeconds = duration
        )
        return sessionToken
    }

    override suspend fun verifyReward(sessionToken: String, proofToken: String): ProviderVerificationResult {
        val session = activeSessions[sessionToken]
            ?: return ProviderVerificationResult(
                isValid = false,
                providerEventId = "",
                activityId = "",
                userId = "",
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "INVALID_SESSION: Session token expired or does not exist."
            )

        // Strict verification: Minimum elapsed duration check
        val elapsedSeconds = (System.currentTimeMillis() - session.startedAt) / 1000
        if (elapsedSeconds < session.requiredDurationSeconds - 2) {
            return ProviderVerificationResult(
                isValid = false,
                providerEventId = "",
                activityId = session.activityId,
                userId = session.userId,
                verifiedRewardAmount = 0.0,
                signature = "",
                failureReason = "ABUSE_DETECTED: Completion timestamp too fast ($elapsedSeconds s vs required ${session.requiredDurationSeconds} s)."
            )
        }

        // Generate cryptographic proof signature (simulates provider HMAC-SHA256)
        val providerEventId = "evt_" + UUID.randomUUID().toString().substring(0, 16)
        val rawSign = "${session.userId}:${session.activityId}:$providerEventId:${session.rewardAmount}"
        val signature = sha256(rawSign)

        // Remove active session to ensure one-time verification
        activeSessions.remove(sessionToken)

        return ProviderVerificationResult(
            isValid = true,
            providerEventId = providerEventId,
            activityId = session.activityId,
            userId = session.userId,
            verifiedRewardAmount = session.rewardAmount,
            signature = signature,
            failureReason = null,
            completionTimestamp = System.currentTimeMillis()
        )
    }

    override suspend fun handleWebhook(
        payload: Map<String, String>,
        signature: String
    ): ProviderVerificationResult {
        val userId = payload["user_id"] ?: return invalidWebhook("Missing user_id")
        val activityId = payload["activity_id"] ?: return invalidWebhook("Missing activity_id")
        val eventId = payload["event_id"] ?: return invalidWebhook("Missing event_id")
        val amount = payload["amount"]?.toDoubleOrNull() ?: 0.0

        // Verify HMAC
        val expectedSign = sha256("$userId:$activityId:$eventId:$amount")
        if (expectedSign != signature) {
            return invalidWebhook("Signature verification failed. Invalid payload key.")
        }

        return ProviderVerificationResult(
            isValid = true,
            providerEventId = eventId,
            activityId = activityId,
            userId = userId,
            verifiedRewardAmount = amount,
            signature = signature
        )
    }

    private fun invalidWebhook(reason: String) = ProviderVerificationResult(
        isValid = false,
        providerEventId = "",
        activityId = "",
        userId = "",
        verifiedRewardAmount = 0.0,
        signature = "",
        failureReason = reason
    )

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
