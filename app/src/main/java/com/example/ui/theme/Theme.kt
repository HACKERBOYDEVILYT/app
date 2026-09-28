package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color(0xFF00296B),
    primaryContainer = PrimaryBlueContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = EmeraldGreenDark,
    onSecondary = Color(0xFF003822),
    secondaryContainer = EmeraldGreenContainerDark,
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = AmberGoldDark,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = AmberGoldContainerDark,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = DarkBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = DarkSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldGreenContainer,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AmberGold,
    onTertiary = Color.White,
    tertiaryContainer = AmberGoldContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun RewardlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val customColors = if (darkTheme) {
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
    } else {
        CustomThemeColors(
            cardBackground = LightCardSurface,
            cardBorder = LightBorder,
            subtleText = Color(0xFF64748B),
            success = StatusSuccess,
            warning = StatusWarning,
            error = StatusError,
            info = StatusInfo,
            streakGold = AmberGold,
            rewardGreen = EmeraldGreen
        )
    }

    val customGradients = if (darkTheme) {
        CustomThemeGradients(
            primary = PrimaryGradient,
            emerald = EmeraldGradient,
            amberStreak = AmberStreakGradient,
            card = DarkCardGradient,
            walletHeader = WalletHeaderGradient
        )
    } else {
        CustomThemeGradients(
            primary = PrimaryGradient,
            emerald = EmeraldGradient,
            amberStreak = AmberStreakGradient,
            card = LightCardGradient,
            walletHeader = WalletHeaderGradient
        )
    }

    CompositionLocalProvider(
        LocalCustomThemeColors provides customColors,
        LocalCustomThemeGradients provides customGradients
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = RewardlyShapes,
            content = content
        )
    }
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = RewardlyTheme(darkTheme = darkTheme, content = content)
