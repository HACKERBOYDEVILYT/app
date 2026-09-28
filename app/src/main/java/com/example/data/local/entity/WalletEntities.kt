package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userId: String,
    val balance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val lifetimeEarned: Double = 0.0,
    val lifetimeWithdrawn: Double = 0.0,
    val currency: String = "USD",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // EARNING, DAILY_BONUS, REFERRAL_BONUS, WITHDRAWAL_DEBIT, WITHDRAWAL_REFUND, ADMIN_ADJUSTMENT
    val amount: Double,
    val currency: String = "USD",
    val status: String = "COMPLETED", // COMPLETED, PENDING, REJECTED, REFUNDED
    val referenceType: String, // ACTIVITY, CLAIM, REFERRAL, WITHDRAWAL, AUDIT
    val referenceId: String,
    val description: String,
    val idempotencyKey: String,
    val createdAt: Long = System.currentTimeMillis()
)
