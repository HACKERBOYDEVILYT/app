package com.example.data.local

import com.example.data.local.entity.*
import java.text.SimpleDateFormat
import java.util.*

object DatabaseInitializer {

    suspend fun seedIfEmpty(database: AppDatabase) {
        val userDao = database.userDao()
        val existing = userDao.getUserById("user_alex_01")
        if (existing != null) return

        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date(now))

        // 1. Seed Demo User
        val demoUser = UserEntity(
            id = "user_alex_01",
            username = "alex_earner",
            email = "alex.johnson@example.com",
            displayName = "Alex Johnson",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80",
            country = "United States",
            timezone = "America/New_York",
            referralCode = "ALEX777",
            status = "ACTIVE",
            role = "USER",
            riskScore = 15,
            isKycVerified = true,
            createdAt = now - (86400000L * 12)
        )
        userDao.insertUser(demoUser)

        // 2. Seed Admin User
        val adminUser = UserEntity(
            id = "admin_sarah_01",
            username = "sarah_admin",
            email = "sarah.admin@rewardly.io",
            displayName = "Sarah Chen (Admin)",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80",
            country = "United States",
            timezone = "America/Los_Angeles",
            referralCode = "SARAHADMIN",
            status = "ACTIVE",
            role = "SUPER_ADMIN",
            riskScore = 5,
            isKycVerified = true,
            createdAt = now - (86400000L * 90)
        )
        userDao.insertUser(adminUser)

        // 3. Seed Additional Users for Admin Panel
        val user2 = UserEntity(
            id = "user_marcus_02",
            username = "marcus_dev",
            email = "marcus@example.com",
            displayName = "Marcus Vance",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=120&q=80",
            country = "Canada",
            timezone = "America/Toronto",
            referralCode = "MARCUS99",
            referredBy = "ALEX777",
            status = "ACTIVE",
            role = "USER",
            riskScore = 22,
            isKycVerified = true,
            createdAt = now - (86400000L * 5)
        )
        val user3 = UserEntity(
            id = "user_elena_03",
            username = "elena_k",
            email = "elena@example.com",
            displayName = "Elena Rostova",
            avatarUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=120&q=80",
            country = "United Kingdom",
            timezone = "Europe/London",
            referralCode = "ELENA2026",
            referredBy = "ALEX777",
            status = "ACTIVE",
            role = "USER",
            riskScore = 18,
            isKycVerified = false,
            createdAt = now - (86400000L * 3)
        )
        val userSuspicious = UserEntity(
            id = "user_bot_04",
            username = "fast_clicker_99",
            email = "suspicious.bot@tempmail.com",
            displayName = "Auto User 99",
            avatarUrl = "",
            country = "Unknown Proxy",
            timezone = "UTC",
            referralCode = "BOTX01",
            status = "SUSPENDED",
            role = "USER",
            riskScore = 88,
            isKycVerified = false,
            createdAt = now - (86400000L * 1)
        )
        userDao.insertUser(user2)
        userDao.insertUser(user3)
        userDao.insertUser(userSuspicious)

        // 4. Seed Wallets
        val walletDao = database.walletDao()
        walletDao.insertWallet(
            WalletEntity(
                userId = "user_alex_01",
                balance = 24.50,
                pendingBalance = 5.00,
                lifetimeEarned = 42.00,
                lifetimeWithdrawn = 12.50,
                currency = "USD"
            )
        )
        walletDao.insertWallet(
            WalletEntity(
                userId = "admin_sarah_01",
                balance = 150.00,
                pendingBalance = 0.0,
                lifetimeEarned = 150.00,
                lifetimeWithdrawn = 0.0,
                currency = "USD"
            )
        )
        walletDao.insertWallet(
            WalletEntity(
                userId = "user_marcus_02",
                balance = 8.20,
                pendingBalance = 0.0,
                lifetimeEarned = 8.20,
                lifetimeWithdrawn = 0.0,
                currency = "USD"
            )
        )

        // 5. Seed Ledger Transactions for Alex
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_seed_01",
                userId = "user_alex_01",
                type = "EARNING",
                amount = 0.50,
                currency = "USD",
                status = "COMPLETED",
                referenceType = "ACTIVITY",
                referenceId = "act_ad_01",
                description = "Verified Admob Video completion reward",
                idempotencyKey = "tx_key_admob_seed_01",
                createdAt = now - (3600000L * 4)
            )
        )
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_seed_02",
                userId = "user_alex_01",
                type = "DAILY_BONUS",
                amount = 0.50,
                currency = "USD",
                status = "COMPLETED",
                referenceType = "CLAIM",
                referenceId = "claim_day_3",
                description = "Day 3 Daily Bonus (Streak 1.25x)",
                idempotencyKey = "tx_key_daily_seed_02",
                createdAt = now - (86400000L * 1)
            )
        )
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_seed_03",
                userId = "user_alex_01",
                type = "REFERRAL_BONUS",
                amount = 2.00,
                currency = "USD",
                status = "COMPLETED",
                referenceType = "REFERRAL",
                referenceId = "ref_marcus",
                description = "Verified referral milestone: marcus_dev",
                idempotencyKey = "tx_key_ref_seed_03",
                createdAt = now - (86400000L * 2)
            )
        )
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_seed_04",
                userId = "user_alex_01",
                type = "WITHDRAWAL_DEBIT",
                amount = -12.50,
                currency = "USD",
                status = "COMPLETED",
                referenceType = "WITHDRAWAL",
                referenceId = "wd_alex_past_01",
                description = "Withdrawal to PayPal (alex.j***@gmail.com)",
                idempotencyKey = "tx_key_wd_seed_04",
                createdAt = now - (86400000L * 7)
            )
        )

        // 6. Seed Earning Activities
        val activityDao = database.activityDao()
        val activities = listOf(
            EarningActivityEntity(
                id = "act_admob_video",
                type = "REWARDED_AD",
                title = "Interactive Fintech Ad",
                description = "Watch sponsor ad to the end with cryptographic server callback proof.",
                rewardAmount = 0.50,
                currency = "USD",
                dailyLimit = 5,
                globalLimit = 2000,
                durationSeconds = 15,
                status = "ACTIVE",
                provider = "AdMob Verified SSV",
                providerActivityId = "admob_ssv_pl_001",
                verificationMethod = "SERVER_CALLBACK"
            ),
            EarningActivityEntity(
                id = "act_unity_playable",
                type = "REWARDED_AD",
                title = "Verified Game Trailer Sponsor",
                description = "Engage with verified partner playable ad for 20 seconds.",
                rewardAmount = 0.75,
                currency = "USD",
                dailyLimit = 4,
                globalLimit = 1500,
                durationSeconds = 20,
                status = "ACTIVE",
                provider = "Unity Ads Verified",
                providerActivityId = "unity_pl_902",
                verificationMethod = "SERVER_CALLBACK"
            ),
            EarningActivityEntity(
                id = "act_survey_consumer",
                type = "SURVEY",
                title = "Digital Banking Pulse Survey",
                description = "Complete accredited 3-minute financial habits research survey.",
                rewardAmount = 1.50,
                currency = "USD",
                dailyLimit = 2,
                globalLimit = 500,
                durationSeconds = 30,
                status = "ACTIVE",
                provider = "TheoremReach Research",
                providerActivityId = "tr_survey_77",
                verificationMethod = "VERIFIED_PROOF"
            ),
            EarningActivityEntity(
                id = "act_survey_tech",
                type = "SURVEY",
                title = "AI & Mobile Utilities Survey",
                description = "Share insights on mobile fintech app performance.",
                rewardAmount = 2.00,
                currency = "USD",
                dailyLimit = 1,
                globalLimit = 300,
                durationSeconds = 40,
                status = "ACTIVE",
                provider = "Dynata Enterprise",
                providerActivityId = "dynata_ai_04",
                verificationMethod = "VERIFIED_PROOF"
            ),
            EarningActivityEntity(
                id = "act_offer_wallet",
                type = "OFFER",
                title = "Sponsor Web3 Wallet Verification",
                description = "Sign in to partner verified custody demo and achieve Level 1.",
                rewardAmount = 3.50,
                currency = "USD",
                dailyLimit = 1,
                globalLimit = 200,
                durationSeconds = 60,
                status = "ACTIVE",
                provider = "TapJoy Direct",
                providerActivityId = "tj_partner_88",
                verificationMethod = "SIGNED_TOKEN"
            ),
            EarningActivityEntity(
                id = "act_campaign_boost",
                type = "ADMIN_CAMPAIGN",
                title = "Spring Verification Milestone",
                description = "Special community bonus for completing 3 verified activities.",
                rewardAmount = 2.50,
                currency = "USD",
                dailyLimit = 1,
                globalLimit = 100,
                durationSeconds = 30,
                status = "ACTIVE",
                provider = "Rewardly Network",
                providerActivityId = "camp_boost_01",
                verificationMethod = "SERVER_CALLBACK"
            )
        )
        activities.forEach { activityDao.insertActivity(it) }

        // 7. Seed Referrals
        val referralDao = database.referralDao()
        referralDao.insertReferral(
            ReferralEntity(
                id = "ref_marcus",
                referrerId = "user_alex_01",
                referredUserId = "user_marcus_02",
                referredUsername = "marcus_dev",
                status = "VERIFIED_EARNER",
                rewardAmount = 2.00,
                milestoneRequirement = "Earn $5.00 from verified activities",
                milestoneProgress = 100,
                createdAt = now - (86400000L * 4)
            )
        )
        referralDao.insertReferral(
            ReferralEntity(
                id = "ref_elena",
                referrerId = "user_alex_01",
                referredUserId = "user_elena_03",
                referredUsername = "elena_k",
                status = "REGISTERED",
                rewardAmount = 2.00,
                milestoneRequirement = "Earn $5.00 from verified activities",
                milestoneProgress = 65,
                createdAt = now - (86400000L * 2)
            )
        )

        // 8. Seed Withdrawals
        val withdrawalDao = database.withdrawalDao()
        withdrawalDao.insertWithdrawal(
            WithdrawalRequestEntity(
                id = "wd_req_01",
                userId = "user_alex_01",
                userDisplayName = "Alex Johnson",
                amount = 10.00,
                fee = 0.25,
                netAmount = 9.75,
                currency = "USD",
                method = "PAYPAL",
                destinationMasked = "alex.j***@gmail.com",
                status = "PENDING",
                riskScore = 14,
                createdAt = now - (3600000L * 5)
            )
        )
        withdrawalDao.insertWithdrawal(
            WithdrawalRequestEntity(
                id = "wd_req_02",
                userId = "user_marcus_02",
                userDisplayName = "Marcus Vance",
                amount = 25.00,
                fee = 0.50,
                netAmount = 24.50,
                currency = "USD",
                method = "CRYPTO_USDC",
                destinationMasked = "0x7F...8b14",
                status = "UNDER_REVIEW",
                riskScore = 28,
                createdAt = now - (3600000L * 12)
            )
        )
        withdrawalDao.insertWithdrawal(
            WithdrawalRequestEntity(
                id = "wd_req_03",
                userId = "user_alex_01",
                userDisplayName = "Alex Johnson",
                amount = 12.50,
                fee = 0.30,
                netAmount = 12.20,
                currency = "USD",
                method = "BANK_ACH",
                destinationMasked = "Chase Checking **** 9821",
                status = "COMPLETED",
                riskScore = 10,
                reviewedBy = "admin_sarah_01",
                reviewedAt = now - (86400000L * 6),
                createdAt = now - (86400000L * 7)
            )
        )

        // 9. Seed Notifications
        val notifDao = database.notificationDao()
        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_01",
                userId = "user_alex_01",
                type = "REWARD_RECEIVED",
                title = "Reward Credited +$0.50",
                message = "Your AdMob video completion was cryptographically verified and added to your ledger.",
                isRead = false,
                createdAt = now - (3600000L * 4)
            )
        )
        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_02",
                userId = "user_alex_01",
                type = "REFERRAL_VERIFIED",
                title = "Referral Milestone Reached! +$2.00",
                message = "Your friend marcus_dev verified $5 in earnings. Referral reward credited.",
                isRead = true,
                createdAt = now - (86400000L * 2)
            )
        )
        notifDao.insertNotification(
            NotificationEntity(
                id = "notif_03",
                userId = "user_alex_01",
                type = "ANNOUNCEMENT",
                title = "Rewardly Platform Security Update",
                message = "All rewards now use instant cryptographic SSV verification signatures for maximum safety.",
                isRead = false,
                createdAt = now - (86400000L * 1)
            )
        )

        // 10. Seed Admin Campaigns
        val adminDao = database.adminDao()
        adminDao.insertCampaign(
            CampaignEntity(
                id = "camp_spring_2026",
                name = "Spring Earner Boost",
                description = "High-payout sponsored reward campaign for verified community members.",
                budget = 1000.0,
                budgetSpent = 245.50,
                rewardPerCompletion = 2.50,
                targetAudience = "All Verified Users",
                status = "ACTIVE",
                completionsCount = 98,
                createdAt = now - (86400000L * 10)
            )
        )
        adminDao.insertCampaign(
            CampaignEntity(
                id = "camp_referral_surge",
                name = "Power Referrers Incentive",
                description = "Additional bonus pool allocated for verified invite conversions.",
                budget = 500.0,
                budgetSpent = 120.00,
                rewardPerCompletion = 5.00,
                targetAudience = "Tier 2 Referrers",
                status = "ACTIVE",
                completionsCount = 24,
                createdAt = now - (86400000L * 15)
            )
        )

        // 11. Seed Fraud Alerts
        adminDao.insertFraudEvent(
            FraudEventEntity(
                id = "fraud_01",
                userId = "user_bot_04",
                username = "fast_clicker_99",
                type = "RAPID_COMPLETIONS",
                riskScore = 88,
                metadata = "User attempted 14 video completions in under 45 seconds using automated script headers.",
                status = "FLAGGED",
                createdAt = now - (3600000L * 8)
            )
        )
        adminDao.insertFraudEvent(
            FraudEventEntity(
                id = "fraud_02",
                userId = "user_marcus_02",
                username = "marcus_dev",
                type = "WITHDRAWAL_ANOMALY",
                riskScore = 35,
                metadata = "First withdrawal request within 24 hours of account creation. Manual verification recommended.",
                status = "UNDER_REVIEW",
                createdAt = now - (3600000L * 12)
            )
        )

        // 12. Seed Admin Audit Logs
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_01",
                adminId = "admin_sarah_01",
                adminName = "Sarah Chen",
                action = "SUSPEND_USER",
                entityType = "USER",
                entityId = "user_bot_04",
                metadata = "Account suspended due to automated replay attack detected by FraudRiskEngine.",
                createdAt = now - (3600000L * 7)
            )
        )
        adminDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "audit_02",
                adminId = "admin_sarah_01",
                adminName = "Sarah Chen",
                action = "APPROVE_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = "wd_req_03",
                metadata = "Approved ACH Bank withdrawal of $12.50 to Chase Checking ****9821.",
                createdAt = now - (86400000L * 6)
            )
        )

        // 13. Seed Support Ticket
        val supportDao = database.supportDao()
        val ticketId = "ticket_alex_01"
        supportDao.insertTicket(
            SupportTicketEntity(
                id = ticketId,
                userId = "user_alex_01",
                userDisplayName = "Alex Johnson",
                subject = "Question regarding PayPal withdrawal fee",
                category = "WITHDRAWAL",
                priority = "LOW",
                status = "IN_PROGRESS",
                lastMessage = "Thanks for contacting Rewardly Support. Standard processing takes 12-24 hours.",
                createdAt = now - (3600000L * 10),
                updatedAt = now - (3600000L * 2)
            )
        )
        supportDao.insertMessage(
            SupportMessageEntity(
                id = "msg_01",
                ticketId = ticketId,
                senderId = "user_alex_01",
                senderName = "Alex Johnson",
                senderRole = "USER",
                message = "Hi! I submitted a $10.00 withdrawal to PayPal. Is there an automated receipt emailed once approved?",
                createdAt = now - (3600000L * 10)
            )
        )
        supportDao.insertMessage(
            SupportMessageEntity(
                id = "msg_02",
                ticketId = ticketId,
                senderId = "admin_sarah_01",
                senderName = "Sarah Chen (Support)",
                senderRole = "SUPPORT",
                message = "Hello Alex! Yes, an automated notification and cryptographic ledger receipt are generated in your app as soon as our finance team verifies the payout.",
                createdAt = now - (3600000L * 2)
            )
        )

        // 11. Seed Default Ads Accounts
        val adsAccountDao = database.adsAccountDao()
        adsAccountDao.insertOrUpdate(
            AdsAccountEntity(
                id = "ad_admob_01",
                networkName = "Google AdMob",
                accountLabel = "Primary Production AdMob",
                appId = "ca-app-pub-3940256099942544~3347511713",
                rewardedUnitId = "ca-app-pub-3940256099942544/5224354917",
                interstitialUnitId = "ca-app-pub-3940256099942544/1033173712",
                rewardPerAd = 0.50,
                isEnabled = true,
                notes = "High-performing Google AdMob mediation with SSV callbacks."
            )
        )
        adsAccountDao.insertOrUpdate(
            AdsAccountEntity(
                id = "ad_unity_02",
                networkName = "Unity Ads",
                accountLabel = "Unity Rewarded Video Network",
                appId = "unity_game_4829104",
                rewardedUnitId = "rewardedVideo",
                interstitialUnitId = "interstitialVideo",
                rewardPerAd = 0.75,
                isEnabled = true,
                notes = "Gaming and high-conversion video ads."
            )
        )
        adsAccountDao.insertOrUpdate(
            AdsAccountEntity(
                id = "ad_applovin_03",
                networkName = "AppLovin MAX",
                accountLabel = "AppLovin Global Mediation",
                appId = "sdk_applovin_max_88319",
                rewardedUnitId = "max_rewarded_zone_1",
                rewardPerAd = 0.60,
                isEnabled = false,
                notes = "Backup mediation tier with competitive eCPMs."
            )
        )
    }
}

