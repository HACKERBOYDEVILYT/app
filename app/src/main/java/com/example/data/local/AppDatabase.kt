package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        WalletTransactionEntity::class,
        EarningActivityEntity::class,
        ActivityAttemptEntity::class,
        DailyClaimEntity::class,
        ReferralEntity::class,
        WithdrawalRequestEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        SupportMessageEntity::class,
        FraudEventEntity::class,
        AdminAuditLogEntity::class,
        CampaignEntity::class,
        AdsAccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun activityDao(): ActivityDao
    abstract fun dailyClaimDao(): DailyClaimDao
    abstract fun referralDao(): ReferralDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportDao(): SupportDao
    abstract fun adminDao(): AdminDao
    abstract fun adsAccountDao(): AdsAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rewardly_database.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
