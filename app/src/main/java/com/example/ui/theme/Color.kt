package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Sapphire / Electric Indigo
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueDark = Color(0xFF60A5FA)
val PrimaryBlueContainer = Color(0xFFDBEAFE)
val PrimaryBlueContainerDark = Color(0xFF1E3A8A)

// Emerald Rewards Green
val EmeraldGreen = Color(0xFF059669)
val EmeraldGreenDark = Color(0xFF34D399)
val EmeraldGreenContainer = Color(0xFFD1FAE5)
val EmeraldGreenContainerDark = Color(0xFF064E3B)

// Amber Streak / Bonus Gold Accent
val AmberGold = Color(0xFFD97706)
val AmberGoldDark = Color(0xFFFBBF24)
val AmberGoldContainer = Color(0xFFFEF3C7)
val AmberGoldContainerDark = Color(0xFF78350F)

// Dark Theme Surfaces & Finishes
val DarkBackground = Color(0xFF0A0F1D)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkSurfaceElevated = Color(0xFF243048)
val DarkCardSurface = Color(0xFF161F33)
val DarkBorder = Color(0xFF374151)
val DarkBorderSubtle = Color(0xFF1F2937)

// Light Theme Surfaces & Finishes
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightCardSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)
val LightBorderSubtle = Color(0xFFF1F5F9)

// Semantic Status Colors
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF3B82F6)

// Risk Assessment Tiers
val RiskLow = Color(0xFF10B981)
val RiskMedium = Color(0xFFF59E0B)
val RiskHigh = Color(0xFFEA580C)
val RiskCritical = Color(0xFFDC2626)

// Tier Accents
val TierBronze = Color(0xFFCD7F32)
val TierSilver = Color(0xFF94A3B8)
val TierGold = Color(0xFFF59E0B)
val TierPlatinum = Color(0xFF6366F1)
val TierDiamond = Color(0xFF06B6D4)

// Custom Design Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF7C3AED))
)

val EmeraldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
)

val AmberStreakGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24))
)

val DarkCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)

val WalletHeaderGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
)

val LightCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
)
