package ir.linovpn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.ui.theme.LinoConnectedGreen
import ir.linovpn.app.ui.theme.LinoConnectingYellow
import ir.linovpn.app.ui.theme.LinoErrorRed
import ir.linovpn.app.ui.theme.LinoPrimaryCyan
import ir.linovpn.app.ui.theme.LinoSurface
import ir.linovpn.app.ui.theme.LinoSurfaceBorder
import ir.linovpn.app.ui.theme.LinoSurfaceVariant
import ir.linovpn.app.ui.theme.LinoTextPrimary
import ir.linovpn.app.ui.theme.LinoTextSecondary

@Composable
fun ServerCard(
    server: Server,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isPinging: Boolean = false
) {
    val borderColor = if (isSelected) LinoPrimaryCyan else LinoSurfaceBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) LinoSurfaceVariant else LinoSurface)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = LinoPrimaryCyan,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF384D70), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = server.name,
                    color = LinoTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                ProtocolBadge(protocol = server.protocol)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${server.host}:${server.port}",
                color = LinoTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPinging) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = LinoPrimaryCyan
                )
            } else {
                LatencyIndicator(latency = server.latency, onPing = onPing)
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (server.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (server.isFavorite) Color(0xFFFF4081) else Color(0xFF6B82A6),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun LatencyIndicator(
    latency: Long,
    onPing: () -> Unit
) {
    val (color, text) = when {
        latency == -1L -> Pair(Color(0xFF6B82A6), "تست")
        latency == -2L -> Pair(LinoErrorRed, "خطا")
        latency < 150 -> Pair(LinoConnectedGreen, "${latency}ms")
        latency < 350 -> Pair(LinoConnectingYellow, "${latency}ms")
        else -> Pair(LinoErrorRed, "${latency}ms")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable { onPing() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
