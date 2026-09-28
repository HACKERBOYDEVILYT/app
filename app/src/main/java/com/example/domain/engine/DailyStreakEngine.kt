package com.example.domain.engine

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class DailyStreakEvaluation(
    val newStreak: Int,
    val multiplier: Double,
    val baseReward: Double,
    val finalReward: Double,
    val milestoneBonus: Double,
    val isMilestoneDay: Boolean,
    val milestoneTitle: String?,
    val dateString: String
)

class DailyStreakEngine {

    companion object {
        const val BASE_DAILY_REWARD = 0.25
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    fun evaluateClaim(
        lastClaimDateStr: String?,
        currentStreakCount: Int,
        nowTimestamp: Long = System.currentTimeMillis()
    ): DailyStreakEvaluation {
        val todayStr = DATE_FORMAT.format(Date(nowTimestamp))

        if (lastClaimDateStr == todayStr) {
            throw IllegalStateException("Already claimed today. Come back tomorrow to continue your streak!")
        }

        val calculatedStreak = if (lastClaimDateStr == null) {
            1
        } else {
            val lastDate = DATE_FORMAT.parse(lastClaimDateStr)
            val todayDate = DATE_FORMAT.parse(todayStr)
            if (lastDate != null && todayDate != null) {
                val diffInMillis = todayDate.time - lastDate.time
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                if (diffDays == 1L) {
                    currentStreakCount + 1
                } else {
                    // Missed more than 1 day: Streak resets to 1
                    1
                }
            } else {
                1
            }
        }

        // Multiplier progression
        val multiplier = when (calculatedStreak) {
            in 1..2 -> 1.0
            in 3..5 -> 1.25
            in 6..9 -> 1.50
            in 10..13 -> 1.75
            else -> 2.0
        }

        // Special milestone rewards (Day 7, Day 14, Day 30)
        val (isMilestone, milestoneTitle, milestoneBonus) = when (calculatedStreak) {
            7 -> Triple(true, "7-Day Streak Master Bonus", 1.00)
            14 -> Triple(true, "14-Day Bi-Weekly Champion", 2.50)
            30 -> Triple(true, "30-Day Legend Milestone", 5.00)
            else -> Triple(false, null, 0.0)
        }

        val baseCalculated = BASE_DAILY_REWARD * multiplier
        val totalReward = baseCalculated + milestoneBonus

        return DailyStreakEvaluation(
            newStreak = calculatedStreak,
            multiplier = multiplier,
            baseReward = BASE_DAILY_REWARD,
            finalReward = totalReward,
            milestoneBonus = milestoneBonus,
            isMilestoneDay = isMilestone,
            milestoneTitle = milestoneTitle,
            dateString = todayStr
        )
    }
}
