package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ActivityAttemptEntity
import com.example.data.local.entity.EarningActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM earning_activities WHERE status = 'ACTIVE' ORDER BY rewardAmount DESC")
    fun getActiveActivities(): Flow<List<EarningActivityEntity>>

    @Query("SELECT * FROM earning_activities ORDER BY createdAt DESC")
    fun getAllActivities(): Flow<List<EarningActivityEntity>>

    @Query("SELECT * FROM earning_activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): EarningActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: EarningActivityEntity)

    @Update
    suspend fun updateActivity(activity: EarningActivityEntity)

    @Query("UPDATE earning_activities SET status = :status WHERE id = :activityId")
    suspend fun updateActivityStatus(activityId: String, status: String)

    @Query("UPDATE earning_activities SET completionsCount = completionsCount + 1 WHERE id = :activityId")
    suspend fun incrementCompletionCount(activityId: String)

    // Attempts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ActivityAttemptEntity)

    @Update
    suspend fun updateAttempt(attempt: ActivityAttemptEntity)

    @Query("SELECT * FROM activity_attempts WHERE providerEventId = :providerEventId LIMIT 1")
    suspend fun getAttemptByProviderEventId(providerEventId: String): ActivityAttemptEntity?

    @Query("SELECT COUNT(*) FROM activity_attempts WHERE userId = :userId AND activityId = :activityId AND verificationStatus = 'VERIFIED' AND createdAt >= :sinceTimestamp")
    suspend fun getVerifiedAttemptCountToday(userId: String, activityId: String, sinceTimestamp: Long): Int

    @Query("SELECT * FROM activity_attempts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserAttempts(userId: String): Flow<List<ActivityAttemptEntity>>

    @Query("SELECT COUNT(*) FROM activity_attempts WHERE verificationStatus = 'VERIFIED'")
    fun getTotalVerifiedCompletions(): Flow<Int>
}
