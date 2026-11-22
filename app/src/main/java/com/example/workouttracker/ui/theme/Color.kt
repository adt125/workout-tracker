package com.example.workouttracker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Exact Color Tokens from Fitness App Redesign.html
val Ground     = Color(0xFF080B18) // Screen background base
val Card       = Color(0xFF0F1426) // Cards, tiles, bottom nav bar
val CardBorder = Color(0xFF1B2240) // Card borders
val Raised     = Color(0xFF151B33) // Inputs, chips, elevated elements
val ProgressTrack = Color(0xFF2B3354) // Progress tracks and dividers
val SnackbarBg = Color(0xFF1C2340) // Snackbar container

val TextHi     = Color(0xFFF1F3FA) // Primary text
val TextLo     = Color(0xFF98A2BE) // Secondary text and labels

val CoralAccent = Color(0xFFF28B6B) // Start of primary button gradient
val RoseAccent  = Color(0xFFE8567C) // Primary button accent (solid fallback)

val TealAccent  = Color(0xFF4CC38A) // Start of progress fill gradient
val MintAccent  = Color(0xFF5ECF9B) // Secondary accent / Positive text (solid fallback)

val Accent     = RoseAccent
val Accent2    = MintAccent
val Positive   = MintAccent

// Gradients matching HTML design
val PrimaryButtonBrush = Brush.horizontalGradient(
    colors = listOf(CoralAccent, RoseAccent)
)

val ProgressBrush = Brush.horizontalGradient(
    colors = listOf(TealAccent, MintAccent)
)

val ScreenBackgroundBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF1B1D52), Ground)
)

// Backward-compatibility aliases
val AppBackground = Ground
val AppSurface = Card
val CardBackground = Card
val InputBackground = Raised
val AccentPurple = RoseAccent
val AccentGreen = MintAccent
val AccentBlue = TealAccent
val TextPrimary = TextHi
val TextSecondary = TextLo
val MutedText = TextLo

val ChartPurple = MintAccent
val ChartCyan = TealAccent
val SuccessBanner = MintAccent.copy(alpha = 0.15f)
val SuccessBorder = MintAccent
