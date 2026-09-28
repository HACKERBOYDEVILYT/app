package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.engine.FraudRiskEngine
import com.example.domain.engine.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun read_appName_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rewardly", appName)
    }

    @Test
    fun fraudRiskEngine_detects_duplicate_event_attempt() {
        val engine = FraudRiskEngine()
        val evaluation = engine.evaluateActivityAttempt(
            dailyCompletedCount = 1,
            dailyLimit = 5,
            secondsSinceLastAttempt = 60,
            minRequiredSeconds = 15,
            isDuplicateEvent = true
        )
        assertTrue(evaluation.shouldBlock)
        assertTrue(evaluation.flags.contains("DUPLICATE_EVENT_REPLAY_ATTEMPT"))
        assertEquals(RiskLevel.CRITICAL, evaluation.riskLevel)
    }

    @Test
    fun fraudRiskEngine_evaluates_normal_activity_as_low_risk() {
        val engine = FraudRiskEngine()
        val evaluation = engine.evaluateActivityAttempt(
            dailyCompletedCount = 1,
            dailyLimit = 5,
            secondsSinceLastAttempt = 30,
            minRequiredSeconds = 15,
            isDuplicateEvent = false
        )
        assertEquals(false, evaluation.shouldBlock)
        assertEquals(RiskLevel.LOW, evaluation.riskLevel)
    }

    @Test
    fun fraudRiskEngine_evaluates_excessive_withdrawal() {
        val engine = FraudRiskEngine()
        val evaluation = engine.evaluateWithdrawal(
            amount = 100.0,
            balance = 20.0,
            lifetimeEarned = 20.0,
            accountAgeDays = 5,
            userRiskScore = 15
        )
        assertTrue(evaluation.shouldBlock)
        assertTrue(evaluation.flags.contains("REQUESTED_AMOUNT_EXCEEDS_BALANCE"))
    }
}
