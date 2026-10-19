package ir.linovpn.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.linovpn.app.R
import ir.linovpn.app.ui.theme.LinoConnectedGreen
import ir.linovpn.app.ui.theme.LinoConnectingYellow
import ir.linovpn.app.ui.theme.LinoPrimaryCyan

@Composable
fun LinoLogoView(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isConnected: Boolean = false,
    isConnecting: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnecting) 1.15f else if (isConnected) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 800 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowColor = when {
        isConnected -> LinoConnectedGreen
        isConnecting -> LinoConnectingYellow
        else -> LinoPrimaryCyan
    }

    Box(
        modifier = modifier.size(size * 1.3f),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected || isConnecting) {
            Box(
                modifier = Modifier
                    .size(size * pulseScale)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = if (isConnecting) 0.25f else 0.15f))
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, glowColor.copy(alpha = 0.8f), CircleShape)
                .background(Color(0xFF060A14)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.linovpn_logo),
                contentDescription = "LinoVPN Logo",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
