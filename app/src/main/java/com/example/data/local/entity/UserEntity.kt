package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String,
    val country: String,
    val timezone: String,
    val referralCode: String,
    val referredBy: String? = null,
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, PENDING_VERIFICATION
    val role: String = "USER",     // SUPER_ADMIN, ADMIN, MODERATOR, FINANCE, SUPPORT, ANALYST, USER
    val riskScore: Int = 12,
    val isKycVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
