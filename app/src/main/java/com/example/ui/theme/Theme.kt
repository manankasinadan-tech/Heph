package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorScheme = darkColorScheme(
    primary = Amber40,
    onPrimary = Color(0xFF241400),
    primaryContainer = Color(0xFF422200),
    onPrimaryContainer = Amber80,
    secondary = AmberGrey80,
    onSecondary = Color(0xFF382E1E),
    tertiary = Bronze80,
    onTertiary = Color(0xFF4A2800),
    background = Charcoal900,
    onBackground = Color(0xFFE2E4E9),
    surface = Charcoal800,
    onSurface = Color(0xFFE2E4E9),
    surfaceVariant = Charcoal700,
    onSurfaceVariant = Color(0xFFC4C7D0),
    outline = CharcoalBorder
)

val LightColorScheme = lightColorScheme(
    primary = AmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB3),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = AmberGrey40,
    onSecondary = Color.White,
    tertiary = Bronze40,
    onTertiary = Color.White,
    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF14171E),
    surface = Color.White,
    onSurface = Color(0xFF14171E),
    surfaceVariant = Color(0xFFE8EAF0),
    onSurfaceVariant = Color(0xFF434752),
    outline = Color(0xFFC7CBD6)
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun HephaistosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Keep distinctive forge identity
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
