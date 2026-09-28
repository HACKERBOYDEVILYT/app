package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entity.AdsAccountEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import java.util.Locale

@Composable
fun AdsAccountsAdminView(
    adsAccounts: List<AdsAccountEntity>,
    onAddAdsAccount: (networkName: String, label: String, appId: String, rewardedUnitId: String, interstitialUnitId: String, rewardPerAd: Double, isEnabled: Boolean) -> Unit,
    onToggleAdsAccount: (String, Boolean) -> Unit,
    onDeleteAdsAccount: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val activeCount = adsAccounts.count { it.isEnabled }
    val avgReward = if (adsAccounts.isNotEmpty()) adsAccounts.map { it.rewardPerAd }.average() else 0.50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action & Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বিজ্ঞাপন অ্যাকাউন্ট (Ad Networks)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Google AdMob, Unity Ads, AppLovin ইত্যাদি কনফিগার করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_open_add_ads_account")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অ্যাকাউন্ট যোগ", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("মোট নেটওয়ার্ক", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${adsAccounts.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column {
                        Text("সক্রিয় নেটওয়ার্ক", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$activeCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreen))
                    }
                    Column {
                        Text("গড় রিওয়ার্ড রেট", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format(Locale.US, "%.2f", avgReward)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue))
                    }
                }
            }
        }

        // List of Accounts
        if (adsAccounts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Campaign,
                title = "কোনো বিজ্ঞাপন অ্যাকাউন্ট যুক্ত নেই",
                message = "নতুন Google AdMob বা Unity Ads অ্যাকাউন্ট যুক্ত করতে উপরের '+ অ্যাকাউন্ট যোগ' বাটনে চাপ দিন।"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(adsAccounts, key = { it.id }) { account ->
                    AdsAccountCard(
                        account = account,
                        onToggle = { isEnabled -> onToggleAdsAccount(account.id, isEnabled) },
                        onDelete = { onDeleteAdsAccount(account.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAdsAccountDialog(
            onDismiss = { showAddDialog = false },
            onSave = { network, label, appId, rewardedId, interstitialId, reward, enabled ->
                onAddAdsAccount(network, label, appId, rewardedId, interstitialId, reward, enabled)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AdsAccountCard(
    account: AdsAccountEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("ads_account_card_${account.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (account.networkName) {
                                    "Google AdMob" -> Color(0xFFFFB300).copy(alpha = 0.2f)
                                    "Unity Ads" -> Color(0xFF6200EA).copy(alpha = 0.2f)
                                    "AppLovin MAX" -> Color(0xFF00B0FF).copy(alpha = 0.2f)
                                    "Start.io" -> Color(0xFF00C853).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = when (account.networkName) {
                                "Google AdMob" -> Color(0xFFF57F17)
                                "Unity Ads" -> Color(0xFF6200EA)
                                "AppLovin MAX" -> Color(0xFF0288D1)
                                "Start.io" -> Color(0xFF2E7D32)
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = account.accountLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = account.networkName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "রিওয়ার্ড: $${String.format(Locale.US, "%.2f", account.rewardPerAd)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = account.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.testTag("toggle_ads_account_${account.id}")
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App ID / Game ID:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(account.appId, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rewarded Unit ID:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(account.rewardedUnitId, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Medium)
                }
                if (account.interstitialUnitId.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interstitial Unit ID:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(account.interstitialUnitId, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAdsAccountDialog(
    onDismiss: () -> Unit,
    onSave: (network: String, label: String, appId: String, rewardedId: String, interstitialId: String, reward: Double, enabled: Boolean) -> Unit
) {
    val networks = listOf("Google AdMob", "Unity Ads", "AppLovin MAX", "Start.io", "IronSource", "Custom Network")
    var selectedNetwork by remember { mutableStateOf(networks[0]) }
    var accountLabel by remember { mutableStateOf("") }
    var appId by remember { mutableStateOf("") }
    var rewardedUnitId by remember { mutableStateOf("") }
    var interstitialUnitId by remember { mutableStateOf("") }
    var rewardPerAdStr by remember { mutableStateOf("0.50") }
    var isEnabled by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নতুন বিজ্ঞাপন অ্যাকাউন্ট যোগ করুন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Network Selection
                Text("বিজ্ঞাপন নেটওয়ার্ক নির্বাচন করুন:", style = MaterialTheme.typography.labelSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    networks.take(3).chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { net ->
                                FilterChip(
                                    selected = selectedNetwork == net,
                                    onClick = { selectedNetwork = net },
                                    label = { Text(net, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = accountLabel,
                    onValueChange = { accountLabel = it },
                    label = { Text("অ্যাকাউন্টের নাম / লেবেল") },
                    placeholder = { Text("যেমন: AdMob Primary Unit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ads_label")
                )

                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    label = { Text("App ID / Game ID / SDK Key") },
                    placeholder = { Text("ca-app-pub-3940256099942544~3347511713") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ads_appid")
                )

                OutlinedTextField(
                    value = rewardedUnitId,
                    onValueChange = { rewardedUnitId = it },
                    label = { Text("Rewarded Video Unit ID") },
                    placeholder = { Text("ca-app-pub-3940256099942544/5224354917") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ads_rewarded_unit")
                )

                OutlinedTextField(
                    value = interstitialUnitId,
                    onValueChange = { interstitialUnitId = it },
                    label = { Text("Interstitial Unit ID (ঐচ্ছিক)") },
                    placeholder = { Text("ca-app-pub-3940256099942544/1033173712") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rewardPerAdStr,
                    onValueChange = { rewardPerAdStr = it },
                    label = { Text("প্রতি বিজ্ঞাপনে ইউজারকে রিওয়ার্ড ($ USD)") },
                    placeholder = { Text("0.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ads_reward_amount")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("অ্যাকাউন্ট তাৎক্ষণিক সক্রিয় রাখবেন?", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            val label = if (accountLabel.isBlank()) "$selectedNetwork Account" else accountLabel.trim()
                            val app = if (appId.isBlank()) "app_id_${System.currentTimeMillis() % 10000}" else appId.trim()
                            val rew = if (rewardedUnitId.isBlank()) "rewarded_unit_default" else rewardedUnitId.trim()
                            val amt = rewardPerAdStr.toDoubleOrNull() ?: 0.50
                            onSave(selectedNetwork, label, app, rew, interstitialUnitId.trim(), amt, isEnabled)
                        },
                        modifier = Modifier.weight(1f).testTag("btn_save_ads_account"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("সংরক্ষণ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}
