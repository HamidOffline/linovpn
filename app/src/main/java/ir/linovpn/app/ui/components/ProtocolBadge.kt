package ir.linovpn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.linovpn.app.domain.model.ServerProtocol

@Composable
fun ProtocolBadge(
    protocol: ServerProtocol,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (protocol) {
        ServerProtocol.VLESS -> Triple(Color(0xFF0055AA), Color(0xFFE0F2FE), "VLESS")
        ServerProtocol.VMESS -> Triple(Color(0xFF007799), Color(0xFFE0F7FA), "VMESS")
        ServerProtocol.TROJAN -> Triple(Color(0xFF5B21B6), Color(0xFFEDE9FE), "TROJAN")
        ServerProtocol.SHADOWSOCKS -> Triple(Color(0xFF1E3A8A), Color(0xFFDBEAFE), "SS")
        ServerProtocol.HYSTERIA2 -> Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "HY2")
        ServerProtocol.TUIC -> Triple(Color(0xFF065F46), Color(0xFFD1FAE5), "TUIC")
        ServerProtocol.WIREGUARD -> Triple(Color(0xFF831843), Color(0xFFFCE7F3), "WG")
        ServerProtocol.SSH -> Triple(Color(0xFF374151), Color(0xFFF3F4F6), "SSH")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
