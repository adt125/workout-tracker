package com.example.workouttracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val AppFontFamily = FontFamily.Default

val ScreenDateStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 28.sp,
    color = TextHi
)

val ScreenTitleStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 24.sp,
    color = TextHi
)

val BigNumberStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 32.sp,
    letterSpacing = (-0.02).em,
    color = TextHi
)

val CardNumberStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 26.sp,
    color = TextHi
)

val SectionTitleStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 18.sp,
    color = TextHi
)

val BodyTextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold, // 700
    fontSize = 15.sp,
    color = TextHi
)

val SecondaryTextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold, // 600
    fontSize = 13.sp,
    color = TextLo
)

val LabelStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold, // 700
    fontSize = 12.sp,
    letterSpacing = 0.08.em,
    color = TextLo
)

val MicroTextStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold, // 700
    fontSize = 11.sp,
    color = TextLo
)

val FitTypography = Typography(
    displayLarge = ScreenDateStyle,
    headlineLarge = BigNumberStyle,
    headlineMedium = CardNumberStyle,
    titleLarge = ScreenTitleStyle,
    titleMedium = SectionTitleStyle,
    bodyLarge = BodyTextStyle,
    bodyMedium = SecondaryTextStyle,
    labelLarge = LabelStyle,
    labelSmall = MicroTextStyle
)

// Alias for Material3 Typography
val Typography = FitTypography
