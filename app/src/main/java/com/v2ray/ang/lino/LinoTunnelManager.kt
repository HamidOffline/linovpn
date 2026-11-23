package com.v2ray.ang.lino

import android.content.Context
import android.net.VpnService
import app.slipnet.tunnel.ResolverConfig
import app.slipnet.tunnel.SlipstreamBridge
import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Unified native process & JNI orchestrator for BOTH Unbar (Soar) and SlipNet FULL
 * (DNSTT, NoizDNS, VayDNS, Soar, Slipstream QUIC, NaiveProxy, Tor + Snowflake/obfs4/Meek, DoH).
 *
 * Each engine exposes a local loopback SOCKS5 port (127.0.0.1:port) that Xray's proxy outbound
 * forwards through — giving us full Xray stats, per-app proxy, routing, and live speed bar for free!
 */
object LinoTunnelManager {

    private const val TAG = "LinoTunnelManager"

    @Volatile
    private var activeProcess: Process? = null

    @Volatile
    private var auxProcess: Process? = null

    @Volatile
    private var slipstreamActive: Boolean = false

    @Volatile
    private var activePort: Int = 0

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun appendLog(line: String) {
        LogUtil.i(AppConfig.TAG, "[$TAG] $line")
        val cur = _logs.value
        _logs.value = (cur + line).takeLast(200)
    }

    fun getTargetPortFor(profile: ProfileItem): Int {
        return when (profile.configType) {
            EConfigType.UNBAR -> LinoSettingsStore.loadUnbar().socksPort
            EConfigType.SLIPNET -> LinoSettingsStore.loadSlipNet().listenPort
            else -> 10891
        }
    }

    @Synchronized
    fun startForProfile(context: Context, profile: ProfileItem, vpnService: VpnService? = null): Boolean {
        stop()
        return when (profile.configType) {
            EConfigType.UNBAR -> startUnbar(context, profile)
            EConfigType.SLIPNET -> startSlipNet(context, profile, vpnService)
            else -> true
        }
    }

    private fun startUnbar(context: Context, profile: ProfileItem): Boolean {
        val settings = LinoSettingsStore.loadUnbar()
        val bin = File(context.applicationInfo.nativeLibraryDir, "libunbar.so")
        if (!bin.exists()) {
            appendLog("ERROR: libunbar.so missing")
            return false
        }
        val zone = profile.server.orEmpty().trim()
        val pubkey = profile.publicKey.orEmpty().trim()
        if (zone.isEmpty() || pubkey.isEmpty()) {
            appendLog("ERROR: Unbar zone or pubkey is empty")
            return false
        }

        val port = settings.socksPort
        val useDiscovery = settings.discoveryMode != "custom"
        val resolvers = if (!useDiscovery && settings.customResolvers.isNotBlank()) {
            settings.customResolvers
        } else {
            profile.seed.orEmpty()
        }

        val args = mutableListOf(
            bin.absolutePath,
            "-zone", zone,
            "-pubkey", pubkey,
            "-port", port.toString(),
            "-discovery=$useDiscovery",
            "-country", profile.host?.ifBlank { "IR" } ?: "IR",
            "-geobypass=${settings.geoBypassIr}"
        )
        if (resolvers.isNotBlank()) {
            args.add("-resolvers")
            args.add(resolvers)
        }

        appendLog("Starting Unbar (Soar): zone=$zone port=$port discovery=$useDiscovery")
        val proc = launchProcess(args, context.filesDir, "unbar") ?: return false
        activeProcess = proc
        activePort = port
        return waitForPort(port, 12_000L, proc)
    }

    private fun startSlipNet(context: Context, profile: ProfileItem, vpnService: VpnService?): Boolean {
        val settings = LinoSettingsStore.loadSlipNet()
        val meta = try { JSONObject(profile.xhttpExtra.orEmpty()) } catch (_: Exception) { JSONObject() }
        val tunnelType = (profile.method ?: meta.optString("tunnelType", settings.defaultTunnelType)).lowercase()
        val domain = profile.server.orEmpty().ifBlank { meta.optString("domain", "") }.trim()
        val pubkey = profile.publicKey.orEmpty().ifBlank { meta.optString("pubkey", "") }.trim()
        val resolvers = settings.customResolvers.ifBlank {
            profile.seed.orEmpty().ifBlank { meta.optString("resolvers", "8.8.8.8:53,1.1.1.1:53") }
        }
        val port = settings.listenPort
        val listenAddr = "127.0.0.1:$port"
        val nativeDir = context.applicationInfo.nativeLibraryDir

        appendLog("Starting SlipNet [$tunnelType] domain=$domain port=$port")

        return when {
            tunnelType.startsWith("dnstt") || tunnelType.startsWith("noizdns") -> {
                val bin = File(nativeDir, "libslipnet_go.so")
                val isNoiz = tunnelType.startsWith("noizdns")
                val stealth = isNoiz && (meta.optBoolean("noizStealth", settings.noizStealthMode))
                val auth = meta.optBoolean("authMode", settings.authoritativeMode)
                val maxPayload = meta.optInt("maxPayload", settings.maxPayload)
                val args = mutableListOf(
                    bin.absolutePath, "dnstt",
                    "-dns", formatDnsAddresses(resolvers, settings.dnsTransport, settings.dohUrl),
                    "-domain", domain,
                    "-pubkey", pubkey,
                    "-listen", listenAddr,
                    "-auth=$auth",
                    "-noiz=$isNoiz",
                    "-stealth=$stealth",
                    "-max-payload", maxPayload.toString(),
                    "-resolver-mode", settings.resolverMode,
                    "-rr-spread", settings.rrSpreadCount.toString()
                )
                val proc = launchProcess(args, context.filesDir, "dnstt") ?: return false
                activeProcess = proc
                activePort = port
                waitForPort(port, 8_000L, proc)
            }

            tunnelType.startsWith("vaydns") -> {
                val bin = File(nativeDir, "libslipnet_go.so")
                val args = mutableListOf(
                    bin.absolutePath, "vaydns",
                    "-dns", formatDnsAddresses(resolvers, settings.dnsTransport, settings.dohUrl),
                    "-domain", domain,
                    "-pubkey", pubkey,
                    "-listen", listenAddr,
                    "-dnstt-compat=${settings.vayDnsttCompat}",
                    "-max-payload", settings.maxPayload.toString(),
                    "-record-type", settings.vayRecordType,
                    "-max-qname", settings.vayMaxQnameLen.toString(),
                    "-rps", settings.vayRps.toString(),
                    "-idle-timeout", settings.vayIdleTimeout.toString(),
                    "-keepalive", settings.vayKeepalive.toString(),
                    "-udp-timeout", settings.vayUdpTimeout.toString(),
                    "-max-labels", settings.vayMaxLabels.toString(),
                    "-client-id-size", settings.vayClientIdSize.toString(),
                    "-resolver-mode", settings.resolverMode,
                    "-rr-spread", settings.rrSpreadCount.toString()
                )
                val proc = launchProcess(args, context.filesDir, "vaydns") ?: return false
                activeProcess = proc
                activePort = port
                waitForPort(port, 8_000L, proc)
            }

            tunnelType.startsWith("soar") -> {
                val bin = File(nativeDir, "libslipnet_go.so")
                val args = listOf(
                    bin.absolutePath, "soar",
                    "-dns", resolvers,
                    "-zone", domain,
                    "-pubkey", pubkey,
                    "-listen", listenAddr
                )
                val proc = launchProcess(args, context.filesDir, "soar") ?: return false
                activeProcess = proc
                activePort = port
                waitForPort(port, 10_000L, proc)
            }

            tunnelType.startsWith("slipstream") -> {
                SlipstreamBridge.proxyOnlyMode = (vpnService == null)
                SlipstreamBridge.setVpnService(vpnService)
                val resConfigs = resolvers.split(",").mapNotNull { entry ->
                    val clean = entry.trim()
                    if (clean.isEmpty()) return@mapNotNull null
                    val hp = clean.split(":")
                    ResolverConfig(
                        host = hp[0],
                        port = hp.getOrNull(1)?.toIntOrNull() ?: 53,
                        authoritative = meta.optBoolean("authMode", settings.authoritativeMode)
                    )
                }.ifEmpty { listOf(ResolverConfig("8.8.8.8", 53, false)) }

                val res = SlipstreamBridge.startClient(
                    domain = domain,
                    resolvers = resConfigs,
                    congestionControl = meta.optString("cc", settings.slipstreamCongestion),
                    keepAliveInterval = meta.optInt("keepAlive", settings.slipstreamKeepAliveMs),
                    tcpListenPort = port,
                    tcpListenHost = "127.0.0.1",
                    gsoEnabled = meta.optBoolean("gso", settings.slipstreamGso)
                )
                slipstreamActive = res.isSuccess
                activePort = SlipstreamBridge.getClientPort()
                appendLog("Slipstream start result: ${res.isSuccess} port=$activePort")
                res.isSuccess
            }

            tunnelType.startsWith("naive") -> {
                val bin = File(nativeDir, "libnaive.so")
                val host = meta.optString("host", domain).ifBlank { domain }
                val nPort = meta.optInt("naivePort", 443)
                val nUser = meta.optString("naiveUser", "")
                val nPass = meta.optString("naivePass", "")
                val authPart = if (nUser.isNotBlank()) "$nUser:$nPass@" else ""
                val proxyUrl = "https://$authPart$host:$nPort"
                val args = listOf(
                    bin.absolutePath,
                    "--listen=socks://$listenAddr",
                    "--proxy=$proxyUrl"
                )
                val proc = launchProcess(args, context.filesDir, "naive") ?: return false
                activeProcess = proc
                activePort = port
                waitForPort(port, 8_000L, proc)
            }

            tunnelType == "tor" || tunnelType == "snowflake" -> {
                val torBin = File(nativeDir, "libtor.so")
                val obfsBin = File(nativeDir, "libobfs4proxy.so")
                val torDataDir = File(context.filesDir, "lino_tor_data").apply { mkdirs() }
                val torrcFile = File(torDataDir, "torrc")
                val bridgeMode = settings.torBridgeMode.lowercase()
                val customBridges = meta.optString("torBridges", settings.torCustomBridges).trim()

                val sb = StringBuilder()
                sb.appendLine("DataDirectory ${torDataDir.absolutePath}")
                sb.appendLine("SocksPort 127.0.0.1:$port")
                sb.appendLine("Log notice stdout")

                if (bridgeMode == "snowflake" || tunnelType == "snowflake") {
                    val sfPort = port + 10
                    val goBin = File(nativeDir, "libslipnet_go.so")
                    val sfArgs = listOf(
                        goBin.absolutePath, "snowflake",
                        "-listen", "127.0.0.1:$sfPort",
                        "-broker", "https://snowflake-broker.torproject.net/",
                        "-fronts", "www.cdn77.com,www.phpmyadmin.net,www.fastly.com",
                        "-stuns", "stun:stun.l.google.com:19302,stun:stun.antisip.com:3478"
                    )
                    auxProcess = launchProcess(sfArgs, context.filesDir, "snowflake")
                    if (auxProcess != null) {
                        waitForPort(sfPort, 5_000L, auxProcess!!)
                        sb.appendLine("Socks5Proxy 127.0.0.1:$sfPort")
                    }
                } else if ((bridgeMode == "obfs4" || bridgeMode == "meek" || bridgeMode == "custom") && obfsBin.exists() && customBridges.isNotBlank()) {
                    sb.appendLine("UseBridges 1")
                    sb.appendLine("ClientTransportPlugin obfs4,meek_lite,webtunnel exec ${obfsBin.absolutePath}")
                    customBridges.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { b ->
                        val line = if (b.startsWith("Bridge ", true)) b else "Bridge $b"
                        sb.appendLine(line)
                    }
                }
                torrcFile.writeText(sb.toString(), Charsets.UTF_8)
                val proc = launchProcess(listOf(torBin.absolutePath, "-f", torrcFile.absolutePath), torDataDir, "tor") ?: return false
                activeProcess = proc
                activePort = port
                waitForPort(port, 15_000L, proc)
            }

            else -> {
                // Fallback for DoH / generic SlipNet profiles: launch Unbar Soar if pubkey exists
                appendLog("Fallback SlipNet mode [$tunnelType]")
                if (pubkey.isNotBlank() && domain.isNotBlank()) {
                    val bin = File(nativeDir, "libslipnet_go.so")
                    val args = listOf(bin.absolutePath, "dnstt", "-dns", resolvers, "-domain", domain, "-pubkey", pubkey, "-listen", listenAddr)
                    val proc = launchProcess(args, context.filesDir, "slipnet") ?: return false
                    activeProcess = proc
                    activePort = port
                    waitForPort(port, 8_000L, proc)
                } else {
                    false
                }
            }
        }
    }

    private fun formatDnsAddresses(raw: String, transport: String, dohUrl: String): String {
        if (transport == "doh" && dohUrl.startsWith("https://")) return dohUrl
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",") { addr ->
            when {
                addr.startsWith("https://") || addr.startsWith("tls://") || addr.startsWith("tcp://") -> addr
                transport == "tcp" -> "tcp://${if (addr.contains(":")) addr else "$addr:53"}"
                transport == "dot" -> "tls://${if (addr.contains(":")) addr else "$addr:853"}"
                addr.contains(":") -> addr
                else -> "$addr:53"
            }
        }
    }

    private fun launchProcess(args: List<String>, workDir: File, tag: String): Process? {
        return try {
            val proc = ProcessBuilder(args)
                .directory(workDir)
                .redirectErrorStream(true)
                .start()
            Thread {
                try {
                    proc.inputStream.bufferedReader().forEachLine { line ->
                        appendLog("[$tag] $line")
                    }
                } catch (_: Exception) {
                }
            }.apply { isDaemon = true; start() }
            proc
        } catch (e: Exception) {
            appendLog("Failed to launch $tag: ${e.message}")
            null
        }
    }

    private fun waitForPort(port: Int, timeoutMs: Long, proc: Process): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (!proc.isAlive) {
                appendLog("Process exited early with code ${proc.exitValue()}")
                return false
            }
            try {
                Socket().use { s -> s.connect(InetSocketAddress("127.0.0.1", port), 250) }
                appendLog("Local SOCKS5 ready on 127.0.0.1:$port")
                return true
            } catch (_: Exception) {
                try { Thread.sleep(180) } catch (_: InterruptedException) { return false }
            }
        }
        appendLog("Timed out waiting for port $port")
        return false
    }

    @Synchronized
    fun stop() {
        if (slipstreamActive) {
            try {
                SlipstreamBridge.stopClient()
                SlipstreamBridge.setVpnService(null)
            } catch (_: Exception) {
            }
            slipstreamActive = false
        }
        try {
            activeProcess?.destroy()
        } catch (_: Exception) {
        }
        activeProcess = null
        try {
            auxProcess?.destroy()
        } catch (_: Exception) {
        }
        auxProcess = null
        activePort = 0
    }

    fun isRunning(): Boolean {
        return (activeProcess?.isAlive == true) || slipstreamActive
    }
}
