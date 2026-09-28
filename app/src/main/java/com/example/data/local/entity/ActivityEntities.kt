package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earning_activities")
data class EarningActivityEntity(
    @PrimaryKey val id: String,
    val type: String, // REWARDED_AD, SURVEY, OFFER, DAILY_BONUS, REFERRAL, ADMIN_CAMPAIGN
    val title: String,
    val description: String,
    val rewardAmount: Double,
    val currency: String = "USD",
    val dailyLimit: Int = 5,
    val globalLimit: Int = 1000,
    val durationSeconds: Int = 30,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val provider: String,
    val providerActivityId: String,
    val verificationMethod: String = "SERVER_CALLBACK", // SERVER_CALLBACK, VERIFIED_PROOF, SIGNED_TOKEN
    val completionsCount: Int = 0,
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + 86400000L * 30,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_attempts")
data class ActivityAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val activityId: String,
    val providerEventId: String,
    val status: String, // INITIATED, IN_PROGRESS, VERIFIED, FAILED, DUPLICATE_REJECTED
    val rewardAmount: Double,
    val verificationStatus: String, // VERIFIED, REJECTED, PENDING
    val ipHash: String,
    val deviceHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)
