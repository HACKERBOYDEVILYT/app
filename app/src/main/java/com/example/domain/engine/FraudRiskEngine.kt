package com.example.domain.engine

enum class RiskLevel(val label: String, val minScore: Int, val maxScore: Int) {
    LOW("LOW", 0, 29),
    MEDIUM("MEDIUM", 30, 59),
    HIGH("HIGH", 60, 79),
    CRITICAL("CRITICAL", 80, 100);

    companion object {
        fun fromScore(score: Int): RiskLevel {
            return when {
                score >= 80 -> CRITICAL
                score >= 60 -> HIGH
                score >= 30 -> MEDIUM
                else -> LOW
            }
        }
    }
}

data class FraudEvaluation(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val flags: List<String>,
    val shouldBlock: Boolean,
    val requiresReview: Boolean
)

class FraudRiskEngine {

    fun evaluateActivityAttempt(
        dailyCompletedCount: Int,
        dailyLimit: Int,
        secondsSinceLastAttempt: Long,
        minRequiredSeconds: Long,
        isDuplicateEvent: Boolean
    ): FraudEvaluation {
        val flags = mutableListOf<String>()
        var score = 10

        if (isDuplicateEvent) {
            flags.add("DUPLICATE_EVENT_REPLAY_ATTEMPT")
            score += 70
        }

        if (secondsSinceLastAttempt < minRequiredSeconds && secondsSinceLastAttempt >= 0) {
            flags.add("IMPOSSIBLE_ACTIVITY_FREQUENCY (${secondsSinceLastAttempt}s < ${minRequiredSeconds}s)")
            score += 45
        }

        if (dailyCompletedCount >= dailyLimit) {
            flags.add("DAILY_LIMIT_EXCEEDED ($dailyCompletedCount / $dailyLimit)")
            score += 35
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        return FraudEvaluation(
            riskScore = score,
            riskLevel = level,
            flags = flags,
            shouldBlock = score >= 70,
            requiresReview = score >= 50
        )
    }

    fun evaluateWithdrawal(
        amount: Double,
        balance: Double,
        lifetimeEarned: Double,
        accountAgeDays: Int,
        userRiskScore: Int
    ): FraudEvaluation {
        val flags = mutableListOf<String>()
        var score = userRiskScore

        if (amount > balance) {
            flags.add("REQUESTED_AMOUNT_EXCEEDS_BALANCE")
            score += 80
        }

        if (accountAgeDays < 1 && amount > 10.0) {
            flags.add("LARGE_WITHDRAWAL_ON_NEW_ACCOUNT")
            score += 30
        }

        if (amount > lifetimeEarned) {
            flags.add("WITHDRAWAL_EXCEEDS_LIFETIME_EARNINGS")
            score += 50
        }

        score = score.coerceIn(0, 100)
        val level = RiskLevel.fromScore(score)

        return FraudEvaluation(
            riskScore = score,
            riskLevel = level,
            flags = flags,
            shouldBlock = amount > balance,
            requiresReview = score >= 40
        )
    }
}
