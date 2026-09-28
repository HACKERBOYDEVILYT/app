package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WalletEntity
import com.example.data.local.entity.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWallet(userId: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun observeWallet(userId: String): Flow<WalletEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactions(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(userId: String, type: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getTransactionByIdempotencyKey(key: String): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE type IN ('EARNING', 'DAILY_BONUS', 'REFERRAL_BONUS') AND status = 'COMPLETED'")
    fun getTotalRewardsIssued(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE userId = :userId AND type IN ('EARNING', 'DAILY_BONUS', 'REFERRAL_BONUS') AND createdAt >= :sinceTimestamp")
    fun getTodayEarnings(userId: String, sinceTimestamp: Long): Flow<Double?>
}
