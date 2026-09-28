package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.util.AppLanguage
import com.example.ui.viewmodel.AppScreen

@Composable
fun ProfileScreen(
    user: UserEntity?,
    language: AppLanguage = AppLanguage.BANGLA,
    onNavigate: (AppScreen) -> Unit,
    onSwitchRole: (String) -> Unit,
    onToggleLanguage: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val isBn = language == AppLanguage.BANGLA

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user?.displayName ?: if (isBn) "ইউজার প্রোফাইল" else "Earner Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${user?.username ?: "user"} • ${user?.email ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(status = user?.status ?: "ACTIVE")
                        StatusBadge(status = user?.role ?: "USER")
                    }
                }
            }
        }

        // 2. Demo Perspective Switcher / Admin Login
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "রোল পরিবর্তন ও অ্যাডমিন প্রবেশ" else "Role Switcher & Admin Portal",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBn) "সাধারণ ইউজার হিসেবে টাস্ক করুন অথবা অ্যাডমিন পাসওয়ার্ড দিয়ে অ্যাডমিন মোডে যান।"
                               else "Test as a verified earner or authenticate with admin password for Super Admin tools.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSwitchRole("USER") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("switch_to_user_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user?.role == "USER") PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isBn) "ইউজার মোড" else "Alex (User)", color = if (user?.role == "USER") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { onSwitchRole("ADMIN") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("switch_to_admin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user?.role == "SUPER_ADMIN") PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isBn) "অ্যাডমিন মোড" else "Admin Mode", color = if (user?.role == "SUPER_ADMIN") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // 3. Security & Anti-Fraud Health
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "অ্যাকাউন্ট নিরাপত্তা ও ফ্রড রেটিং" else "Account Security & Anti-Fraud Health",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBn) "রিস্ক স্কোর:" else "Risk Score:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${user?.riskScore ?: 10}/100 (${if (isBn) "নিরাপদ" else "Compliant"})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBn) "কেওয়াইসি ভেরিফিকেশন:" else "KYC Identity Status:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (user?.isKycVerified == true) (if (isBn) "ভেরিফাইড লেভেল ২" else "Verified Level 2") else (if (isBn) "আনভেরিফাইড" else "Unverified"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBn) "দেশ ও টাইমজোন:" else "Country / Jurisdiction:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${user?.country ?: "BD"} (${user?.timezone ?: "Asia/Dhaka"})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // 4. Navigation Menu Items
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Language,
                        title = if (isBn) "ভাষা পরিবর্তন (Language)" else "Switch Language",
                        subtitle = if (isBn) "বর্তমান ভাষা: বাংলা (English এ যেতে চাপুন)" else "Current: English (Tap for Bangla)",
                        onClick = onToggleLanguage
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = if (isBn) "নোটিফিকেশন ও ঘোষণা" else "Notifications & Announcements",
                        subtitle = if (isBn) "রিওয়ার্ড এলার্ট, উত্তোলন স্ট্যাটাস ও আপডেট" else "Reward alerts, payouts, and system announcements",
                        onClick = { onNavigate(AppScreen.NOTIFICATIONS) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = if (isBn) "হেল্পডেস্ক ও সাপোর্ট টিকিট" else "Support Tickets & Helpdesk",
                        subtitle = if (isBn) "পেমেন্ট বা অ্যাকাউন্ট সমস্যায় যোগাযোগ করুন" else "Contact compliance, billing, and technical staff",
                        onClick = { onNavigate(AppScreen.SUPPORT) }
                    )
                    if (user?.role in listOf("SUPER_ADMIN", "ADMIN")) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ProfileMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            title = if (isBn) "অ্যাডমিন ম্যানেজমেন্ট কনসোল" else "Admin Management Console",
                            subtitle = if (isBn) "ইউজার, বিজ্ঞাপন অ্যাকাউন্ট, ফ্রড ও রিপোর্ট কন্ট্রোল" else "Access users, ads accounts, fraud engine, and reports",
                            onClick = { onNavigate(AppScreen.ADMIN_PANEL) }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ProfileMenuItem(
                        icon = Icons.Default.Logout,
                        title = if (isBn) "লগআউট করুন" else "Log Out",
                        subtitle = if (isBn) "বর্তমান সেশন থেকে বের হয়ে যান" else "Sign out of your account",
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

