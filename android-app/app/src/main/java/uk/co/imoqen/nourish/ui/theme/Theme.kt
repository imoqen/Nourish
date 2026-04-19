package uk.co.imoqen.nourish.ui.theme

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

/**
 * Uses Color to assign colours to Nourish in light and dark mode.
 *  - primary: buttons etc
 *  - onPrimary: on top of primary
 *  - secondary: accent colour for less important buttons
 *  - onSecondary: on top of secondary
 *  - background: screen background
 *  - surface: cards, sheets, containers, etc
 *  - onSurface: on the surface
 *  - error: self explanatory
 *  @author Android Material 3 and Imogen Pemberton, 2026
 */

// Dark mode:
private val NourishDarkColorScheme = darkColorScheme(
    primary = NourishGreenDark,
    secondary = NourishGreenDark,
    tertiary = NourishGreenLight,

    background = PurpleGrey40,
    surface = Purple40,
    onBackground = Color.White,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    primaryContainer = TextBoxColourDark,
    onPrimaryContainer = Color.White,

    error = ErrorDark,
    errorContainer = ErrorLight
)

// Light mode:
private val NourishLightColorScheme = lightColorScheme(
    primary = NourishGreen,
    secondary = NourishGreenDark,
    tertiary = NourishGreenDark,

    background = Color.White,
    surface = Purple80,
    onBackground = Color.Black,

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,

    primaryContainer = TextBoxColourLight,
    onPrimaryContainer = Color.Black,

    error = ErrorLight,
    errorContainer = ErrorDark,

    surfaceContainer = SurfaceContainerLight
)

/**
 * NourishTheme, to access the colours in both light and dark mode.
 */
@Composable
fun NourishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Using custom colour scheme rather than dynamic.
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> NourishDarkColorScheme
        else -> NourishLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}