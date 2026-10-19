package ir.linovpn.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LinoPrimaryCyan = Color(0xFF00C8FF)
val LinoSecondaryBlue = Color(0xFF0088FF)
val LinoDeepBlue = Color(0xFF0A47C9)
val LinoDarkNavy = Color(0xFF060A14)
val LinoSurface = Color(0xFF0D1526)
val LinoSurfaceVariant = Color(0xFF132038)
val LinoSurfaceBorder = Color(0xFF1E304F)

val LinoConnectedGreen = Color(0xFF00E676)
val LinoConnectingYellow = Color(0xFFFFB300)
val LinoErrorRed = Color(0xFFFF5252)

val LinoTextPrimary = Color(0xFFE8F4FD)
val LinoTextSecondary = Color(0xFF8FA3BF)
val LinoTextMuted = Color(0xFF536A89)

// Aliases for convenience
val LinoCyan = LinoPrimaryCyan
val LinoCardBg = LinoSurface
val LinoGray = LinoTextSecondary

val LinoLogoGradient = Brush.linearGradient(
    colors = listOf(LinoPrimaryCyan, LinoSecondaryBlue, LinoDeepBlue)
)

val LinoCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF111D33), Color(0xFF0B1424))
)

val LinoButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C8FF), Color(0xFF0A47C9))
)

val LinoGlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x3300C8FF), Color(0x00060A14))
)
