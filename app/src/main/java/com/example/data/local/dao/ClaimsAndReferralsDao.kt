package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DailyClaimEntity
import com.example.data.local.entity.ReferralEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyClaimDao {
    @Query("SELECT * FROM daily_claims WHERE userId = :userId AND claimDate = :dateStr LIMIT 1")
    suspend fun getClaimForDate(userId: String, dateStr: String): DailyClaimEntity?

    @Query("SELECT * FROM daily_claims WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestClaim(userId: String): DailyClaimEntity?

    @Query("SELECT * FROM daily_claims WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllClaimsForUser(userId: String): Flow<List<DailyClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClaim(claim: DailyClaimEntity)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerId = :referrerId ORDER BY createdAt DESC")
    fun getReferralsForUser(referrerId: String): Flow<List<ReferralEntity>>

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :referrerId")
    fun getReferralCount(referrerId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :referrerId AND status = 'VERIFIED_EARNER'")
    fun getVerifiedReferralCount(referrerId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity)

    @Update
    suspend fun updateReferral(referral: ReferralEntity)

    @Query("SELECT * FROM referrals ORDER BY createdAt DESC")
    fun getAllReferrals(): Flow<List<ReferralEntity>>
}
