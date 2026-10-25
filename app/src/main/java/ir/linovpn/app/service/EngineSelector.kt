package ir.linovpn.app.service

import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.ServerProtocol
import ir.linovpn.app.domain.model.VpnCore
import ir.linovpn.clash.ClashConfigBuilder
import ir.linovpn.clash.ClashEngine
import ir.linovpn.clash.ClashProxy
import ir.linovpn.common.TunConfig
import ir.linovpn.common.VpnEngine
import ir.linovpn.singbox.SingBoxConfigBuilder
import ir.linovpn.singbox.SingBoxEngine
import ir.linovpn.singbox.SingBoxServerConfig
import ir.linovpn.v2ray.V2RayEngine
import ir.linovpn.xray.XrayConfigBuilder
import ir.linovpn.xray.XrayEngine
import ir.linovpn.xray.XrayServerConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EngineSelector @Inject constructor(
    private val xrayEngine: XrayEngine,
    private val singBoxEngine: SingBoxEngine,
    private val clashEngine: ClashEngine,
    private val v2RayEngine: V2RayEngine
) {

    fun selectEngine(server: Server, userPreferredCore: VpnCore): VpnEngine {
        val selected = when (userPreferredCore) {
            VpnCore.XRAY -> xrayEngine
            VpnCore.SINGBOX -> if (singBoxEngine.isLibraryLoaded()) singBoxEngine else xrayEngine
            VpnCore.CLASH -> if (clashEngine.isLibraryLoaded()) clashEngine else xrayEngine
            VpnCore.V2RAY -> if (v2RayEngine.isLibraryLoaded()) v2RayEngine else xrayEngine
        }

        if (server.protocol == ServerProtocol.HYSTERIA2 ||
            server.protocol == ServerProtocol.TUIC ||
            server.protocol == ServerProtocol.WIREGUARD ||
            server.protocol == ServerProtocol.SSH
        ) {
            return if (singBoxEngine.isLibraryLoaded()) singBoxEngine else selected
        }

        return selected
    }

    fun buildConfig(server: Server, engine: VpnEngine, tunConfig: TunConfig?, bypassIran: Boolean = true): String {
        return when (engine) {
            is SingBoxEngine -> {
                val cfg = SingBoxServerConfig(
                    type = server.protocol.name.lowercase(),
                    server = server.host,
                    serverPort = server.port,
                    uuid = server.uuid,
                    password = server.password,
                    flow = server.flow,
                    network = server.network,
                    serverName = server.serverName,
                    realityEnabled = server.security.equals("reality", ignoreCase = true),
                    publicKey = server.publicKey,
                    shortId = server.shortId,
                    upMbps = server.upMbps,
                    downMbps = server.downMbps,
                    obfsType = server.obfsType,
                    obfsPassword = server.obfsPassword,
                    congestionControl = server.congestion,
                    privateKey = server.privateKey,
                    peerPublicKey = server.peerPublicKey,
                    localAddress = server.localAddress,
                    username = server.username
                )
                SingBoxConfigBuilder.buildConfig(cfg, tunFd = tunConfig?.fd ?: -1, bypassIran = bypassIran)
            }
            is ClashEngine -> {
                val proxy = ClashProxy(
                    name = server.name,
                    type = when (server.protocol) {
                        ServerProtocol.VLESS -> "vless"
                        ServerProtocol.VMESS -> "vmess"
                        ServerProtocol.TROJAN -> "trojan"
                        ServerProtocol.SHADOWSOCKS -> "ss"
                        ServerProtocol.HYSTERIA2 -> "hysteria2"
                        ServerProtocol.TUIC -> "tuic"
                        ServerProtocol.WIREGUARD -> "wireguard"
                        else -> "vless"
                    },
                    server = server.host,
                    port = server.port,
                    uuid = server.uuid,
                    password = server.password,
                    tls = server.security.equals("tls", ignoreCase = true) || server.security.equals("reality", ignoreCase = true),
                    servername = server.serverName,
                    network = server.network
                )
                ClashConfigBuilder.buildYaml(listOf(proxy))
            }
            is XrayEngine, is V2RayEngine -> {
                val cfg = XrayServerConfig(
                    address = server.host,
                    port = server.port,
                    protocol = server.protocol.name,
                    uuid = server.uuid,
                    alterId = server.alterId,
                    flow = server.flow,
                    streamNetwork = server.network,
                    security = server.security,
                    serverName = server.serverName,
                    publicKey = server.publicKey,
                    shortId = server.shortId,
                    spiderX = server.spiderX,
                    fingerprint = server.fingerprint,
                    path = server.path,
                    host = server.hostHeader ?: server.serverName,
                    serviceName = server.serviceName,
                    method = server.method,
                    password = server.password
                )
                XrayConfigBuilder.buildConfig(
                    server = cfg,
                    tunFd = tunConfig?.fd ?: -1,
                    bypassIran = bypassIran
                )
            }
            else -> ""
        }
    }
}
