package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id")
    suspend fun getWithdrawalById(id: String): WithdrawalRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(request: WithdrawalRequestEntity)

    @Update
    suspend fun updateWithdrawal(request: WithdrawalRequestEntity)

    @Query("SELECT COUNT(*) FROM withdrawal_requests WHERE status = 'PENDING'")
    fun getPendingWithdrawalsCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM withdrawal_requests WHERE status = 'COMPLETED'")
    fun getTotalWithdrawnAmount(): Flow<Double?>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :ticketId")
    suspend fun getTicketById(ticketId: String): SupportTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY createdAt ASC")
    fun getMessagesForTicket(ticketId: String): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SupportMessageEntity)
}

@Dao
interface AdminDao {
    // Fraud Events
    @Query("SELECT * FROM fraud_events ORDER BY createdAt DESC")
    fun getAllFraudEvents(): Flow<List<FraudEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFraudEvent(event: FraudEventEntity)

    @Query("UPDATE fraud_events SET status = :status, reviewedBy = :reviewedBy WHERE id = :id")
    suspend fun updateFraudStatus(id: String, status: String, reviewedBy: String)

    @Query("SELECT COUNT(*) FROM fraud_events WHERE status = 'FLAGGED'")
    fun getActiveFraudAlertsCount(): Flow<Int>

    // Audit Logs
    @Query("SELECT * FROM admin_audit_logs ORDER BY createdAt DESC")
    fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLogEntity)

    // Campaigns
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    suspend fun getCampaignById(campaignId: String): CampaignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: CampaignEntity)

    @Update
    suspend fun updateCampaign(campaign: CampaignEntity)
}

@Dao
interface AdsAccountDao {
    @Query("SELECT * FROM ads_accounts ORDER BY createdAt DESC")
    fun getAllAdsAccounts(): Flow<List<AdsAccountEntity>>

    @Query("SELECT * FROM ads_accounts WHERE isEnabled = 1")
    fun getActiveAdsAccounts(): Flow<List<AdsAccountEntity>>

    @Query("SELECT * FROM ads_accounts WHERE id = :id")
    suspend fun getAdsAccountById(id: String): AdsAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: AdsAccountEntity)

    @Delete
    suspend fun delete(account: AdsAccountEntity)

    @Query("DELETE FROM ads_accounts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE ads_accounts SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleEnabled(id: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}

