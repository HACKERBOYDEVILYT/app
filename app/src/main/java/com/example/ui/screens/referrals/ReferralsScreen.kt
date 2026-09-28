package com.example.ui.screens.referrals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun ReferralsScreen(
    user: UserEntity?,
    referrals: List<ReferralEntity>,
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val referralCode = user?.referralCode ?: "REWARDLY"
    val referralUrl = "https://rewardly.io/register?ref=$referralCode"

    val verifiedCount = referrals.count { it.status == "VERIFIED_EARNER" }
    val totalReferralEarned = referrals.filter { it.status == "VERIFIED_EARNER" }.sumOf { it.rewardAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Referral Link & Code Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("referrals_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "INVITE FRIENDS & EARN $2.00",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Earn $2.00 For Every Verified Friend",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Rewards are automatically credited to your ledger once your referral completes $5.00 in verified activities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Code Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR REFERRAL CODE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text(
                                text = referralCode,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(referralCode))
                                onShowToast("Referral code copied to clipboard!")
                            },
                            modifier = Modifier.testTag("copy_referral_code_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(referralUrl))
                            onShowToast("Share link copied: $referralUrl")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("share_referral_link_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Invite Link")
                    }
                }
            }
        }

        // 2. Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Invites",
                    value = "${referrals.size}",
                    icon = Icons.Default.People,
                    iconColor = PrimaryBlue,
                    subtitle = "Signed Up",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Verified",
                    value = "$verifiedCount",
                    icon = Icons.Default.Verified,
                    iconColor = EmeraldGreen,
                    subtitle = "$2.00 Unlocked",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = "Total Referral Income",
                value = "$${String.format(java.util.Locale.US, "%.2f", totalReferralEarned)} USD",
                icon = Icons.Default.MonetizationOn,
                iconColor = EmeraldGreen,
                subtitle = "Credited directly to ledger",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Referral List
        item {
            Text(
                text = "Referred Friends (${referrals.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (referrals.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.GroupAdd,
                    title = "No referrals yet",
                    message = "Share your link with colleagues and community members to start earning milestone bonuses."
                )
            }
        } else {
            items(referrals) { ref ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("referral_item_${ref.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = ref.referredUsername,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = ref.milestoneRequirement,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            StatusBadge(status = ref.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress to $5 milestone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Milestone Completion:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ref.milestoneProgress}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ref.milestoneProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (ref.milestoneProgress >= 100) EmeraldGreen else PrimaryBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
