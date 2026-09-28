package com.example.ui.util

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    BANGLA("bn", "Bangla", "বাংলা"),
    ENGLISH("en", "English", "English")
}

object Strings {
    // Current active language
    var currentLanguage: AppLanguage = AppLanguage.BANGLA

    fun t(key: String): String {
        val isBn = currentLanguage == AppLanguage.BANGLA
        return when (key) {
            // General & Branding
            "app_name" -> if (isBn) "রিওয়ার্ডলি" else "Rewardly"
            "tagline" -> if (isBn) "ভেরিফাইড টাস্ক সম্পূর্ণ করে আসল রিওয়ার্ড আয় করুন" else "Complete verified tasks & earn legitimate rewards"
            "verified" -> if (isBn) "ভেরিফাইড" else "VERIFIED"
            "balance" -> if (isBn) "ব্যালেন্স" else "Balance"
            "available_balance" -> if (isBn) "উত্তোলনযোগ্য ব্যালেন্স" else "Available Balance"
            "pending_balance" -> if (isBn) "অপেক্ষমান ব্যালেন্স" else "Pending Balance"
            "lifetime_earned" -> if (isBn) "মোট অর্জিত আয়" else "Lifetime Earned"

            // Navigation
            "nav_home" -> if (isBn) "হোম" else "Home"
            "nav_earn" -> if (isBn) "আয় করুন" else "Earn"
            "nav_wallet" -> if (isBn) "ওয়ালেট" else "Wallet"
            "nav_withdraw" -> if (isBn) "উত্তোলন" else "Withdraw"
            "nav_referrals" -> if (isBn) "রেফারেল" else "Referrals"
            "nav_profile" -> if (isBn) "প্রোফাইল" else "Profile"
            "nav_admin" -> if (isBn) "অ্যাডমিন" else "Admin"

            // Auth Screen
            "auth_welcome" -> if (isBn) "রিওয়ার্ডলিতে স্বাগতম" else "Welcome to Rewardly"
            "auth_login_tab" -> if (isBn) "লগইন" else "Sign In"
            "auth_register_tab" -> if (isBn) "নতুন অ্যাকাউন্ট" else "Register"
            "auth_admin_tab" -> if (isBn) "অ্যাডমিন প্রবেশ" else "Admin Portal"
            "email_or_username" -> if (isBn) "ইউজারনেম বা ইমেইল" else "Username or Email"
            "password" -> if (isBn) "পাসওয়ার্ড" else "Password"
            "admin_password" -> if (isBn) "অ্যাডমিন পাসওয়ার্ড (robiul1000)" else "Admin Password (robiul1000)"
            "btn_login" -> if (isBn) "লগইন করুন" else "Sign In"
            "btn_quick_demo" -> if (isBn) "ডেমো ইউজার হিসেবে শুরু করুন" else "Continue as Demo Earner"
            "btn_unlock_admin" -> if (isBn) "অ্যাডমিন প্যানেল আনলক করুন" else "Unlock Admin Console"
            "full_name" -> if (isBn) "আপনার পুরো নাম" else "Full Name"
            "username" -> if (isBn) "ইউজারনেম" else "Username"
            "email" -> if (isBn) "ইমেইল এড্রেস" else "Email Address"
            "referral_code_optional" -> if (isBn) "রেফারেল কোড (ঐচ্ছিক)" else "Referral Code (Optional)"
            "btn_create_account" -> if (isBn) "অ্যাকাউন্ট তৈরি করুন" else "Create Account"
            "logout" -> if (isBn) "লগআউট" else "Log Out"

            // Admin & Ads
            "admin_panel" -> if (isBn) "সুপার অ্যাডমিন প্যানেল" else "Super Admin Console"
            "ads_accounts" -> if (isBn) "বিজ্ঞাপন অ্যাকাউন্ট (Ads)" else "Ads Accounts"
            "add_ads_account" -> if (isBn) "+ বিজ্ঞাপন অ্যাকাউন্ট যোগ করুন" else "+ Add Ads Account"
            "ad_network" -> if (isBn) "বিজ্ঞাপন নেটওয়ার্ক" else "Ad Network"
            "account_label" -> if (isBn) "অ্যাকাউন্টের নাম / লেবেল" else "Account Label"
            "app_id_game_id" -> if (isBn) "App ID / Game ID / SDK Key" else "App ID / Game ID / SDK Key"
            "rewarded_unit_id" -> if (isBn) "রিওয়ার্ডেড ভিডিও ইউনিট আইডি (Rewarded Unit ID)" else "Rewarded Video Unit ID"
            "interstitial_unit_id" -> if (isBn) "ইন্টারস্টিশিয়াল ইউনিট আইডি (ঐচ্ছিক)" else "Interstitial Unit ID (Optional)"
            "reward_per_ad" -> if (isBn) "প্রতি বিজ্ঞাপনে রিওয়ার্ড ($)" else "Reward Per Ad View ($)"
            "status_active" -> if (isBn) "সক্রিয় (Active)" else "Active"
            "status_inactive" -> if (isBn) "নিষ্ক্রিয় (Inactive)" else "Inactive"
            "save_account" -> if (isBn) "অ্যাকাউন্ট সংরক্ষণ করুন" else "Save Account"
            "delete" -> if (isBn) "মুছে ফেলুন" else "Delete"
            "cancel" -> if (isBn) "বাতিল" else "Cancel"

            // Daily Streak & Activity
            "daily_bonus" -> if (isBn) "দৈনিক বোনাস" else "Daily Streak Bonus"
            "claim_bonus" -> if (isBn) "বোনাস গ্রহণ করুন" else "Claim Bonus"
            "claimed_today" -> if (isBn) "আজকের বোনাস নেওয়া হয়েছে" else "Claimed Today"
            "day" -> if (isBn) "দিন" else "Day"
            "start_activity" -> if (isBn) "কাজ শুরু করুন" else "Start Activity"
            "verify_claim" -> if (isBn) "ভেরিফাই ও রিওয়ার্ড নিন" else "Verify & Claim Reward"

            // Language Switcher
            "switch_lang" -> if (isBn) "English এ পরিবর্তন" else "বাংলায় পরিবর্তন"
            else -> key
        }
    }
}
