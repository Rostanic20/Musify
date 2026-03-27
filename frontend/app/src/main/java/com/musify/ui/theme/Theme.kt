package com.musify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MusifyPrimary,
    onPrimary = MusifyTextPrimary,
    primaryContainer = MusifyPrimaryDark,
    onPrimaryContainer = MusifyTextPrimary,
    
    secondary = MusifyAccent,
    onSecondary = MusifyBackground,
    secondaryContainer = MusifyPrimaryLight,
    onSecondaryContainer = MusifyBackground,
    
    tertiary = MusifyPrimaryLight,
    onTertiary = MusifyBackground,
    
    background = MusifyBackground,
    onBackground = MusifyTextPrimary,
    
    surface = MusifySurface,
    onSurface = MusifyTextPrimary,
    surfaceVariant = MusifySurfaceVariant,
    onSurfaceVariant = MusifyTextSecondary,
    
    error = MusifyError,
    onError = MusifyTextPrimary,
    errorContainer = MusifyError.copy(alpha = 0.2f),
    onErrorContainer = MusifyError,
    
    outline = MusifyTextDisabled,
    outlineVariant = MusifySurfaceVariant,
    
    scrim = MusifyBackground.copy(alpha = 0.8f),
)

private val LightColorScheme = lightColorScheme(
    primary = MusifyPrimary,
    onPrimary = MusifyTextPrimary,
    primaryContainer = MusifyPrimaryLight,
    onPrimaryContainer = MusifyBackground,
    
    secondary = MusifyAccent,
    onSecondary = MusifyTextPrimary,
    
    background = MusifyBackgroundLight,
    onBackground = MusifyTextPrimaryLight,
    
    surface = MusifySurfaceLight,
    onSurface = MusifyTextPrimaryLight,
    surfaceVariant = MusifySurfaceLight,
    onSurfaceVariant = MusifyTextSecondaryLight,
    
    error = MusifyError,
    onError = MusifyTextPrimary,
)

@Composable
fun MusifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}