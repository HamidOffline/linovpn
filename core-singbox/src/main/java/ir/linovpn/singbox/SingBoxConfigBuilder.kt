package ir.linovpn.singbox

import org.json.JSONArray
import org.json.JSONObject

data class SingBoxServerConfig(
    val type: String, // hysteria2, tuic, wireguard, ssh, vless, vmess, trojan, shadowsocks
    val server: String,
    val serverPort: Int,
    val tag: String = "proxy",
    val password: String? = null,
    val uuid: String? = null,
    val flow: String? = null,
    val network: String? = "tcp",
    val tlsEnabled: Boolean = true,
    val serverName: String? = null,
    val realityEnabled: Boolean = false,
    val publicKey: String? = null,
    val shortId: String? = null,
    // Hysteria2
    val upMbps: Int = 100,
    val downMbps: Int = 100,
    val obfsType: String? = null,
    val obfsPassword: String? = null,
    // TUIC
    val congestionControl: String? = "bbr",
    // WireGuard
    val privateKey: String? = null,
    val peerPublicKey: String? = null,
    val localAddress: List<String> = emptyList(),
    // SSH
    val username: String? = null
)

object SingBoxConfigBuilder {

    fun buildConfig(
        server: SingBoxServerConfig,
        mixedPort: Int = 2080,
        tunFd: Int = -1,
        bypassIran: Boolean = true
    ): String {
        val root = JSONObject()

        // 1. Log
        root.put("log", JSONObject().apply {
            put("level", "warn")
            put("timestamp", true)
        })

        // 2. DNS
        val dnsObj = JSONObject().apply {
            val servers = JSONArray().apply {
                put(JSONObject().apply {
                    put("tag", "remote-dns")
                    put("address", "https://1.1.1.1/dns-query")
                    put("detour", "proxy")
                })
                put(JSONObject().apply {
                    put("tag", "local-dns")
                    put("address", "local")
                    put("detour", "direct")
                })
            }
            put("servers", servers)
            val rules = JSONArray().apply {
                if (bypassIran) {
                    rulesForIranDns(this)
                }
            }
            put("rules", rules)
            put("final", "remote-dns")
        }
        root.put("dns", dnsObj)

        // 3. Inbounds
        val inbounds = JSONArray()
        val mixedInbound = JSONObject().apply {
            put("type", "mixed")
            put("tag", "mixed-in")
            put("listen", "127.0.0.1")
            put("listen_port", mixedPort)
            put("sniff", true)
        }
        inbounds.put(mixedInbound)

        if (tunFd > 0) {
            val tunInbound = JSONObject().apply {
                put("type", "tun")
                put("tag", "tun-in")
                put("file_descriptor", tunFd)
                put("auto_route", false)
                put("strict_route", false)
                put("endpoint_independent_nat", true)
                put("stack", "system")
            }
            inbounds.put(tunInbound)
        }
        root.put("inbounds", inbounds)

        // 4. Outbounds
        val outbounds = JSONArray()
        outbounds.put(buildProxyOutbound(server))

        // Direct outbound
        outbounds.put(JSONObject().apply {
            put("type", "direct")
            put("tag", "direct")
        })

        // Block outbound
        outbounds.put(JSONObject().apply {
            put("type", "block")
            put("tag", "block")
        })

        // DNS outbound
        outbounds.put(JSONObject().apply {
            put("type", "dns")
            put("tag", "dns-out")
        })

        root.put("outbounds", outbounds)

        // 5. Route
        val routeObj = JSONObject().apply {
            put("auto_detect_interface", true)
            put("final", "proxy")
            val rules = JSONArray()

            // Route DNS traffic
            rules.put(JSONObject().apply {
                put("protocol", "dns")
                put("outbound", "dns-out")
            })

            // Bypass private networks
            rules.put(JSONObject().apply {
                put("ip_is_private", true)
                put("outbound", "direct")
            })

            // Bypass Iran if enabled
            if (bypassIran) {
                rules.put(JSONObject().apply {
                    put("geoip", JSONArray().apply { put("ir") })
                    put("outbound", "direct")
                })
                rules.put(JSONObject().apply {
                    put("geosite", JSONArray().apply { put("ir") })
                    put("outbound", "direct")
                })
            }

            put("rules", rules)
        }
        root.put("route", routeObj)

        return root.toString(2)
    }

    private fun rulesForIranDns(rules: JSONArray) {
        rules.put(JSONObject().apply {
            put("geosite", JSONArray().apply { put("ir") })
            put("server", "local-dns")
        })
    }

    private fun buildProxyOutbound(server: SingBoxServerConfig): JSONObject {
        val out = JSONObject()
        val type = server.type.lowercase()
        out.put("tag", "proxy")

        when (type) {
            "hysteria2", "hy2" -> {
                out.put("type", "hysteria2")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("password", server.password ?: "")
                out.put("up_mbps", server.upMbps)
                out.put("down_mbps", server.downMbps)
                val tlsObj = JSONObject().apply {
                    put("enabled", true)
                    if (!server.serverName.isNullOrBlank()) {
                        put("server_name", server.serverName)
                    }
                    put("insecure", false)
                }
                out.put("tls", tlsObj)
                if (!server.obfsType.isNullOrBlank()) {
                    out.put("obfs", JSONObject().apply {
                        put("type", server.obfsType)
                        put("password", server.obfsPassword ?: "")
                    })
                }
            }
            "tuic" -> {
                out.put("type", "tuic")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("uuid", server.uuid ?: "")
                out.put("password", server.password ?: "")
                out.put("congestion_control", server.congestionControl ?: "bbr")
                out.put("tls", JSONObject().apply {
                    put("enabled", true)
                    if (!server.serverName.isNullOrBlank()) {
                        put("server_name", server.serverName)
                    }
                })
            }
            "wireguard", "wg" -> {
                out.put("type", "wireguard")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("private_key", server.privateKey ?: "")
                out.put("peer_public_key", server.peerPublicKey ?: "")
                val localArr = JSONArray()
                server.localAddress.forEach { localArr.put(it) }
                if (localArr.length() == 0) localArr.put("10.0.0.2/32")
                out.put("local_address", localArr)
            }
            "ssh" -> {
                out.put("type", "ssh")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("user", server.username ?: "root")
                out.put("password", server.password ?: "")
            }
            "vless" -> {
                out.put("type", "vless")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("uuid", server.uuid ?: "")
                if (!server.flow.isNullOrBlank()) {
                    out.put("flow", server.flow)
                }
                if (server.realityEnabled) {
                    out.put("tls", JSONObject().apply {
                        put("enabled", true)
                        put("server_name", server.serverName ?: server.server)
                        put("reality", JSONObject().apply {
                            put("enabled", true)
                            put("public_key", server.publicKey ?: "")
                            put("short_id", server.shortId ?: "")
                        })
                    })
                } else if (server.tlsEnabled) {
                    out.put("tls", JSONObject().apply {
                        put("enabled", true)
                        put("server_name", server.serverName ?: server.server)
                    })
                }
            }
            "trojan" -> {
                out.put("type", "trojan")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("password", server.password ?: "")
                out.put("tls", JSONObject().apply {
                    put("enabled", true)
                    put("server_name", server.serverName ?: server.server)
                })
            }
            else -> {
                out.put("type", "shadowsocks")
                out.put("server", server.server)
                out.put("server_port", server.serverPort)
                out.put("method", "2022-blake3-aes-128-gcm")
                out.put("password", server.password ?: "")
            }
        }

        return out
    }
}
