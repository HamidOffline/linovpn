package ir.linovpn.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ir.linovpn.app.domain.model.AppThemeMode

// iOS Streisand Official Colors
val IosBlue = Color(0xFF007AFF)
val IosGreen = Color(0xFF34C759)
val IosYellow = Color(0xFFFFCC00)
val IosRed = Color(0xFFFF3B30)

// Light Theme (iOS Grouped Standard)
val IosBgLight = Color(0xFFF2F2F7)
val IosCardLight = Color(0xFFFFFFFF)
val IosSquircleLight = Color(0xFFE5E5EA)
val IosPillLight = Color(0xFFE5E5EA)
val IosSeparatorLight = Color(0xFFE5E5EA)
val IosTextPrimaryLight = Color(0xFF000000)
val IosTextSecondaryLight = Color(0xFF8E8E93)

// Dark Theme (iOS OLED Standard)
val IosBgDark = Color(0xFF000000)
val IosCardDark = Color(0xFF1C1C1E)
val IosSquircleDark = Color(0xFF1C1C1E)
val IosPillDark = Color(0xFF2C2C2E)
val IosSeparatorDark = Color(0xFF2C2C2E)
val IosTextPrimaryDark = Color(0xFFFFFFFF)
val IosTextSecondaryDark = Color(0xFF8E8E93)

private val DarkColorScheme = darkColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosBlue,
    onPrimaryContainer = Color.White,
    secondary = IosBlue,
    onSecondary = Color.White,
    background = IosBgDark,
    onBackground = IosTextPrimaryDark,
    surface = IosCardDark,
    onSurface = IosTextPrimaryDark,
    surfaceVariant = IosSquircleDark,
    onSurfaceVariant = IosTextSecondaryDark,
    outline = IosSeparatorDark,
    error = IosRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosBlue,
    onPrimaryContainer = Color.White,
    secondary = IosBlue,
    onSecondary = Color.White,
    background = IosBgLight,
    onBackground = IosTextPrimaryLight,
    surface = IosCardLight,
    onSurface = IosTextPrimaryLight,
    surfaceVariant = IosSquircleLight,
    onSurfaceVariant = IosTextSecondaryLight,
    outline = IosSeparatorLight,
    error = IosRed,
    onError = Color.White
)

@Composable
fun LinoVpnTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
