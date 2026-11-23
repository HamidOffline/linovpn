package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// LinoVPN Logo-Matched Palette (Electric Cyan -> Azure Blue -> Deep Royal Sapphire)
private val LightColor = lightColorScheme(
    primary = Color(0xFF006AE6), // Signature Lino Azure Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6EAFF), // Soft Sky Blue
    onPrimaryContainer = Color(0xFF002359), // Deep Royal Navy
    secondary = Color(0xFF00B4E6), // Lino Electric Cyan
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFF5FF), // Ice Cyan
    onSecondaryContainer = Color(0xFF003547),
    tertiary = Color(0xFF00A878), // Emerald Ping Green
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC5F7E5),
    onTertiaryContainer = Color(0xFF00291D),
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4F8FF), // Clean Glacier Ice
    onBackground = Color(0xFF0C1829), // Deep Navy Ink
    surface = Color(0xFFF8FBFF), // Crisp Cool White
    onSurface = Color(0xFF0C1829),
    surfaceVariant = Color(0xFFE3EEF9), // Soft Blue-Gray
    onSurfaceVariant = Color(0xFF44566C),
    outline = Color(0xFF7B93B0),
    outlineVariant = Color(0xFFCDDDF0),
    inverseSurface = Color(0xFF152236),
    inverseOnSurface = Color(0xFFEBF3FF),
    inversePrimary = Color(0xFF38C6FF),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF006AE6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEEF5FF),
    surfaceContainerHigh = Color(0xFFE5F0FF),
    surfaceContainerHighest = Color(0xFFD9E8FC),
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFF2CC5FF), // Luminous Lino Cyan-Blue
    onPrimary = Color(0xFF002642),
    primaryContainer = Color(0xFF004599), // Royal Sapphire Blue
    onPrimaryContainer = Color(0xFFD6EEFF),
    secondary = Color(0xFF1A8CFF), // Bright Ocean Azure
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF0D3366),
    onSecondaryContainer = Color(0xFFCCE5FF),
    tertiary = Color(0xFF00E0A4), // Neon Mint Ping
    onTertiary = Color(0xFF003324),
    tertiaryContainer = Color(0xFF00523B),
    onTertiaryContainer = Color(0xFFA8F7DF),
    error = Color(0xFFFF6B7A),
    errorContainer = Color(0xFF7A1424),
    onError = Color(0xFF4F000B),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF08101E), // Deep Midnight Sapphire
    onBackground = Color(0xFFE8F1FF),
    surface = Color(0xFF0B1526), // Dark Sapphire Surface
    onSurface = Color(0xFFE8F1FF),
    surfaceVariant = Color(0xFF1A2B45),
    onSurfaceVariant = Color(0xFF9EB4D1),
    outline = Color(0xFF4B6485),
    outlineVariant = Color(0xFF203452),
    inverseSurface = Color(0xFFE8F1FF),
    inverseOnSurface = Color(0xFF0B1526),
    inversePrimary = Color(0xFF006AE6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF2CC5FF),
    surfaceContainerLowest = Color(0xFF060C17),
    surfaceContainerLow = Color(0xFF0F1C31),
    surfaceContainer = Color(0xFF13223B),
    surfaceContainerHigh = Color(0xFF182A47),
    surfaceContainerHighest = Color(0xFF203659),
)

// Semantic Colors (matched to LinoVPN brand identity)
val colorPing = Color(0xFF00B87C) // Fresh Emerald Green
val colorPingRed = Color(0xFFE53955) // Crisp Coral Red
val colorConfigType = Color(0xFF0091EA) // Lino Cyan-Blue
val colorFabActive = Color(0xFF0084FF) // Lino Signature Azure
val colorFabInactiveLight = Color(0xFF6B84A3) // Cool Slate-Blue
val colorFabInactiveDark = Color(0xFF283D5A) // Deep Steel Navy
val dividerColorLight = Color(0xFFD5E3F5)
val dividerColorDark = Color(0xFF1F3352)

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9102138)
val toastNormalBgDark = Color(0xD91A3050)
val toastSuccessBg = Color(0xD900895B)
val toastErrorBg = Color(0xD9C62828)
val toastInfoBg = Color(0xD9005EC2)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Default to false so LinoVPN's signature cyan/blue brand palette is active out of the box
    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
        val nightMode = when (mode) {
            "1" -> AppCompatDelegate.MODE_NIGHT_NO
            "2" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun toggleLightDark(currentlyDark: Boolean) {
        setThemeMode(if (currentlyDark) "1" else "2")
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
