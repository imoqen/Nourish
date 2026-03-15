package uk.co.imoqen.nourish.ui.theme

import android.app.Activity
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
import uk.co.imoqen.nourish.ui.theme.PurpleGrey80

// primary: buttons etc
// onPrimary: on top of primary
// secondary: accent colour for less important buttons
// onSecondary: on top of secondary
// background: screen background
// surface: cards, sheets, containers, etc
// onSurface: on the surface
// error: self explanatory

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

    // surface variant isu sed by navbar so dont edit it.

    // for text boxes etc to stand out
    primaryContainer = TextBoxColourDark,
    onPrimaryContainer = Color.White,

    error = ErrorDark,
    errorContainer = ErrorLight
)

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

    // surface variant isu sed by navbar so dont edit it.

    // for text boxes etc to stand out
    primaryContainer = TextBoxColourLight,
    onPrimaryContainer = Color.Black,

    error = ErrorLight,
    errorContainer = ErrorDark,

    surfaceContainer = SurfaceContainerLight

    //surfaceContainer = PurpleGrey120

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun NourishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // using custom colour scheme rather than dynamic.
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