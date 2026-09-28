package com.example.domain.engine

import java.security.MessageDigest
import java.util.UUID

data class LedgerCalculationResult(
    val currentBalance: Double,
    val newBalance: Double,
    val deltaAmount: Double,
    val newLifetimeEarned: Double,
    val newLifetimeWithdrawn: Double,
    val idempotencyKey: String,
    val auditHash: String
)

class WalletLedgerService {

    fun calculateActivityCredit(
        currentBalance: Double,
        lifetimeEarned: Double,
        rewardAmount: Double,
        activityId: String,
        providerEventId: String
    ): LedgerCalculationResult {
        require(rewardAmount > 0.0) { "Reward credit amount must be strictly positive" }
        require(activityId.isNotBlank()) { "Activity ID cannot be blank" }
        require(providerEventId.isNotBlank()) { "Provider event ID cannot be blank" }

        val newBalance = currentBalance + rewardAmount
        val newLifetime = lifetimeEarned + rewardAmount
        val idempotencyKey = "tx_act_${providerEventId}"
        val auditHash = generateLedgerHash("CREDIT", rewardAmount, idempotencyKey, newBalance)

        return LedgerCalculationResult(
            currentBalance = currentBalance,
            newBalance = newBalance,
            deltaAmount = rewardAmount,
            newLifetimeEarned = newLifetime,
            newLifetimeWithdrawn = 0.0,
            idempotencyKey = idempotencyKey,
            auditHash = auditHash
        )
    }

    fun calculateWithdrawalDebit(
        currentBalance: Double,
        requestedAmount: Double,
        withdrawalId: String
    ): LedgerCalculationResult {
        require(requestedAmount >= 5.0) { "Minimum withdrawal threshold is $5.00" }
        require(requestedAmount <= currentBalance) { "Insufficient funds: balance $$currentBalance is less than requested $$requestedAmount" }

        val newBalance = currentBalance - requestedAmount
        val idempotencyKey = "tx_wd_${withdrawalId}"
        val auditHash = generateLedgerHash("DEBIT", requestedAmount, idempotencyKey, newBalance)

        return LedgerCalculationResult(
            currentBalance = currentBalance,
            newBalance = newBalance,
            deltaAmount = -requestedAmount,
            newLifetimeEarned = 0.0,
            newLifetimeWithdrawn = 0.0,
            idempotencyKey = idempotencyKey,
            auditHash = auditHash
        )
    }

    fun calculateWithdrawalRefund(
        currentBalance: Double,
        refundAmount: Double,
        withdrawalId: String
    ): LedgerCalculationResult {
        require(refundAmount > 0.0) { "Refund amount must be positive" }

        val newBalance = currentBalance + refundAmount
        val idempotencyKey = "tx_refund_${withdrawalId}"
        val auditHash = generateLedgerHash("REFUND", refundAmount, idempotencyKey, newBalance)

        return LedgerCalculationResult(
            currentBalance = currentBalance,
            newBalance = newBalance,
            deltaAmount = refundAmount,
            newLifetimeEarned = 0.0,
            newLifetimeWithdrawn = 0.0,
            idempotencyKey = idempotencyKey,
            auditHash = auditHash
        )
    }

    fun calculateManualAdjustment(
        currentBalance: Double,
        adjustAmount: Double,
        adminId: String
    ): LedgerCalculationResult {
        require(adjustAmount != 0.0) { "Adjustment cannot be zero" }
        val newBalance = (currentBalance + adjustAmount).coerceAtLeast(0.0)
        val idempotencyKey = "tx_adj_${adminId}_${UUID.randomUUID().toString().substring(0, 8)}"
        val auditHash = generateLedgerHash("MANUAL_ADJUSTMENT", adjustAmount, idempotencyKey, newBalance)

        return LedgerCalculationResult(
            currentBalance = currentBalance,
            newBalance = newBalance,
            deltaAmount = adjustAmount,
            newLifetimeEarned = 0.0,
            newLifetimeWithdrawn = 0.0,
            idempotencyKey = idempotencyKey,
            auditHash = auditHash
        )
    }

    private fun generateLedgerHash(type: String, amount: Double, idempotencyKey: String, balanceAfter: Double): String {
        val input = "$type:$amount:$idempotencyKey:$balanceAfter:${System.currentTimeMillis()}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
