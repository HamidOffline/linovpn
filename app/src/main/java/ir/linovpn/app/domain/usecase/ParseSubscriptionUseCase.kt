package ir.linovpn.app.domain.usecase

import android.net.Uri
import android.util.Base64
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.ServerProtocol
import ir.linovpn.app.domain.model.VpnCore
import org.json.JSONObject
import java.net.URLDecoder
import javax.inject.Inject

class ParseSubscriptionUseCase @Inject constructor() {

    fun parseSubscriptionContent(content: String, profileId: String? = null): List<Server> {
        val trimmed = content.trim()
        val servers = mutableListOf<Server>()

        // 1. Try Base64 decoding
        val decoded = tryDecodeBase64(trimmed) ?: trimmed

        // 2. Line by line parsing
        val lines = decoded.lines()
        for (line in lines) {
            val cleanLine = line.trim()
            if (cleanLine.isBlank() || cleanLine.startsWith("#")) continue
            val server = parseSingleLink(cleanLine, profileId)
            if (server != null) {
                servers.add(server)
            }
        }

        // 3. Fallback: Clash YAML parsing if lines didn't yield servers
        if (servers.isEmpty() && (content.contains("proxies:") || content.contains("proxy-groups:"))) {
            servers.addAll(parseClashYaml(content, profileId))
        }

        return servers
    }

    fun parseSingleLink(link: String, profileId: String? = null): Server? {
        val trimmed = link.trim()
        return try {
            when {
                trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed, profileId)
                trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed, profileId)
                trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed, profileId)
                trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed, profileId)
                trimmed.startsWith("hy2://", ignoreCase = true) || trimmed.startsWith("hysteria2://", ignoreCase = true) -> parseHysteria2(trimmed, profileId)
                trimmed.startsWith("tuic://", ignoreCase = true) -> parseTuic(trimmed, profileId)
                trimmed.startsWith("wg://", ignoreCase = true) || trimmed.startsWith("wireguard://", ignoreCase = true) -> parseWireguard(trimmed, profileId)
                else -> null
            }
        } catch (e: Throwable) {
            null
        }
    }

    private fun parseVless(link: String, profileId: String?): Server {
        val uri = Uri.parse(link)
        val userInfo = uri.userInfo ?: ""
        val host = uri.host ?: ""
        val port = if (uri.port > 0) uri.port else 443
        val fragment = uri.fragment?.let {
            runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
        }?.takeIf { it.isNotBlank() } ?: "VLESS - $host"

        val flow = uri.getQueryParameter("flow")
        val security = uri.getQueryParameter("security") ?: "tls"
        val type = uri.getQueryParameter("type") ?: "tcp"
        val pbk = uri.getQueryParameter("pbk")
        val sid = uri.getQueryParameter("sid")
        val sni = uri.getQueryParameter("sni")
        val hostHeader = uri.getQueryParameter("host") ?: sni
        val fp = uri.getQueryParameter("fp") ?: "chrome"
        val spx = uri.getQueryParameter("spx")
        val path = uri.getQueryParameter("path")
        val serviceName = uri.getQueryParameter("serviceName")

        return Server(
            name = fragment,
            host = host,
            port = port,
            protocol = ServerProtocol.VLESS,
            preferredCore = VpnCore.XRAY,
            uuid = userInfo,
            flow = flow,
            network = type,
            security = security,
            serverName = sni,
            publicKey = pbk,
            shortId = sid,
            fingerprint = fp,
            spiderX = spx,
            path = path,
            hostHeader = hostHeader,
            serviceName = serviceName,
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseVmess(link: String, profileId: String?): Server? {
        val base64Data = link.substringAfter("vmess://")
        val jsonStr = tryDecodeBase64(base64Data) ?: return null
        val json = JSONObject(jsonStr)

        val name = json.optString("ps", "VMess")
        val host = json.optString("add")
        val port = json.optInt("port", 443)
        val id = json.optString("id")
        val aid = json.optInt("aid", 0)
        val net = json.optString("net", "tcp")
        val type = json.optString("type", "none")
        val hostHeader = json.optString("host")
        val path = json.optString("path")
        val tls = json.optString("tls")
        val sni = json.optString("sni")

        return Server(
            name = name,
            host = host,
            port = port,
            protocol = ServerProtocol.VMESS,
            preferredCore = VpnCore.XRAY,
            uuid = id,
            alterId = aid,
            network = net,
            security = if (tls.equals("tls", ignoreCase = true)) "tls" else "none",
            serverName = if (sni.isNotBlank()) sni else hostHeader,
            hostHeader = hostHeader,
            path = path,
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseTrojan(link: String, profileId: String?): Server {
        val uri = Uri.parse(link)
        val password = uri.userInfo ?: ""
        val host = uri.host ?: ""
        val port = if (uri.port > 0) uri.port else 443
        val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "Trojan - $host"
        val sni = uri.getQueryParameter("sni") ?: host
        val type = uri.getQueryParameter("type") ?: "tcp"

        return Server(
            name = name,
            host = host,
            port = port,
            protocol = ServerProtocol.TROJAN,
            preferredCore = VpnCore.XRAY,
            password = password,
            network = type,
            security = "tls",
            serverName = sni,
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseShadowsocks(link: String, profileId: String?): Server? {
        val withoutScheme = link.substringAfter("ss://")
        val fragment = withoutScheme.substringAfter("#", "")
        val name = if (fragment.isNotBlank()) URLDecoder.decode(fragment, "UTF-8") else "Shadowsocks"
        val mainPart = withoutScheme.substringBefore("#")

        val decoded = tryDecodeBase64(mainPart)
        if (decoded != null && decoded.contains("@")) {
            val userPass = decoded.substringBefore("@")
            val hostPort = decoded.substringAfter("@")
            val method = userPass.substringBefore(":")
            val password = userPass.substringAfter(":")
            val host = hostPort.substringBefore(":")
            val port = hostPort.substringAfter(":").toIntOrNull() ?: 8388

            return Server(
                name = name,
                host = host,
                port = port,
                protocol = ServerProtocol.SHADOWSOCKS,
                preferredCore = VpnCore.XRAY,
                method = method,
                password = password,
                profileId = profileId,
                rawUri = link
            )
        } else if (mainPart.contains("@")) {
            val userPassB64 = mainPart.substringBefore("@")
            val hostPort = mainPart.substringAfter("@")
            val userPass = tryDecodeBase64(userPassB64) ?: userPassB64
            val method = userPass.substringBefore(":")
            val password = userPass.substringAfter(":")
            val host = hostPort.substringBefore(":")
            val port = hostPort.substringAfter(":").toIntOrNull() ?: 8388

            return Server(
                name = name,
                host = host,
                port = port,
                protocol = ServerProtocol.SHADOWSOCKS,
                preferredCore = VpnCore.XRAY,
                method = method,
                password = password,
                profileId = profileId,
                rawUri = link
            )
        }
        return null
    }

    private fun parseHysteria2(link: String, profileId: String?): Server {
        val uri = Uri.parse(link)
        val password = uri.userInfo ?: ""
        val host = uri.host ?: ""
        val port = if (uri.port > 0) uri.port else 443
        val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "Hysteria2 - $host"
        val sni = uri.getQueryParameter("sni")
        val obfs = uri.getQueryParameter("obfs")
        val obfsPass = uri.getQueryParameter("obfs-password")

        return Server(
            name = name,
            host = host,
            port = port,
            protocol = ServerProtocol.HYSTERIA2,
            preferredCore = VpnCore.SINGBOX,
            password = password,
            serverName = sni,
            obfsType = obfs,
            obfsPassword = obfsPass,
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseTuic(link: String, profileId: String?): Server {
        val uri = Uri.parse(link)
        val userInfo = uri.userInfo ?: ""
        val uuid = userInfo.substringBefore(":")
        val password = userInfo.substringAfter(":", "")
        val host = uri.host ?: ""
        val port = if (uri.port > 0) uri.port else 8443
        val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "TUIC - $host"
        val sni = uri.getQueryParameter("sni")
        val cc = uri.getQueryParameter("congestion_control") ?: "bbr"

        return Server(
            name = name,
            host = host,
            port = port,
            protocol = ServerProtocol.TUIC,
            preferredCore = VpnCore.SINGBOX,
            uuid = uuid,
            password = password,
            serverName = sni,
            congestion = cc,
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseWireguard(link: String, profileId: String?): Server {
        val uri = Uri.parse(link)
        val host = uri.host ?: ""
        val port = if (uri.port > 0) uri.port else 51820
        val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "WireGuard - $host"
        val privKey = uri.getQueryParameter("privateKey") ?: uri.userInfo
        val peerPub = uri.getQueryParameter("publicKey") ?: uri.getQueryParameter("peer")
        val ip = uri.getQueryParameter("ip")

        return Server(
            name = name,
            host = host,
            port = port,
            protocol = ServerProtocol.WIREGUARD,
            preferredCore = VpnCore.SINGBOX,
            privateKey = privKey,
            peerPublicKey = peerPub,
            localAddress = if (ip != null) listOf(ip) else emptyList(),
            profileId = profileId,
            rawUri = link
        )
    }

    private fun parseClashYaml(yaml: String, profileId: String?): List<Server> {
        val servers = mutableListOf<Server>()
        val lines = yaml.lines()
        var insideProxies = false
        var currentMap = mutableMapOf<String, String>()

        fun commitCurrent() {
            if (currentMap.isNotEmpty()) {
                val name = currentMap["name"] ?: "Proxy"
                val server = currentMap["server"] ?: ""
                val port = currentMap["port"]?.toIntOrNull() ?: 443
                val type = currentMap["type"]?.lowercase() ?: "vless"

                if (server.isNotBlank()) {
                    val protocol = when (type) {
                        "vless" -> ServerProtocol.VLESS
                        "vmess" -> ServerProtocol.VMESS
                        "trojan" -> ServerProtocol.TROJAN
                        "ss" -> ServerProtocol.SHADOWSOCKS
                        "hysteria2" -> ServerProtocol.HYSTERIA2
                        "tuic" -> ServerProtocol.TUIC
                        "wireguard" -> ServerProtocol.WIREGUARD
                        else -> ServerProtocol.VLESS
                    }
                    servers.add(
                        Server(
                            name = name,
                            host = server,
                            port = port,
                            protocol = protocol,
                            preferredCore = if (protocol == ServerProtocol.HYSTERIA2 || protocol == ServerProtocol.TUIC) VpnCore.SINGBOX else VpnCore.XRAY,
                            uuid = currentMap["uuid"],
                            password = currentMap["password"],
                            profileId = profileId
                        )
                    )
                }
                currentMap.clear()
            }
        }

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.startsWith("proxies:")) {
                insideProxies = true
                continue
            }
            if (insideProxies) {
                if (line.startsWith("proxy-groups:") || line.startsWith("rules:")) {
                    commitCurrent()
                    break
                }
                if (line.startsWith("- name:") || line.startsWith("- {name:")) {
                    commitCurrent()
                }
                val parts = line.removePrefix("-").trim().split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().replace("\"", "").replace("'", "")
                    val value = parts[1].trim().replace("\"", "").replace("'", "")
                    currentMap[key] = value
                }
            }
        }
        commitCurrent()
        return servers
    }

    private fun tryDecodeBase64(input: String): String? {
        val clean = input.replace("\n", "").replace("\r", "").trim()
        if (clean.isBlank()) return null

        // If it already looks like raw server links or Clash yaml, return as is
        if (clean.contains("://") || clean.contains("proxies:") || clean.contains("outbounds:")) {
            return clean
        }

        // Apply proper 4-character padding
        val padLen = (4 - (clean.length % 4)) % 4
        val padded = clean + "=".repeat(padLen)

        // 1. Try standard Base64 first (handles '/', '+', standard padding)
        try {
            val bytes = Base64.decode(padded, Base64.DEFAULT)
            val str = String(bytes, Charsets.UTF_8)
            if (isLikelyValidDecoded(str)) {
                return str
            }
        } catch (_: Throwable) {}

        // 2. Try URL_SAFE Base64 (handles '_', '-')
        try {
            val bytes = Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP)
            val str = String(bytes, Charsets.UTF_8)
            if (isLikelyValidDecoded(str)) {
                return str
            }
        } catch (_: Throwable) {}

        // 3. Last resort fallback
        return try {
            val bytes = Base64.decode(padded, Base64.DEFAULT)
            String(bytes, Charsets.UTF_8)
        } catch (_: Throwable) {
            null
        }
    }

    private fun isLikelyValidDecoded(str: String): Boolean {
        return str.contains("://") || str.contains("proxies:") || str.contains("proxy-groups:")
    }
}
