package ir.linovpn.xray

import org.json.JSONArray
import org.json.JSONObject

data class XrayServerConfig(
    val address: String,
    val port: Int,
    val protocol: String, // VLESS, VMESS, TROJAN, SHADOWSOCKS
    val uuid: String? = null,
    val alterId: Int = 0,
    val flow: String? = null,
    val streamNetwork: String = "tcp", // tcp, ws, grpc, h2
    val security: String = "none", // none, tls, reality
    // Reality / TLS details
    val serverName: String? = null,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null,
    val fingerprint: String? = "chrome",
    // WS / gRPC
    val path: String? = null,
    val host: String? = null,
    val serviceName: String? = null,
    // Shadowsocks
    val method: String? = null,
    val password: String? = null
)

object XrayConfigBuilder {

    fun buildConfig(
        server: XrayServerConfig,
        socksPort: Int = 10808,
        httpPort: Int = 10809,
        tunFd: Int = -1,
        dnsServer: String = "1.1.1.1",
        bypassIran: Boolean = true
    ): String {
        val root = JSONObject()

        // 0. Stats
        root.put("stats", JSONObject())

        // 1. Log configuration
        val logObj = JSONObject()
        logObj.put("loglevel", "warning")
        root.put("log", logObj)

        // 2. Policy
        val policyObj = JSONObject().apply {
            val levelsObj = JSONObject().apply {
                val level8 = JSONObject().apply {
                    put("handshake", 4)
                    put("connIdle", 300)
                    put("uplinkOnly", 1)
                    put("downlinkOnly", 1)
                }
                put("8", level8)
            }
            put("levels", levelsObj)
            val systemObj = JSONObject().apply {
                put("statsOutboundUplink", true)
                put("statsOutboundDownlink", true)
            }
            put("system", systemObj)
        }
        root.put("policy", policyObj)

        // 3. DNS
        val dnsObj = JSONObject()
        val serversArr = JSONArray()
        if (dnsServer.isNotBlank()) {
            serversArr.put(dnsServer)
        }
        serversArr.put("1.1.1.1")
        serversArr.put("8.8.8.8")
        serversArr.put("localhost")
        dnsObj.put("servers", serversArr)
        root.put("dns", dnsObj)

        // 4. Inbounds (TUN, Socks5 & HTTP)
        val inboundsArr = JSONArray()

        if (tunFd > 0) {
            val tunInbound = JSONObject().apply {
                put("tag", "tun")
                put("protocol", "tun")
                put("settings", JSONObject().apply {
                    put("name", "xray0")
                    put("MTU", 1500)
                    put("userLevel", 8)
                })
                put("sniffing", JSONObject().apply {
                    put("enabled", true)
                    put("destOverride", JSONArray().apply {
                        put("http")
                        put("tls")
                        put("quic")
                    })
                })
            }
            inboundsArr.put(tunInbound)
        }

        val socksInbound = JSONObject().apply {
            put("tag", "socks")
            put("port", socksPort)
            put("listen", "127.0.0.1")
            put("protocol", "socks")
            put("settings", JSONObject().apply {
                put("auth", "noauth")
                put("udp", true)
                put("userLevel", 8)
            })
            put("sniffing", JSONObject().apply {
                put("enabled", true)
                put("destOverride", JSONArray().apply {
                    put("http")
                    put("tls")
                    put("quic")
                })
            })
        }
        inboundsArr.put(socksInbound)

        val httpInbound = JSONObject().apply {
            put("tag", "http")
            put("port", httpPort)
            put("listen", "127.0.0.1")
            put("protocol", "http")
        }
        inboundsArr.put(httpInbound)
        root.put("inbounds", inboundsArr)

        // 4. Outbounds
        val outboundsArr = JSONArray()
        val proxyOutbound = JSONObject()
        proxyOutbound.put("tag", "proxy")

        when (server.protocol.uppercase()) {
            "VLESS" -> buildVlessOutbound(proxyOutbound, server)
            "VMESS" -> buildVmessOutbound(proxyOutbound, server)
            "TROJAN" -> buildTrojanOutbound(proxyOutbound, server)
            "SHADOWSOCKS" -> buildShadowsocksOutbound(proxyOutbound, server)
            else -> buildVlessOutbound(proxyOutbound, server)
        }

        // Stream Settings (TLS, Reality, WS, gRPC)
        proxyOutbound.put("streamSettings", buildStreamSettings(server))
        outboundsArr.put(proxyOutbound)

        // Direct outbound
        val directOutbound = JSONObject().apply {
            put("tag", "direct")
            put("protocol", "freedom")
            put("streamSettings", JSONObject().apply {
                put("sockopt", JSONObject().apply {
                    put("domainStrategy", "UseIP")
                })
            })
        }
        outboundsArr.put(directOutbound)

        // Block outbound
        val blockOutbound = JSONObject().apply {
            put("tag", "block")
            put("protocol", "blackhole")
            put("settings", JSONObject())
        }
        outboundsArr.put(blockOutbound)

        root.put("outbounds", outboundsArr)

        // 5. Routing
        val routingObj = JSONObject().apply {
            put("domainStrategy", "AsIs")
            val rulesArr = JSONArray()

            // Bypass Iran if enabled
            if (bypassIran) {
                rulesArr.put(JSONObject().apply {
                    put("type", "field")
                    put("outboundTag", "direct")
                    put("ip", JSONArray().apply { put("geoip:ir") })
                })
                rulesArr.put(JSONObject().apply {
                    put("type", "field")
                    put("outboundTag", "direct")
                    put("domain", JSONArray().apply {
                        put("regexp:.*\\.ir$")
                        put("domain:ir")
                    })
                })
            }

            // Private network direct
            rulesArr.put(JSONObject().apply {
                put("type", "field")
                put("outboundTag", "direct")
                put("ip", JSONArray().apply { put("geoip:private") })
            })

            // Default to proxy
            rulesArr.put(JSONObject().apply {
                put("type", "field")
                put("outboundTag", "proxy")
                put("network", "tcp,udp")
            })

            put("rules", rulesArr)
        }
        root.put("routing", routingObj)

        return root.toString(2)
    }

    fun buildDelayTestConfig(server: XrayServerConfig): String {
        val root = JSONObject()
        val logObj = JSONObject().apply { put("loglevel", "none") }
        root.put("log", logObj)

        val outboundsArr = JSONArray()
        val proxyOutbound = JSONObject().apply { put("tag", "proxy") }
        when (server.protocol.uppercase()) {
            "VLESS" -> buildVlessOutbound(proxyOutbound, server)
            "VMESS" -> buildVmessOutbound(proxyOutbound, server)
            "TROJAN" -> buildTrojanOutbound(proxyOutbound, server)
            "SHADOWSOCKS" -> buildShadowsocksOutbound(proxyOutbound, server)
            else -> buildVlessOutbound(proxyOutbound, server)
        }
        proxyOutbound.put("streamSettings", buildStreamSettings(server))
        outboundsArr.put(proxyOutbound)
        root.put("outbounds", outboundsArr)
        return root.toString()
    }


    private fun buildVlessOutbound(json: JSONObject, server: XrayServerConfig) {
        json.put("protocol", "vless")
        val settings = JSONObject()
        val vnext = JSONArray()
        val serverObj = JSONObject().apply {
            put("address", server.address)
            put("port", server.port)
            val users = JSONArray().apply {
                val user = JSONObject().apply {
                    put("id", server.uuid ?: "")
                    put("encryption", "none")
                    if (!server.flow.isNullOrBlank()) {
                        put("flow", server.flow)
                    }
                }
                put(user)
            }
            put("users", users)
        }
        vnext.put(serverObj)
        settings.put("vnext", vnext)
        json.put("settings", settings)
    }

    private fun buildVmessOutbound(json: JSONObject, server: XrayServerConfig) {
        json.put("protocol", "vmess")
        val settings = JSONObject()
        val vnext = JSONArray()
        val serverObj = JSONObject().apply {
            put("address", server.address)
            put("port", server.port)
            val users = JSONArray().apply {
                val user = JSONObject().apply {
                    put("id", server.uuid ?: "")
                    put("alterId", server.alterId)
                    put("security", "auto")
                }
                put(user)
            }
            put("users", users)
        }
        vnext.put(serverObj)
        settings.put("vnext", vnext)
        json.put("settings", settings)
    }

    private fun buildTrojanOutbound(json: JSONObject, server: XrayServerConfig) {
        json.put("protocol", "trojan")
        val settings = JSONObject()
        val servers = JSONArray()
        val s = JSONObject().apply {
            put("address", server.address)
            put("port", server.port)
            put("password", server.password ?: server.uuid ?: "")
        }
        servers.put(s)
        settings.put("servers", servers)
        json.put("settings", settings)
    }

    private fun buildShadowsocksOutbound(json: JSONObject, server: XrayServerConfig) {
        json.put("protocol", "shadowsocks")
        val settings = JSONObject()
        val servers = JSONArray()
        val s = JSONObject().apply {
            put("address", server.address)
            put("port", server.port)
            put("method", server.method ?: "2022-blake3-aes-128-gcm")
            put("password", server.password ?: "")
        }
        servers.put(s)
        settings.put("servers", servers)
        json.put("settings", settings)
    }

    private fun buildStreamSettings(server: XrayServerConfig): JSONObject {
        val stream = JSONObject()
        val net = server.streamNetwork.lowercase()
        stream.put("network", net)

        when (server.security.lowercase()) {
            "reality" -> {
                stream.put("security", "reality")
                val realitySettings = JSONObject().apply {
                    put("show", false)
                    put("fingerprint", server.fingerprint ?: "chrome")
                    put("serverName", server.serverName ?: server.address)
                    put("publicKey", server.publicKey ?: "")
                    put("shortId", server.shortId ?: "")
                    put("spiderX", server.spiderX ?: "/")
                }
                stream.put("realitySettings", realitySettings)
            }
            "tls" -> {
                stream.put("security", "tls")
                val tlsSettings = JSONObject().apply {
                    put("allowInsecure", false)
                    put("serverName", server.serverName ?: server.host ?: server.address)
                    put("fingerprint", server.fingerprint ?: "chrome")
                    val alpnArr = JSONArray().apply {
                        put("http/1.1")
                        put("h2")
                    }
                    put("alpn", alpnArr)
                }
                stream.put("tlsSettings", tlsSettings)
            }
            else -> {
                stream.put("security", "none")
            }
        }

        when (net) {
            "ws" -> {
                val wsSettings = JSONObject().apply {
                    put("path", server.path ?: "/")
                    val headers = JSONObject()
                    val hostVal = server.host ?: server.serverName ?: server.address
                    if (hostVal.isNotBlank()) {
                        headers.put("Host", hostVal)
                    }
                    put("headers", headers)
                }
                stream.put("wsSettings", wsSettings)
            }
            "httpupgrade" -> {
                val huSettings = JSONObject().apply {
                    put("path", server.path ?: "/")
                    val hostVal = server.host ?: server.serverName ?: server.address
                    if (hostVal.isNotBlank()) {
                        put("host", hostVal)
                    }
                }
                stream.put("httpupgradeSettings", huSettings)
            }
            "xhttp", "splithttp" -> {
                val xhttpSettings = JSONObject().apply {
                    put("path", server.path ?: "/")
                    val hostVal = server.host ?: server.serverName ?: server.address
                    if (hostVal.isNotBlank()) {
                        put("host", hostVal)
                    }
                    put("mode", "auto")
                }
                stream.put("xhttpSettings", xhttpSettings)
            }
            "grpc" -> {
                val grpcSettings = JSONObject().apply {
                    put("serviceName", server.serviceName ?: "")
                    put("multiMode", true)
                }
                stream.put("grpcSettings", grpcSettings)
            }
            "tcp" -> {
                val tcpSettings = JSONObject().apply {
                    put("header", JSONObject().apply {
                        put("type", "none")
                    })
                }
                stream.put("tcpSettings", tcpSettings)
            }
        }

        return stream
    }
}
