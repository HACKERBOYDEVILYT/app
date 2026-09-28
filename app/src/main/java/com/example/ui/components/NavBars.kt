package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.util.AppLanguage
import com.example.ui.viewmodel.AppScreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardlyTopBar(
    user: UserEntity?,
    wallet: WalletEntity?,
    unreadNotifs: Int,
    currentScreen: AppScreen,
    language: AppLanguage = AppLanguage.BANGLA,
    onNotificationsClick: () -> Unit,
    onRoleSwitchClick: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val isBn = language == AppLanguage.BANGLA

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isBn) "রিওয়ার্ডলি" else "REWARDLY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBn) "ভেরিফাইড" else "VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                        }
                    }
                    Text(
                        text = if (user?.role == "SUPER_ADMIN" || user?.role == "ADMIN") {
                            if (isBn) "সুপার অ্যাডমিন কনসোল" else "Super Admin Console"
                        } else {
                            if (isBn) "আসল রিওয়ার্ড প্ল্যাটফর্ম" else "Legitimate Rewards Platform"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Balance Pill
            if (wallet != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("top_bar_balance_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", wallet.balance)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Language Switcher button
            IconButton(
                onClick = onToggleLanguage,
                modifier = Modifier.testTag("top_bar_lang_toggle")
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = if (isBn) "EN" else "বাং",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Notification Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("top_bar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifs > 0) {
                            Badge { Text("$unreadNotifs") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.NOTIFICATIONS) Icons.Default.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }

            // Role Switcher / Admin Button
            IconButton(
                onClick = onRoleSwitchClick,
                modifier = Modifier.testTag("top_bar_role_switch_button")
            ) {
                Icon(
                    imageVector = if (user?.role == "SUPER_ADMIN") Icons.Default.AdminPanelSettings else Icons.Outlined.SwitchAccount,
                    contentDescription = "Switch Account Perspective",
                    tint = if (user?.role == "SUPER_ADMIN") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            }

            // Logout icon button
            if (user != null) {
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("top_bar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun RewardlyBottomBar(
    currentScreen: AppScreen,
    isAdminUser: Boolean,
    language: AppLanguage = AppLanguage.BANGLA,
    onTabSelected: (AppScreen) -> Unit
) {
    val isBn = language == AppLanguage.BANGLA

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onTabSelected(AppScreen.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.HOME) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Home"
                )
            },
            label = { Text(if (isBn) "হোম" else "Home", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_tab_home")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.EARN,
            onClick = { onTabSelected(AppScreen.EARN) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.EARN) Icons.Default.Stars else Icons.Outlined.Stars,
                    contentDescription = "Earn"
                )
            },
            label = { Text(if (isBn) "আয়" else "Earn", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_tab_earn")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.WALLET,
            onClick = { onTabSelected(AppScreen.WALLET) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.WALLET) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                    contentDescription = "Wallet"
                )
            },
            label = { Text(if (isBn) "ওয়ালেট" else "Wallet", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_tab_wallet")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.WITHDRAW,
            onClick = { onTabSelected(AppScreen.WITHDRAW) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.WITHDRAW) Icons.Default.Payments else Icons.Outlined.Payments,
                    contentDescription = "Withdraw"
                )
            },
            label = { Text(if (isBn) "উত্তোলন" else "Withdraw", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_tab_withdraw")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.REFERRALS,
            onClick = { onTabSelected(AppScreen.REFERRALS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.REFERRALS) Icons.Default.GroupAdd else Icons.Outlined.GroupAdd,
                    contentDescription = "Referrals"
                )
            },
            label = { Text(if (isBn) "রেফারেল" else "Referrals", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_tab_referrals")
        )
        if (isAdminUser) {
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_PANEL,
                onClick = { onTabSelected(AppScreen.ADMIN_PANEL) },
                icon = {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.ADMIN_PANEL) Icons.Default.Shield else Icons.Outlined.Shield,
                        contentDescription = "Admin"
                    )
                },
                label = { Text(if (isBn) "অ্যাডমিন" else "Admin", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("nav_tab_admin")
            )
        } else {
            NavigationBarItem(
                selected = currentScreen == AppScreen.PROFILE,
                onClick = { onTabSelected(AppScreen.PROFILE) },
                icon = {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text(if (isBn) "প্রোফাইল" else "Profile", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("nav_tab_profile")
            )
        }
    }
}

