package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    user: UserEntity?,
    wallet: WalletEntity?,
    activities: List<EarningActivityEntity>,
    transactions: List<WalletTransactionEntity>,
    isDailyClaimed: Boolean,
    currentStreak: Int,
    onClaimDaily: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onStartActivity: (EarningActivityEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_welcome_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (user?.isKycVerified == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "KYC VERIFIED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                        Text(
                            text = user?.displayName ?: "Earner",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Risk Level: ${if ((user?.riskScore ?: 10) < 30) "LOW RISK (Compliant)" else "UNDER REVIEW"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if ((user?.riskScore ?: 10) < 30) EmeraldGreen else StatusWarning
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // 2. High-Fidelity Ledger Wallet Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_wallet_ledger_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBackground
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.7f),
                                    Color(0xFF0F172A),
                                    Color(0xFF064E3B).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VERIFIED LEDGER WALLET",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF94A3B8)
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$${String.format(Locale.US, "%.2f", wallet?.balance ?: 0.0)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Spendable Balance (USD)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Divider(color = Color.White.copy(alpha = 0.15f))

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Pending", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", wallet?.pendingBalance ?: 0.0)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGoldDark
                                )
                            }
                            Column {
                                Text("Lifetime Earned", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", wallet?.lifetimeEarned ?: 0.0)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGreenDark
                                )
                            }
                            Column {
                                Text("Withdrawn", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", wallet?.lifetimeWithdrawn ?: 0.0)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigate(AppScreen.EARN) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_quick_earn_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Earning")
                            }
                            OutlinedButton(
                                onClick = { onNavigate(AppScreen.WITHDRAW) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_quick_withdraw_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw")
                            }
                        }
                    }
                }
            }
        }

        // 3. Daily Streak Bonus Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_streak_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Daily Bonus",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AmberGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${currentStreak}d Streak",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AmberGold
                                    )
                                }
                            }
                            Text(
                                text = if (isDailyClaimed) "Claimed today. Streak protected!" else "Claim +$0.25 bonus + streak multiplier",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onClaimDaily,
                        enabled = !isDailyClaimed,
                        modifier = Modifier.testTag("home_claim_daily_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text(if (isDailyClaimed) "Claimed" else "Claim")
                    }
                }
            }
        }

        // 4. Quick Nav Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Activities",
                    value = "${activities.size} Ready",
                    icon = Icons.Default.SmartDisplay,
                    iconColor = PrimaryBlue,
                    subtitle = "Ads & Surveys",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppScreen.EARN) }
                )
                StatCard(
                    title = "Referrals",
                    value = user?.referralCode ?: "CODE",
                    icon = Icons.Default.GroupAdd,
                    iconColor = EmeraldGreen,
                    subtitle = "$2.00 / Invite",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppScreen.REFERRALS) }
                )
            }
        }

        // 5. Featured Verified Activities Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Earning Activities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigate(AppScreen.EARN) },
                    modifier = Modifier.testTag("home_view_all_activities_button")
                ) {
                    Text("View All")
                }
            }
        }

        items(activities.take(3)) { activity ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_activity_item_${activity.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activity.type) {
                                    "REWARDED_AD" -> Icons.Default.SmartDisplay
                                    "SURVEY" -> Icons.Default.Poll
                                    "OFFER" -> Icons.Default.TaskAlt
                                    else -> Icons.Default.Stars
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activity.provider,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${activity.durationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+$${String.format(Locale.US, "%.2f", activity.rewardAmount)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { onStartActivity(activity) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("home_start_activity_${activity.id}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 6. Recent Transactions Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Ledger Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigate(AppScreen.WALLET) },
                    modifier = Modifier.testTag("home_view_all_transactions_button")
                ) {
                    Text("View Ledger")
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No transactions yet",
                    message = "Complete your first verified rewarded activity to receive funds in your ledger."
                )
            }
        } else {
            items(transactions.take(3)) { tx ->
                val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                val dateStr = dateFormat.format(Date(tx.createdAt))
                val isCredit = tx.amount > 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isCredit) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isCredit) EmeraldGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "$dateStr • ${tx.type}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${if (isCredit) "+" else ""}$${String.format(Locale.US, "%.2f", tx.amount)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCredit) EmeraldGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
