package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.util.AppLanguage
import com.example.ui.util.Strings
import com.example.ui.viewmodel.ADMIN_MASTER_PASSWORD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLoginDemoUser: () -> Unit,
    onLoginUser: (String) -> Unit,
    onRegisterUser: (String, String, String, String?) -> Unit,
    onLoginAdmin: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Register, 2: Admin
    val isBn = language == AppLanguage.BANGLA

    // Login state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register state
    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRefCode by remember { mutableStateOf("") }

    // Admin state
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordVisible by remember { mutableStateOf(false) }
    var adminError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Language switcher chip at top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = true,
                onClick = onToggleLanguage,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = if (isBn) "বাংলা (BN)" else "English (EN)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("auth_lang_toggle")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Branding Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "REWARDLY",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldGreen.copy(alpha = 0.15f),
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = if (isBn) "ভেরিফাইড" else "VERIFIED",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = EmeraldGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = if (isBn) "ভেরিফাইড কাজ করে আসল রিওয়ার্ড আয় করুন ও সহজে উত্তোলন করুন"
                   else "Complete legitimate tasks, earn real rewards & withdraw instantly",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("auth_tab_row")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isBn) "লগইন" else "Sign In", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isBn) "রেজিস্ট্রেশন" else "Sign Up", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isBn) "অ্যাডমিন" else "Admin", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card container for tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    // TAB 0: LOGIN
                    0 -> {
                        Text(
                            text = if (isBn) "আপনার অ্যাকাউন্টে লগইন করুন" else "Welcome Back Earner",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text(if (isBn) "ইউজারনেম বা ইমেইল" else "Username or Email") },
                            placeholder = { Text("alex_earner") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text(if (isBn) "পাসওয়ার্ড" else "Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Button(
                            onClick = {
                                if (loginUsername.isNotBlank()) {
                                    onLoginUser(loginUsername.trim())
                                } else {
                                    onLoginDemoUser()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_login"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "লগইন করুন" else "Sign In", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        // Quick demo earner login button
                        OutlinedButton(
                            onClick = onLoginDemoUser,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_quick_demo_earner"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "Alex Johnson (ডেমো ইউজার হিসেবে প্রবেশ)" else "Alex Johnson (Continue as Demo)",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // TAB 1: REGISTER
                    1 -> {
                        Text(
                            text = if (isBn) "নতুন অ্যাকাউন্ট তৈরি করুন" else "Create New Earner Account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text(if (isBn) "পুরো নাম" else "Full Name") },
                            placeholder = { Text("Rahim Ahmed") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                        )

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text(if (isBn) "ইউজারনেম" else "Username") },
                            placeholder = { Text("rahim2026") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_username_input")
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text(if (isBn) "ইমেইল এড্রেস" else "Email Address") },
                            placeholder = { Text("rahim@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text(if (isBn) "পাসওয়ার্ড" else "Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                        )

                        OutlinedTextField(
                            value = regRefCode,
                            onValueChange = { regRefCode = it },
                            label = { Text(if (isBn) "রেফারেল কোড (ঐচ্ছিক)" else "Referral Code (Optional)") },
                            placeholder = { Text("ALEX777") },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_ref_input")
                        )

                        Button(
                            onClick = {
                                val name = if (regName.isBlank()) "New Earner" else regName.trim()
                                val uname = if (regUsername.isBlank()) "user_${System.currentTimeMillis() % 10000}" else regUsername.trim()
                                val email = if (regEmail.isBlank()) "$uname@rewardly.io" else regEmail.trim()
                                onRegisterUser(name, email, uname, regRefCode.takeIf { it.isNotBlank() })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_register"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "অ্যাকাউন্ট তৈরি করুন ও শুরু করুন" else "Register & Start Earning", fontWeight = FontWeight.Bold)
                        }
                    }

                    // TAB 2: ADMIN PORTAL
                    2 -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isBn) "মাস্টার অ্যাডমিন পোর্টাল" else "Master Admin Portal",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = if (isBn) "বিজ্ঞাপন অ্যাকাউন্ট, উইথড্রয়াল ও ইউজার কন্ট্রোল করতে পাসওয়ার্ড দিন।"
                                               else "Enter admin password to manage ads accounts, payouts & users.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = {
                                adminPasswordInput = it
                                adminError = null
                            },
                            label = { Text(if (isBn) "অ্যাডমিন পাসওয়ার্ড (robiul1000)" else "Admin Password (robiul1000)") },
                            placeholder = { Text("robiul1000") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                    Icon(
                                        imageVector = if (adminPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            isError = adminError != null,
                            supportingText = {
                                adminError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (adminPasswordInput.trim() == ADMIN_MASTER_PASSWORD) {
                                        onLoginAdmin(adminPasswordInput.trim())
                                    } else {
                                        adminError = if (isBn) "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন (robiul1000)।"
                                                     else "Incorrect password! Enter robiul1000."
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_portal_password_input")
                        )

                        Button(
                            onClick = {
                                if (adminPasswordInput.trim() == ADMIN_MASTER_PASSWORD) {
                                    onLoginAdmin(adminPasswordInput.trim())
                                } else {
                                    adminError = if (isBn) "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন (robiul1000)।"
                                                 else "Incorrect password! Enter robiul1000."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_unlock_admin_portal"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "অ্যাডমিন প্যানেল আনলক করুন" else "Unlock Admin Console",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Helper hint
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isBn) "ডিফল্ট অ্যাডমিন পাসওয়ার্ড: robiul1000"
                                           else "Default Admin Password: robiul1000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
