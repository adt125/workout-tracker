package com.example.workouttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val FitScheme = darkColorScheme(
    background = Ground,
    onBackground = TextHi,
    surface = Card,
    onSurface = TextHi,
    surfaceVariant = Raised,
    onSurfaceVariant = TextLo,
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Accent.copy(alpha = 0.18f),
    onPrimaryContainer = Accent,
    secondary = Accent2,
    onSecondary = Ground,
    secondaryContainer = Accent2.copy(alpha = 0.18f),
    onSecondaryContainer = Accent2,
    outline = CardBorder
)

@Immutable
data class ExtraColors(
    val positive: Color,
    val accent2: Color,
    val progressTrack: Color,
    val cardBorder: Color
)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(
        positive = Positive,
        accent2 = Accent2,
        progressTrack = ProgressTrack,
        cardBorder = CardBorder
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalExtraColors provides ExtraColors(
            positive = Positive,
            accent2 = Accent2,
            progressTrack = ProgressTrack,
            cardBorder = CardBorder
        )
    ) {
        MaterialTheme(
            colorScheme = FitScheme,
            typography = FitTypography,
            content = content
        )
    }
}

@Composable
fun WorkoutTrackerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AppTheme(content = content)
}
