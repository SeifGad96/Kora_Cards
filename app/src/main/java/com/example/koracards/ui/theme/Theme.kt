package com.example.koracards.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    background = BrandBackground,
    surface = CardDarkSurface,
    error = FeedbackRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BrandBackground,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    background = Color(0xFFF8F9FA),
    surface = CardLightSurface,
    error = FeedbackRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BrandBackground,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    onError = Color.White
)

@Composable
fun KoraCardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = if (isArabic) ArabicTypography else EnglishTypography
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = KoraShapes,
            content = content
        )
    }
}