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
    secondary = PH_Price_Target, // Sử dụng màu cam đậm làm màu phụ
    tertiary = PH_Progress_Bar,  // Sử dụng màu xanh mòng két làm màu nhấn (tertiary)
    background = PH_OnBackground, // Dùng màu xám đậm (2D2D2D) cho nền Dark Mode
    surface = PH_OnBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White, // Chữ trắng trên nền tối
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PH_Primary,
    secondary = PH_Price_Target,
    tertiary = PH_Progress_Bar,
    background = PH_Background, // Dùng màu kem nhạt (FFF8F1) cho nền Light Mode
    surface = PH_Surface,       // Trắng tinh cho các bề mặt (Card, Dialog)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = PH_OnBackground, // Chữ xám đậm trên nền sáng
    onSurface = PH_OnSurface
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