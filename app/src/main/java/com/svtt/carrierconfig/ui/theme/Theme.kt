package com.svtt.carrierconfig.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    primaryContainer = AccentPrimaryDark,
    onPrimaryContainer = AccentPrimaryLight,
    
    secondary = AccentSecondary,
    onSecondary = Color.White,
    secondaryContainer = AccentSecondaryDark,
    onSecondaryContainer = AccentSecondaryLight,
    
    tertiary = AccentSuccess,
    onTertiary = Color.Black,
    tertiaryContainer = AccentSuccessGlow,
    onTertiaryContainer = AccentSuccess,
    
    background = BackgroundDeepDark,
    onBackground = TextPrimary,
    
    surface = BackgroundDark,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundElevated,
    onSurfaceVariant = TextSecondary,
    
    error = AccentError,
    onError = Color.White,
    errorContainer = AccentErrorGlow,
    onErrorContainer = AccentError,
    
    outline = Color.White.copy(alpha = 0.2f),
    outlineVariant = Color.White.copy(alpha = 0.1f),
    
    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun SVTTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDeepDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
            
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = false
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
