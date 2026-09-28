package com.example.ui.screens.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WalletEntity
import com.example.data.local.entity.WithdrawalRequestEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

data class PayoutMethod(
    val id: String,
    val name: String,
    val description: String,
    val fee: Double,
    val placeholder: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    wallet: WalletEntity?,
    withdrawals: List<WithdrawalRequestEntity>,
    onSubmitWithdrawal: (Double, String, String) -> Unit
) {
    val methods = listOf(
        PayoutMethod(
            id = "PAYPAL",
            name = "PayPal Payout",
            description = "Dispatched within 12h • $0.25 fee",
            fee = 0.25,
            placeholder = "Enter verified PayPal email (e.g. user@gmail.com)",
            icon = Icons.Default.Payment
        ),
        PayoutMethod(
            id = "CRYPTO_USDC",
            name = "Crypto USDC (Base/Polygon)",
            description = "Fast settlement • $0.50 fee",
            fee = 0.50,
            placeholder = "Enter 0x... EVM wallet address",
            icon = Icons.Default.CurrencyBitcoin
        ),
        PayoutMethod(
            id = "BANK_ACH",
            name = "Direct ACH Bank Transfer",
            description = "US Banks • 1-2 business days • $0.35 fee",
            fee = 0.35,
            placeholder = "Enter routing & account number",
            icon = Icons.Default.AccountBalance
        ),
        PayoutMethod(
            id = "AMAZON_CARD",
            name = "Amazon Gift Card (Digital)",
            description = "Zero fees • Instant digital code",
            fee = 0.00,
            placeholder = "Enter recipient email for digital code",
            icon = Icons.Default.CardGiftcard
        )
    )

    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var amountInput by remember { mutableStateOf("") }
    var destinationInput by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
    val netAmount = (enteredAmount - selectedMethod.fee).coerceAtLeast(0.0)
    val availableBalance = wallet?.balance ?: 0.0
    val isAmountValid = enteredAmount >= 5.0 && enteredAmount <= availableBalance
    val isDestinationValid = destinationInput.isNotBlank() && destinationInput.length >= 4

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Available Balance Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_balance_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE FOR WITHDRAWAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", availableBalance)} USD",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Min. threshold: $5.00 • Server-side verification required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 2. Select Payout Method
        item {
            Text(
                text = "1. Select Payout Method",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(methods) { method ->
            val isSelected = selectedMethod.id == method.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMethod = method }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("withdraw_method_${method.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = method.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = method.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = method.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedMethod = method }
                    )
                }
            }
        }

        // 3. Amount & Destination Form
        item {
            Text(
                text = "2. Enter Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (USD)") },
                        placeholder = { Text("e.g. 10.00") },
                        prefix = { Text("$", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        isError = amountInput.isNotBlank() && !isAmountValid,
                        supportingText = {
                            if (amountInput.isNotBlank() && enteredAmount < 5.0) {
                                Text("Minimum withdrawal is $5.00")
                            } else if (enteredAmount > availableBalance) {
                                Text("Amount exceeds available balance ($${String.format(Locale.US, "%.2f", availableBalance)})")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = destinationInput,
                        onValueChange = { destinationInput = it },
                        label = { Text("Destination Account / Address") },
                        placeholder = { Text(selectedMethod.placeholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_destination_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gross Amount:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${String.format(Locale.US, "%.2f", enteredAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Processing Fee (${selectedMethod.name}):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("-$${String.format(Locale.US, "%.2f", selectedMethod.fee)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Payout Delivered:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "$${String.format(Locale.US, "%.2f", netAmount)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = isAmountValid && isDestinationValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("withdraw_submit_request_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review & Submit Withdrawal")
                    }
                }
            }
        }

        // 4. Past Withdrawal Requests Tracker
        item {
            Text(
                text = "Past Payout Requests (${withdrawals.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No past withdrawals",
                    message = "When you request payouts, their live compliance status and delivery tracking will appear here."
                )
            }
        } else {
            items(withdrawals) { req ->
                val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_item_${req.id}"),
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
                                    text = "$${String.format(Locale.US, "%.2f", req.amount)} via ${req.method}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = req.destinationMasked,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = req.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Requested: ${dateFormat.format(Date(req.createdAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Net: $${String.format(Locale.US, "%.2f", req.netAmount)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (req.rejectionReason != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reason: ${req.rejectionReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Payout Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please confirm that your payout details are accurate. Once submitted, requests undergo compliance and fraud checks.")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("• Method: ${selectedMethod.name}", fontWeight = FontWeight.SemiBold)
                    Text("• Destination: $destinationInput", fontWeight = FontWeight.SemiBold)
                    Text("• Gross Amount: $${String.format(Locale.US, "%.2f", enteredAmount)}", fontWeight = FontWeight.SemiBold)
                    Text("• Net Payout: $${String.format(Locale.US, "%.2f", netAmount)}", fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSubmitWithdrawal(enteredAmount, selectedMethod.id, destinationInput)
                        amountInput = ""
                        destinationInput = ""
                    },
                    modifier = Modifier.testTag("confirm_withdrawal_dialog_button")
                ) {
                    Text("Submit Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
