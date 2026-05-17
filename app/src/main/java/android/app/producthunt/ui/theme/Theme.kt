package android.app.producthunt.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PH_Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A260E),
    onPrimaryContainer = Color(0xFFFFD8C8),
    secondary = PH_Price_Target,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2600),
    onSecondaryContainer = Color(0xFFFFD9B3),
    tertiary = PH_Progress_Bar,
    onTertiary = Color.White,
    background = PH_Background_Dark,
    onBackground = Color(0xFFF5EFEA),
    surface = PH_Surface_Dark,
    onSurface = Color(0xFFF5EFEA),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCFC7C0),
    outline = Color(0xFF5F5852),
    outlineVariant = Color(0xFF3A352F),
    error = PH_Status_Error_Text,
    errorContainer = Color(0xFF4A1717),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary = PH_Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE1D2),
    onPrimaryContainer = Color(0xFF3C1700),
    secondary = PH_Price_Target,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3C4),
    onSecondaryContainer = Color(0xFF361900),
    tertiary = PH_Progress_Bar,
    onTertiary = Color.White,
    background = PH_Background,
    onBackground = PH_OnBackground,
    surface = PH_Surface,
    onSurface = PH_OnSurface,
    surfaceVariant = Color(0xFFFFEFE4),
    onSurfaceVariant = Color(0xFF6F6258),
    outline = Color(0xFFE8DCD2),
    outlineVariant = Color(0xFFF0E8E0),
    error = PH_Status_Error_Text,
    errorContainer = PH_Status_Error_Bg,
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun AndroidAppProductHuntTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
