package com.example.domain.model

enum class Role(val roleName: String, val displayName: String) {
    SUPER_ADMIN("SUPER_ADMIN", "Super Administrator"),
    ADMIN("ADMIN", "Platform Administrator"),
    MODERATOR("MODERATOR", "Content & Activity Moderator"),
    SUPPORT("SUPPORT", "Customer Support Specialist"),
    FINANCE("FINANCE", "Finance & Payout Officer"),
    ANALYST("ANALYST", "Business & Risk Analyst"),
    USER("USER", "Verified Earner");

    companion object {
        fun fromString(roleStr: String): Role {
            return entries.find { it.roleName.equals(roleStr, ignoreCase = true) } ?: USER
        }
    }
}

enum class Permission(val permissionKey: String, val description: String) {
    USERS_READ("users.read", "View user accounts and profiles"),
    USERS_UPDATE("users.update", "Edit user profiles and metadata"),
    USERS_SUSPEND("users.suspend", "Suspend and ban compromised accounts"),
    REWARDS_READ("rewards.read", "View reward earnings and ledger history"),
    REWARDS_ADJUST("rewards.adjust", "Issue manual audited balance adjustments"),
    WITHDRAWALS_READ("withdrawals.read", "View payout requests and queues"),
    WITHDRAWALS_APPROVE("withdrawals.approve", "Approve and authorize payout requests"),
    WITHDRAWALS_REJECT("withdrawals.reject", "Reject and refund invalid withdrawal requests"),
    CAMPAIGNS_CREATE("campaigns.create", "Create and launch community incentive campaigns"),
    CAMPAIGNS_UPDATE("campaigns.update", "Edit or pause active campaigns"),
    CAMPAIGNS_DELETE("campaigns.delete", "Archive or remove campaigns"),
    FRAUD_READ("fraud.read", "Inspect fraud detection signals and risk logs"),
    FRAUD_REVIEW("fraud.review", "Review and resolve flagged security events"),
    SETTINGS_UPDATE("settings.update", "Modify platform fees, limits, and configurations"),
    AUDIT_READ("audit.read", "Inspect immutable platform audit trails"),
    SUPPORT_READ("support.read", "View customer support tickets"),
    SUPPORT_REPLY("support.reply", "Reply to customer support tickets"),
    SUPPORT_RESOLVE("support.resolve", "Close and resolve customer support tickets")
}
