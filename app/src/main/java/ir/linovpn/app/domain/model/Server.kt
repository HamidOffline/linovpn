package ir.linovpn.app.domain.model

import java.util.UUID

enum class ServerProtocol {
    VLESS,
    VMESS,
    TROJAN,
    SHADOWSOCKS,
    TUIC,
    HYSTERIA2,
    WIREGUARD,
    SSH;

    companion object {
        fun fromString(str: String): ServerProtocol {
            return when (str.lowercase()) {
                "vless" -> VLESS
                "vmess" -> VMESS
                "trojan" -> TROJAN
                "shadowsocks", "ss" -> SHADOWSOCKS
                "tuic" -> TUIC
                "hysteria2", "hy2" -> HYSTERIA2
                "wireguard", "wg" -> WIREGUARD
                "ssh" -> SSH
                else -> VLESS
            }
        }
    }
}

enum class VpnCore(val displayName: String) {
    XRAY("Xray-core"),
    SINGBOX("sing-box"),
    CLASH("Clash Meta"),
    V2RAY("v2ray-core")
}

data class Server(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int,
    val protocol: ServerProtocol,
    val preferredCore: VpnCore = VpnCore.XRAY,
    // VLESS / VMess / Trojan
    val uuid: String? = null,
    val alterId: Int = 0,
    val flow: String? = null,
    val network: String = "tcp", // tcp, ws, grpc, h2, quic
    val security: String = "tls", // none, tls, reality
    // Reality / TLS details
    val serverName: String? = null,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null,
    val fingerprint: String? = "chrome",
    // WS / gRPC
    val path: String? = null,
    val hostHeader: String? = null,
    val serviceName: String? = null,
    // Shadowsocks
    val method: String? = null,
    val password: String? = null,
    // Hysteria 2 / TUIC
    val obfsType: String? = null,
    val obfsPassword: String? = null,
    val congestion: String? = "bbr",
    val upMbps: Int = 100,
    val downMbps: Int = 100,
    // WireGuard
    val privateKey: String? = null,
    val peerPublicKey: String? = null,
    val localAddress: List<String> = emptyList(),
    // SSH
    val username: String? = null,
    // Status & Metadata
    val profileId: String? = null,
    val latency: Long = -1L,        // -1 = untracked, -2 = timeout
    val isSelected: Boolean = false,
    val isFavorite: Boolean = false,
    val rawUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
