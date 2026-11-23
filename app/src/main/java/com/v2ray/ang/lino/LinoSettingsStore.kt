package com.v2ray.ang.lino

import com.tencent.mmkv.MMKV

data class UnbarSettings(
    val connectionMode: String = "vpn",            // "vpn" or "socks"
    val discoveryMode: String = "builtin",         // "builtin" (2307 IR + Discovery) or "custom"
    val customResolvers: String = "",              // comma-separated IP:53
    val tcpMode: Boolean = false,                  // UDP (false) vs TCP-53 (true)
    val mtu: Int = 0,                              // 0=Auto, 1280, 1400, 1500
    val maxAnswer: Int = 0,                        // 0=Auto, 512, 768, 1024, 1232
    val socksPort: Int = 10891,
    val socksUsername: String = "",
    val socksPassword: String = "",
    val uploadLimitKbps: Int = 0,                  // 0=unlimited
    val downloadLimitKbps: Int = 0,                // 0=unlimited
    val geoBypassIr: Boolean = true,
    val customBypassDomains: String = "",
    val showSpeedInNotification: Boolean = true,
    val diagnosticLogs: Boolean = true
)

data class SlipNetSettings(
    val defaultTunnelType: String = "dnstt",       // dnstt, noizdns, vaydns, soar, slipstream, ssh, naive, doh, tor
    val dnsTransport: String = "udp",              // udp, tcp, dot, doh
    val dohUrl: String = "https://1.1.1.1/dns-query",
    val resolverMode: String = "fanout",           // fanout, round_robin, fastest
    val rrSpreadCount: Int = 3,
    val customResolvers: String = "",              // comma-separated resolvers (populated by DNS Scanner)
    val authoritativeMode: Boolean = false,
    val noizStealthMode: Boolean = true,
    val maxPayload: Int = 0,                       // 0=Auto
    // VayDNS options
    val vayRecordType: String = "txt",             // txt, a, aaaa, mx, cname
    val vayMaxQnameLen: Int = 101,
    val vayRps: Double = 0.0,
    val vayIdleTimeout: Int = 0,
    val vayKeepalive: Int = 0,
    val vayUdpTimeout: Int = 0,
    val vayMaxLabels: Int = 0,
    val vayClientIdSize: Int = 0,
    val vayDnsttCompat: Boolean = false,
    // Slipstream QUIC options
    val slipstreamCongestion: String = "bbr",      // bbr, dcubic
    val slipstreamKeepAliveMs: Int = 5000,
    val slipstreamGso: Boolean = false,
    // SSH chaining / transport options
    val sshOverTls: Boolean = false,
    val sshSni: String = "",
    val sshOverWs: Boolean = false,
    val sshWsPath: String = "/",
    val sshHttpConnectProxy: String = "",
    val sshPayload: String = "",
    // Tor options
    val torBridgeMode: String = "snowflake",       // direct, snowflake, obfs4, meek, custom
    val torCustomBridges: String = "",
    // Local port & Scanner options
    val listenPort: Int = 10892,
    val scannerThreads: Int = 32,
    val scannerTimeoutMs: Int = 1500,
    val scannerTestDomain: String = "google.com",
    val scannerSource: String = "unbar_ir"         // unbar_ir, ir_lite, ir_full, custom
)

object LinoSettingsStore {
    private val unbarKv: MMKV by lazy { MMKV.mmkvWithID("lino_unbar_prefs", MMKV.MULTI_PROCESS_MODE) }
    private val slipKv: MMKV by lazy { MMKV.mmkvWithID("lino_slipnet_prefs", MMKV.MULTI_PROCESS_MODE) }
    private val appKv: MMKV by lazy { MMKV.mmkvWithID("lino_app_prefs", MMKV.MULTI_PROCESS_MODE) }

    // -------- App / MojAzad Smart Behavior --------
    var onboardingCompleted: Boolean
        get() = appKv.decodeBool("onboarding_done", false)
        set(v) { appKv.encode("onboarding_done", v) }

    var autoConnectAfterImport: Boolean
        get() = appKv.decodeBool("auto_connect_import", true)
        set(v) { appKv.encode("auto_connect_import", v) }

    var defaultDoubleColumnApplied: Boolean
        get() = appKv.decodeBool("double_col_init_v3", false)
        set(v) { appKv.encode("double_col_init_v3", v) }

    // -------- Unbar Settings (Dedicated & Isolated) --------
    fun loadUnbar(): UnbarSettings = UnbarSettings(
        connectionMode = unbarKv.decodeString("conn_mode", "vpn") ?: "vpn",
        discoveryMode = unbarKv.decodeString("disc_mode", "builtin") ?: "builtin",
        customResolvers = unbarKv.decodeString("custom_resolvers", "") ?: "",
        tcpMode = unbarKv.decodeBool("tcp_mode", false),
        mtu = unbarKv.decodeInt("mtu", 0),
        maxAnswer = unbarKv.decodeInt("max_answer", 0),
        socksPort = unbarKv.decodeInt("socks_port", 10891),
        socksUsername = unbarKv.decodeString("socks_user", "") ?: "",
        socksPassword = unbarKv.decodeString("socks_pass", "") ?: "",
        uploadLimitKbps = unbarKv.decodeInt("up_limit_kbps", 0),
        downloadLimitKbps = unbarKv.decodeInt("down_limit_kbps", 0),
        geoBypassIr = unbarKv.decodeBool("geo_bypass_ir", true),
        customBypassDomains = unbarKv.decodeString("bypass_domains", "") ?: "",
        showSpeedInNotification = unbarKv.decodeBool("show_speed_notif", true),
        diagnosticLogs = unbarKv.decodeBool("diag_logs", true)
    )

    fun saveUnbar(s: UnbarSettings) {
        unbarKv.encode("conn_mode", s.connectionMode)
        unbarKv.encode("disc_mode", s.discoveryMode)
        unbarKv.encode("custom_resolvers", s.customResolvers.trim())
        unbarKv.encode("tcp_mode", s.tcpMode)
        unbarKv.encode("mtu", s.mtu)
        unbarKv.encode("max_answer", s.maxAnswer)
        unbarKv.encode("socks_port", s.socksPort.coerceIn(1024, 65000))
        unbarKv.encode("socks_user", s.socksUsername)
        unbarKv.encode("socks_pass", s.socksPassword)
        unbarKv.encode("up_limit_kbps", s.uploadLimitKbps.coerceAtLeast(0))
        unbarKv.encode("down_limit_kbps", s.downloadLimitKbps.coerceAtLeast(0))
        unbarKv.encode("geo_bypass_ir", s.geoBypassIr)
        unbarKv.encode("bypass_domains", s.customBypassDomains.trim())
        unbarKv.encode("show_speed_notif", s.showSpeedInNotification)
        unbarKv.encode("diag_logs", s.diagnosticLogs)
    }

    fun applyScannedResolversToUnbar(resolversCsv: String) {
        val cur = loadUnbar()
        saveUnbar(cur.copy(discoveryMode = "custom", customResolvers = resolversCsv))
    }

    // -------- SlipNet Settings (Dedicated & Isolated) --------
    fun loadSlipNet(): SlipNetSettings = SlipNetSettings(
        defaultTunnelType = slipKv.decodeString("tunnel_type", "dnstt") ?: "dnstt",
        dnsTransport = slipKv.decodeString("dns_transport", "udp") ?: "udp",
        dohUrl = slipKv.decodeString("doh_url", "https://1.1.1.1/dns-query") ?: "https://1.1.1.1/dns-query",
        resolverMode = slipKv.decodeString("res_mode", "fanout") ?: "fanout",
        rrSpreadCount = slipKv.decodeInt("rr_spread", 3),
        customResolvers = slipKv.decodeString("custom_resolvers", "") ?: "",
        authoritativeMode = slipKv.decodeBool("auth_mode", false),
        noizStealthMode = slipKv.decodeBool("noiz_stealth", true),
        maxPayload = slipKv.decodeInt("max_payload", 0),
        vayRecordType = slipKv.decodeString("vay_rec", "txt") ?: "txt",
        vayMaxQnameLen = slipKv.decodeInt("vay_qname", 101),
        vayRps = slipKv.decodeDouble("vay_rps", 0.0),
        vayIdleTimeout = slipKv.decodeInt("vay_idle", 0),
        vayKeepalive = slipKv.decodeInt("vay_keepalive", 0),
        vayUdpTimeout = slipKv.decodeInt("vay_udp_to", 0),
        vayMaxLabels = slipKv.decodeInt("vay_max_labels", 0),
        vayClientIdSize = slipKv.decodeInt("vay_cid_size", 0),
        vayDnsttCompat = slipKv.decodeBool("vay_dnstt_compat", false),
        slipstreamCongestion = slipKv.decodeString("ss_cc", "bbr") ?: "bbr",
        slipstreamKeepAliveMs = slipKv.decodeInt("ss_ka_ms", 5000),
        slipstreamGso = slipKv.decodeBool("ss_gso", false),
        sshOverTls = slipKv.decodeBool("ssh_tls", false),
        sshSni = slipKv.decodeString("ssh_sni", "") ?: "",
        sshOverWs = slipKv.decodeBool("ssh_ws", false),
        sshWsPath = slipKv.decodeString("ssh_ws_path", "/") ?: "/",
        sshHttpConnectProxy = slipKv.decodeString("ssh_http_proxy", "") ?: "",
        sshPayload = slipKv.decodeString("ssh_payload", "") ?: "",
        torBridgeMode = slipKv.decodeString("tor_bridge_mode", "snowflake") ?: "snowflake",
        torCustomBridges = slipKv.decodeString("tor_custom_bridges", "") ?: "",
        listenPort = slipKv.decodeInt("listen_port", 10892),
        scannerThreads = slipKv.decodeInt("scan_threads", 32),
        scannerTimeoutMs = slipKv.decodeInt("scan_timeout", 1500),
        scannerTestDomain = slipKv.decodeString("scan_domain", "google.com") ?: "google.com",
        scannerSource = slipKv.decodeString("scan_source", "unbar_ir") ?: "unbar_ir"
    )

    fun saveSlipNet(s: SlipNetSettings) {
        slipKv.encode("tunnel_type", s.defaultTunnelType)
        slipKv.encode("dns_transport", s.dnsTransport)
        slipKv.encode("doh_url", s.dohUrl.trim())
        slipKv.encode("res_mode", s.resolverMode)
        slipKv.encode("rr_spread", s.rrSpreadCount.coerceIn(1, 16))
        slipKv.encode("custom_resolvers", s.customResolvers.trim())
        slipKv.encode("auth_mode", s.authoritativeMode)
        slipKv.encode("noiz_stealth", s.noizStealthMode)
        slipKv.encode("max_payload", s.maxPayload.coerceAtLeast(0))
        slipKv.encode("vay_rec", s.vayRecordType)
        slipKv.encode("vay_qname", s.vayMaxQnameLen.coerceIn(40, 250))
        slipKv.encode("vay_rps", s.vayRps.coerceAtLeast(0.0))
        slipKv.encode("vay_idle", s.vayIdleTimeout.coerceAtLeast(0))
        slipKv.encode("vay_keepalive", s.vayKeepalive.coerceAtLeast(0))
        slipKv.encode("vay_udp_to", s.vayUdpTimeout.coerceAtLeast(0))
        slipKv.encode("vay_max_labels", s.vayMaxLabels.coerceAtLeast(0))
        slipKv.encode("vay_cid_size", s.vayClientIdSize.coerceAtLeast(0))
        slipKv.encode("vay_dnstt_compat", s.vayDnsttCompat)
        slipKv.encode("ss_cc", s.slipstreamCongestion)
        slipKv.encode("ss_ka_ms", s.slipstreamKeepAliveMs.coerceIn(1000, 60000))
        slipKv.encode("ss_gso", s.slipstreamGso)
        slipKv.encode("ssh_tls", s.sshOverTls)
        slipKv.encode("ssh_sni", s.sshSni.trim())
        slipKv.encode("ssh_ws", s.sshOverWs)
        slipKv.encode("ssh_ws_path", s.sshWsPath.trim().ifEmpty { "/" })
        slipKv.encode("ssh_http_proxy", s.sshHttpConnectProxy.trim())
        slipKv.encode("ssh_payload", s.sshPayload)
        slipKv.encode("tor_bridge_mode", s.torBridgeMode)
        slipKv.encode("tor_custom_bridges", s.torCustomBridges.trim())
        slipKv.encode("listen_port", s.listenPort.coerceIn(1024, 65000))
        slipKv.encode("scan_threads", s.scannerThreads.coerceIn(4, 128))
        slipKv.encode("scan_timeout", s.scannerTimeoutMs.coerceIn(300, 10000))
        slipKv.encode("scan_domain", s.scannerTestDomain.trim().ifEmpty { "google.com" })
        slipKv.encode("scan_source", s.scannerSource)
    }

    fun applyScannedResolversToSlipNet(resolversCsv: String) {
        val cur = loadSlipNet()
        saveSlipNet(cur.copy(customResolvers = resolversCsv))
    }
}
