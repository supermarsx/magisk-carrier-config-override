package com.supermarsx.carrierconfig.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme with glassmorphism design
 */
private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = BackgroundDeepDark,
    primaryContainer = AccentPrimaryDark,
    onPrimaryContainer = TextPrimary,
    
    secondary = AccentSecondary,
    onSecondary = BackgroundDeepDark,
    secondaryContainer = AccentSecondaryDark,
    onSecondaryContainer = TextPrimary,
    
    tertiary = AccentSuccess,
    onTertiary = BackgroundDeepDark,
    
    error = AccentError,
    onError = BackgroundDeepDark,
    errorContainer = AccentErrorGlow,
    onErrorContainer = TextPrimary,
    
    background = BackgroundDeepDark,
    onBackground = TextPrimary,
    
    surface = BackgroundDark,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundElevated,
    onSurfaceVariant = TextSecondary,
    
    outline = GlassSurfaceMedium,
    outlineVariant = GlassSurface,
)

/**
 * Main theme composable for CCO app
 * Implements glassmorphism dark theme with Material 3
 */
@Composable
fun CCOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Currently only dark themes are supported (glassmorphism design).
    // The darkTheme parameter is accepted so "Auto (System)" doesn't crash;
    // a light scheme can be added later.
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDeepDark.toArgb()
            window.navigationBarColor = BackgroundDeepDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun CarrierConfigTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CCOTheme(darkTheme = darkTheme, content = content)
}
