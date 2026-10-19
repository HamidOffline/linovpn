package ir.linovpn.clash

data class ClashProxy(
    val name: String,
    val type: String, // ss, vmess, vless, trojan, hysteria2, tuic, wireguard
    val server: String,
    val port: Int,
    val uuid: String? = null,
    val password: String? = null,
    val cipher: String? = null,
    val tls: Boolean = true,
    val servername: String? = null,
    val realityOpts: Map<String, String>? = null,
    val network: String? = "tcp",
    val wsOpts: Map<String, String>? = null
)

object ClashConfigBuilder {

    fun buildYaml(
        proxies: List<ClashProxy>,
        mixedPort: Int = 7890,
        mode: String = "rule",
        logLevel: String = "warning"
    ): String {
        val sb = StringBuilder()

        sb.appendLine("port: 7890")
        sb.appendLine("socks-port: 7891")
        sb.appendLine("mixed-port: $mixedPort")
        sb.appendLine("allow-lan: false")
        sb.appendLine("mode: $mode")
        sb.appendLine("log-level: $logLevel")
        sb.appendLine("ipv6: false")
        sb.appendLine("external-controller: 127.0.0.1:9090")
        sb.appendLine()

        // DNS
        sb.appendLine("dns:")
        sb.appendLine("  enable: true")
        sb.appendLine("  listen: 127.0.0.1:1053")
        sb.appendLine("  ipv6: false")
        sb.appendLine("  enhanced-mode: fake-ip")
        sb.appendLine("  fake-ip-range: 198.18.0.1/16")
        sb.appendLine("  nameserver:")
        sb.appendLine("    - https://1.1.1.1/dns-query")
        sb.appendLine("    - https://8.8.8.8/dns-query")
        sb.appendLine()

        // Proxies
        sb.appendLine("proxies:")
        for (p in proxies) {
            sb.appendLine("  - name: \"${p.name}\"")
            sb.appendLine("    type: ${p.type}")
            sb.appendLine("    server: ${p.server}")
            sb.appendLine("    port: ${p.port}")
            if (!p.uuid.isNullOrBlank()) sb.appendLine("    uuid: ${p.uuid}")
            if (!p.password.isNullOrBlank()) sb.appendLine("    password: ${p.password}")
            if (!p.cipher.isNullOrBlank()) sb.appendLine("    cipher: ${p.cipher}")
            if (p.tls) sb.appendLine("    tls: true")
            if (!p.servername.isNullOrBlank()) sb.appendLine("    servername: ${p.servername}")
            if (p.realityOpts != null) {
                sb.appendLine("    reality-opts:")
                p.realityOpts.forEach { (k, v) ->
                    sb.appendLine("      $k: $v")
                }
            }
            if (p.network == "ws") {
                sb.appendLine("    network: ws")
                if (p.wsOpts != null) {
                    sb.appendLine("    ws-opts:")
                    p.wsOpts.forEach { (k, v) ->
                        sb.appendLine("      $k: $v")
                    }
                }
            }
        }
        sb.appendLine()

        // Proxy Groups
        sb.appendLine("proxy-groups:")
        sb.appendLine("  - name: PROXY")
        sb.appendLine("    type: select")
        sb.appendLine("    proxies:")
        for (p in proxies) {
            sb.appendLine("      - \"${p.name}\"")
        }
        sb.appendLine()

        // Rules
        sb.appendLine("rules:")
        sb.appendLine("  - GEOIP,IR,DIRECT")
        sb.appendLine("  - GEOSITE,category-ads-all,REJECT")
        sb.appendLine("  - MATCH,PROXY")

        return sb.toString()
    }
}
