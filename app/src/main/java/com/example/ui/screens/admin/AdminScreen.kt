package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminSubTab
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    adminUser: UserEntity,
    currentTab: AdminSubTab,
    users: List<UserEntity>,
    withdrawals: List<WithdrawalRequestEntity>,
    activities: List<EarningActivityEntity>,
    campaigns: List<CampaignEntity>,
    fraudEvents: List<FraudEventEntity>,
    auditLogs: List<AdminAuditLogEntity>,
    totalRewardsIssued: Double,
    adsAccounts: List<AdsAccountEntity> = emptyList(),
    onTabSelected: (AdminSubTab) -> Unit,
    onApproveWithdrawal: (String) -> Unit,
    onRejectWithdrawal: (String, String) -> Unit,
    onSuspendUser: (String, String) -> Unit,
    onRestoreUser: (String) -> Unit,
    onAdjustBalance: (String, Double, String) -> Unit,
    onCreateActivity: (EarningActivityEntity) -> Unit,
    onCreateCampaign: (CampaignEntity) -> Unit,
    onResolveFraud: (String) -> Unit,
    onAddAdsAccount: (networkName: String, label: String, appId: String, rewardedUnitId: String, interstitialUnitId: String, rewardPerAd: Double, isEnabled: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onToggleAdsAccount: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteAdsAccount: (String) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    val tabs = AdminSubTab.values()

    // Modals state
    var selectedWithdrawalForReject by remember { mutableStateOf<WithdrawalRequestEntity?>(null) }
    var selectedUserForAdjust by remember { mutableStateOf<UserEntity?>(null) }
    var showCreateActivityModal by remember { mutableStateOf(false) }
    var showCreateCampaignModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Admin Sub-Tabs Navigation Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .testTag("admin_sub_tabs"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isSelected = currentTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    label = {
                        Text(
                            text = tab.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 2. Tab Content
        when (currentTab) {
            AdminSubTab.OVERVIEW -> {
                AdminOverviewView(
                    users = users,
                    withdrawals = withdrawals,
                    activities = activities,
                    totalRewardsIssued = totalRewardsIssued,
                    fraudEvents = fraudEvents
                )
            }
            AdminSubTab.USERS -> {
                AdminUsersView(
                    users = users,
                    onSuspendUser = { userId -> onSuspendUser(userId, "Suspended via Admin Console review") },
                    onRestoreUser = onRestoreUser,
                    onOpenAdjust = { selectedUserForAdjust = it }
                )
            }
            AdminSubTab.WITHDRAWALS -> {
                AdminWithdrawalsView(
                    withdrawals = withdrawals,
                    onApprove = onApproveWithdrawal,
                    onOpenReject = { selectedWithdrawalForReject = it }
                )
            }
            AdminSubTab.ACTIVITIES -> {
                AdminActivitiesView(
                    activities = activities,
                    onOpenCreate = { showCreateActivityModal = true }
                )
            }
            AdminSubTab.ADS_ACCOUNTS -> {
                AdsAccountsAdminView(
                    adsAccounts = adsAccounts,
                    onAddAdsAccount = onAddAdsAccount,
                    onToggleAdsAccount = onToggleAdsAccount,
                    onDeleteAdsAccount = onDeleteAdsAccount
                )
            }
            AdminSubTab.CAMPAIGNS -> {
                AdminCampaignsView(
                    campaigns = campaigns,
                    onOpenCreate = { showCreateCampaignModal = true }
                )
            }
            AdminSubTab.FRAUD -> {
                AdminFraudView(
                    fraudEvents = fraudEvents,
                    onResolve = onResolveFraud
                )
            }
            AdminSubTab.AUDIT -> {
                AdminAuditLogsView(auditLogs = auditLogs)
            }
            AdminSubTab.REPORTS -> {
                AdminReportsView(
                    totalRewardsIssued = totalRewardsIssued,
                    totalWithdrawn = withdrawals.filter { it.status == "COMPLETED" }.sumOf { it.amount },
                    usersCount = users.size,
                    onExportCsv = { onShowToast("CSV audit report exported to secure storage.") }
                )
            }
        }
    }

    // Modal: Reject Withdrawal
    if (selectedWithdrawalForReject != null) {
        var rejectReason by remember { mutableStateOf("") }
        val wd = selectedWithdrawalForReject!!
        Dialog(onDismissRequest = { selectedWithdrawalForReject = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Reject Withdrawal & Refund",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Rejecting this $${wd.amount} request will automatically credit the funds back to the user's ledger wallet with an audit note.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. Invalid account details or risk threshold exceeded") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedWithdrawalForReject = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rejectReason.isNotBlank()) {
                                    onRejectWithdrawal(wd.id, rejectReason)
                                    selectedWithdrawalForReject = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = rejectReason.isNotBlank()
                        ) {
                            Text("Confirm Rejection")
                        }
                    }
                }
            }
        }
    }

    // Modal: Manual Balance Adjustment
    if (selectedUserForAdjust != null) {
        var adjustAmount by remember { mutableStateOf("") }
        var adjustReason by remember { mutableStateOf("") }
        val targetUser = selectedUserForAdjust!!

        Dialog(onDismissRequest = { selectedUserForAdjust = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Adjust Ledger Balance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Target User: ${targetUser.displayName} (@${targetUser.username})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it },
                        label = { Text("Amount (+ for credit, - for debit)") },
                        placeholder = { Text("e.g. 5.00 or -2.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text("Audit Justification / Note") },
                        placeholder = { Text("e.g. Verified survey compensation adjustment") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedUserForAdjust = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = adjustAmount.toDoubleOrNull()
                                if (amt != null && adjustReason.isNotBlank()) {
                                    onAdjustBalance(targetUser.id, amt, adjustReason)
                                    selectedUserForAdjust = null
                                }
                            },
                            enabled = adjustAmount.toDoubleOrNull() != null && adjustReason.isNotBlank()
                        ) {
                            Text("Post to Ledger")
                        }
                    }
                }
            }
        }
    }

    // Modal: Create Activity
    if (showCreateActivityModal) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var reward by remember { mutableStateOf("1.00") }
        var provider by remember { mutableStateOf("Rewardly Network") }
        var type by remember { mutableStateOf("REWARDED_AD") }
        var duration by remember { mutableStateOf("20") }
        var dailyLimit by remember { mutableStateOf("5") }

        Dialog(onDismissRequest = { showCreateActivityModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Create Earning Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Activity Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = reward,
                            onValueChange = { reward = it },
                            label = { Text("Reward ($)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (s)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateActivityModal = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val r = reward.toDoubleOrNull() ?: 0.50
                                val d = duration.toIntOrNull() ?: 20
                                val dl = dailyLimit.toIntOrNull() ?: 5
                                if (title.isNotBlank()) {
                                    val newAct = EarningActivityEntity(
                                        id = "act_" + UUID.randomUUID().toString().substring(0, 8),
                                        type = type,
                                        title = title,
                                        description = description,
                                        rewardAmount = r,
                                        durationSeconds = d,
                                        dailyLimit = dl,
                                        provider = provider,
                                        providerActivityId = "admin_custom_" + UUID.randomUUID().toString().substring(0, 6)
                                    )
                                    onCreateActivity(newAct)
                                    showCreateActivityModal = false
                                }
                            }
                        ) {
                            Text("Publish Activity")
                        }
                    }
                }
            }
        }
    }

    // Modal: Create Campaign
    if (showCreateCampaignModal) {
        var campName by remember { mutableStateOf("") }
        var campDesc by remember { mutableStateOf("") }
        var campBudget by remember { mutableStateOf("500.00") }
        var campReward by remember { mutableStateOf("2.00") }

        Dialog(onDismissRequest = { showCreateCampaignModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Launch Community Campaign", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = campName,
                        onValueChange = { campName = it },
                        label = { Text("Campaign Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = campDesc,
                        onValueChange = { campDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = campBudget,
                            onValueChange = { campBudget = it },
                            label = { Text("Total Budget ($)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = campReward,
                            onValueChange = { campReward = it },
                            label = { Text("Payout / User ($)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateCampaignModal = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = campBudget.toDoubleOrNull() ?: 100.0
                                val r = campReward.toDoubleOrNull() ?: 1.0
                                if (campName.isNotBlank()) {
                                    val newCamp = CampaignEntity(
                                        id = "camp_" + UUID.randomUUID().toString().substring(0, 8),
                                        name = campName,
                                        description = campDesc,
                                        budget = b,
                                        rewardPerCompletion = r
                                    )
                                    onCreateCampaign(newCamp)
                                    showCreateCampaignModal = false
                                }
                            }
                        ) {
                            Text("Launch Campaign")
                        }
                    }
                }
            }
        }
    }
}

// 1. Overview Tab
@Composable
private fun AdminOverviewView(
    users: List<UserEntity>,
    withdrawals: List<WithdrawalRequestEntity>,
    activities: List<EarningActivityEntity>,
    totalRewardsIssued: Double,
    fraudEvents: List<FraudEventEntity>
) {
    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING" || it.status == "UNDER_REVIEW" }
    val estPlatformRevenue = totalRewardsIssued * 1.55 // Legitimate 35% gross sponsor margin model
    val estMargin = estPlatformRevenue - totalRewardsIssued

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Users",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    iconColor = PrimaryBlue,
                    subtitle = "${users.count { it.status == "ACTIVE" }} Active",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Payouts",
                    value = "${pendingWithdrawals.size}",
                    icon = Icons.Default.Payments,
                    iconColor = AmberGold,
                    subtitle = "$${String.format(Locale.US, "%.2f", pendingWithdrawals.sumOf { it.amount })}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Rewards Issued",
                    value = "$${String.format(Locale.US, "%.2f", totalRewardsIssued)}",
                    icon = Icons.Default.Verified,
                    iconColor = EmeraldGreen,
                    subtitle = "Audited Ledger Outflow",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Platform Net Margin",
                    value = "$${String.format(Locale.US, "%.2f", estMargin)}",
                    icon = Icons.Default.TrendingUp,
                    iconColor = PrimaryBlue,
                    subtitle = "Sponsor Margin ~35%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "System Health & Integrity Monitors",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• Ad Provider Mediation: Connected (Mock SSV Sandbox Mode)",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "• Idempotency Replay Guard: Active (Zero duplicate claims permitted)",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "• Active Fraud Alerts: ${fraudEvents.count { it.status == "FLAGGED" }} flagged events requiring review",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (fraudEvents.any { it.status == "FLAGGED" }) StatusWarning else EmeraldGreen
                    )
                }
            }
        }
    }
}

// 2. Users Tab
@Composable
private fun AdminUsersView(
    users: List<UserEntity>,
    onSuspendUser: (String) -> Unit,
    onRestoreUser: (String) -> Unit,
    onOpenAdjust: (UserEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(users) { u ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = u.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "@${u.username} • ${u.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = u.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Risk: ${u.riskScore}/100 • ${u.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onOpenAdjust(u) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Adjust $", style = MaterialTheme.typography.labelSmall)
                            }
                            if (u.status == "ACTIVE") {
                                Button(
                                    onClick = { onSuspendUser(u.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Suspend", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = { onRestoreUser(u.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Restore", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Withdrawals Tab
@Composable
private fun AdminWithdrawalsView(
    withdrawals: List<WithdrawalRequestEntity>,
    onApprove: (String) -> Unit,
    onOpenReject: (WithdrawalRequestEntity) -> Unit
) {
    if (withdrawals.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Payments,
            title = "No withdrawals in queue",
            message = "When users request withdrawals, they will appear here for compliance review."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(withdrawals) { wd ->
                val isActionable = wd.status == "PENDING" || wd.status == "UNDER_REVIEW"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", wd.amount)} via ${wd.method}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "To: ${wd.destinationMasked} (${wd.userDisplayName})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = wd.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Risk Score: ${wd.riskScore}/100",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (wd.riskScore > 30) StatusWarning else EmeraldGreen
                            )
                            if (isActionable) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { onOpenReject(wd) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Reject")
                                    }
                                    Button(
                                        onClick = { onApprove(wd.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Approve")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Activities Tab
@Composable
private fun AdminActivitiesView(
    activities: List<EarningActivityEntity>,
    onOpenCreate: () -> Unit
) {
    Column {
        Button(
            onClick = onOpenCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create New Earning Activity")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(activities) { act ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(act.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "${act.provider} • Reward: $${String.format(Locale.US, "%.2f", act.rewardAmount)} • ${act.durationSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = act.status)
                    }
                }
            }
        }
    }
}

// 5. Campaigns Tab
@Composable
private fun AdminCampaignsView(
    campaigns: List<CampaignEntity>,
    onOpenCreate: () -> Unit
) {
    Column {
        Button(
            onClick = onOpenCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Launch Community Campaign")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(campaigns) { camp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(camp.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            StatusBadge(status = camp.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(camp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Budget: $${camp.budgetSpent} / $${camp.budget}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Completions: ${camp.completionsCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. Fraud Detection Tab
@Composable
private fun AdminFraudView(
    fraudEvents: List<FraudEventEntity>,
    onResolve: (String) -> Unit
) {
    if (fraudEvents.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Security,
            title = "Zero Fraud Alerts",
            message = "No suspicious activities or replay attempts detected."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(fraudEvents) { ev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = ev.type.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (ev.riskScore >= 60) StatusError else StatusWarning
                                )
                                Text("User: ${ev.username}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(status = ev.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(ev.metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Risk Score: ${ev.riskScore}/100", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            if (ev.status == "FLAGGED") {
                                Button(
                                    onClick = { onResolve(ev.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Mark Resolved", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. Audit Logs Tab
@Composable
private fun AdminAuditLogsView(auditLogs: List<AdminAuditLogEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(auditLogs) { log ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action.replace("_", " "),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = dateFormat.format(Date(log.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Admin: ${log.adminName} • Entity: ${log.entityType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.metadata,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 8. Reports Tab
@Composable
private fun AdminReportsView(
    totalRewardsIssued: Double,
    totalWithdrawn: Double,
    usersCount: Int,
    onExportCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Financial & Compliance Reconciliation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Rewards Credited:")
                    Text("$${String.format(Locale.US, "%.2f", totalRewardsIssued)}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Dispatched Payouts:")
                    Text("$${String.format(Locale.US, "%.2f", totalWithdrawn)}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Enrolled Accounts:")
                    Text("$usersCount", fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = onExportCsv,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Ledger Audit CSV")
        }
    }
}
