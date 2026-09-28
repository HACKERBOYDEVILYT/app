package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CustomThemeColors(
    val cardBackground: Color,
    val cardBorder: Color,
    val subtleText: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val streakGold: Color,
    val rewardGreen: Color
)

data class CustomThemeGradients(
    val primary: Brush,
    val emerald: Brush,
    val amberStreak: Brush,
    val card: Brush,
    val walletHeader: Brush
)

val LocalCustomThemeColors = staticCompositionLocalOf {
    CustomThemeColors(
        cardBackground = DarkCardSurface,
        cardBorder = DarkBorder,
        subtleText = Color(0xFF9CA3AF),
        success = StatusSuccess,
        warning = StatusWarning,
        error = StatusError,
        info = StatusInfo,
        streakGold = AmberGoldDark,
        rewardGreen = EmeraldGreenDark
    )
}

val LocalCustomThemeGradients = staticCompositionLocalOf {
    CustomThemeGradients(
        primary = PrimaryGradient,
        emerald = EmeraldGradient,
        amberStreak = AmberStreakGradient,
        card = DarkCardGradient,
        walletHeader = WalletHeaderGradient
    )
}

object RewardlyThemeTokens {
    val colors: CustomThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomThemeColors.current

    val gradients: CustomThemeGradients
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomThemeGradients.current
}
