package com.v2ray.ang.lino

import android.util.Base64
import app.slipnet.util.LockPasswordUtil
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Unbar URI & JSON parser/serializer.
 * Supports unbar://<base64url-json> and Unbar JSON arrays.
 */
object UnbarFmt {

    fun parse(str: String): ProfileItem? {
        val trimmed = str.trim()
        if (!trimmed.startsWith("unbar://", ignoreCase = true)) return null
        return try {
            val payload = trimmed.substringAfter("://").substringBefore("#").trim()
            val padded = when (payload.length % 4) {
                2 -> "$payload=="
                3 -> "$payload="
                else -> payload
            }
            val jsonStr = String(Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
            fromJsonObject(JSONObject(jsonStr))
        } catch (_: Exception) {
            null
        }
    }

    fun parseJsonArray(jsonText: String): List<ProfileItem> {
        val list = mutableListOf<ProfileItem>()
        try {
            val arr = JSONArray(jsonText.trim())
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                fromJsonObject(obj)?.let { list.add(it) }
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun fromJsonObject(obj: JSONObject): ProfileItem? {
        val zone = obj.optString("zone", "").trim()
        val pubkey = obj.optString("pubkey", "").trim()
        if (zone.isEmpty() || pubkey.isEmpty()) return null

        val name = obj.optString("name", "").ifBlank { "Unbar ($zone)" }
        val resolverMode = obj.optString("resolverMode", "DISCOVERY").uppercase()
        val country = obj.optString("country", "IR").ifBlank { "IR" }
        val resolversArr = obj.optJSONArray("resolvers")
        val resList = mutableListOf<String>()
        if (resolversArr != null) {
            for (i in 0 until resolversArr.length()) {
                val r = resolversArr.optString(i, "").trim()
                if (r.isNotEmpty()) resList.add(r)
            }
        }

        val item = ProfileItem.create(EConfigType.UNBAR)
        item.remarks = name
        item.server = zone
        item.serverPort = "53"
        item.publicKey = pubkey
        item.mode = resolverMode
        item.host = country
        item.seed = resList.joinToString(",")
        item.xhttpExtra = obj.toString()
        return item
    }

    fun toUri(config: ProfileItem): String {
        val obj = JSONObject()
            .put("v", 1)
            .put("name", config.remarks.ifBlank { "Unbar" })
            .put("zone", config.server.orEmpty())
            .put("pubkey", config.publicKey.orEmpty())
            .put("resolverMode", config.mode?.ifBlank { "DISCOVERY" } ?: "DISCOVERY")
            .put("country", config.host?.ifBlank { "IR" } ?: "IR")
        val resArr = JSONArray()
        config.seed.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { resArr.put(it) }
        obj.put("resolvers", resArr)
        val b64 = Base64.encodeToString(
            obj.toString().toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        return "unbar://$b64"
    }
}

/**
 * SlipNet URI parser/serializer.
 * Supports slipnet://<base64> and encrypted slipnet-enc://<base64> (decrypted natively via libslipstream.so).
 */
object SlipNetFmt {

    fun parse(str: String): ProfileItem? {
        val line = str.trim()
        val decoded: String = when {
            line.startsWith("slipnet-enc://", ignoreCase = true) -> {
                try {
                    val rawBytes = Base64.decode(line.substring("slipnet-enc://".length).trim(), Base64.NO_WRAP)
                    LockPasswordUtil.decryptConfig(rawBytes)
                } catch (_: Exception) {
                    return null
                }
            }
            line.startsWith("slipnet://", ignoreCase = true) -> {
                try {
                    String(Base64.decode(line.substring("slipnet://".length).trim(), Base64.NO_WRAP), Charsets.UTF_8)
                } catch (_: Exception) {
                    return null
                }
            }
            else -> return null
        }

        val parts = decoded.split("|")
        if (parts.size < 11) return null

        val rawType = parts.getOrNull(1)?.lowercase()?.trim().orEmpty()
        val tunnelType = when (rawType) {
            "ss", "slipstream" -> "slipstream"
            "slipstream_ssh" -> "slipstream_ssh"
            "dnstt" -> "dnstt"
            "dnstt_ssh" -> "dnstt_ssh"
            "sayedns", "noizdns" -> "noizdns"
            "sayedns_ssh", "noizdns_ssh" -> "noizdns_ssh"
            "vaydns" -> "vaydns"
            "vaydns_ssh" -> "vaydns_ssh"
            "soar" -> "soar"
            "soar_ssh" -> "soar_ssh"
            "ssh" -> "ssh"
            "doh" -> "doh"
            "snowflake", "tor" -> "tor"
            "naive" -> "naive"
            "naive_ssh" -> "naive_ssh"
            else -> rawType.ifEmpty { "dnstt" }
        }

        val name = parts.getOrNull(2)?.ifBlank { null } ?: "SlipNet ($tunnelType)"
        val domain = parts.getOrNull(3).orEmpty().trim()
        val resolvers = parts.getOrNull(4).orEmpty().trim()
        val authMode = parts.getOrNull(5) == "1" || parts.getOrNull(5).equals("true", true)
        val keepAlive = parts.getOrNull(6)?.toIntOrNull() ?: 5000
        val cc = parts.getOrNull(7)?.ifBlank { "bbr" } ?: "bbr"
        val port = parts.getOrNull(8)?.toIntOrNull() ?: 53
        val host = parts.getOrNull(9).orEmpty().trim()
        val gso = parts.getOrNull(10) == "1"
        val pubkey = parts.getOrNull(11).orEmpty().trim()
        val socksUser = parts.getOrNull(12).orEmpty()
        val socksPass = parts.getOrNull(13).orEmpty()
        val sshUser = parts.getOrNull(15).orEmpty()
        val sshPass = parts.getOrNull(16).orEmpty()
        val sshPort = parts.getOrNull(17)?.toIntOrNull() ?: 22
        val sshHost = parts.getOrNull(19).orEmpty()
        val dohUrl = parts.getOrNull(21).orEmpty()
        val dnsTransport = parts.getOrNull(22).orEmpty().ifBlank { "udp" }
        val torBridgesB64 = parts.getOrNull(26).orEmpty()
        val torBridges = if (torBridgesB64.isNotBlank()) {
            try { String(Base64.decode(torBridgesB64, Base64.NO_WRAP), Charsets.UTF_8) } catch (_: Exception) { "" }
        } else ""
        val naivePort = parts.getOrNull(28)?.toIntOrNull() ?: 443
        val naiveUser = parts.getOrNull(29).orEmpty()
        val naivePassB64 = parts.getOrNull(30).orEmpty()
        val naivePass = if (naivePassB64.isNotBlank()) {
            try { String(Base64.decode(naivePassB64, Base64.NO_WRAP), Charsets.UTF_8) } catch (_: Exception) { "" }
        } else ""
        val noizStealth = parts.getOrNull(38) == "1"
        val maxPayload = parts.getOrNull(39)?.toIntOrNull() ?: 0

        val meta = JSONObject()
            .put("tunnelType", tunnelType)
            .put("domain", domain)
            .put("resolvers", resolvers)
            .put("authMode", authMode)
            .put("keepAlive", keepAlive)
            .put("cc", cc)
            .put("host", host)
            .put("port", port)
            .put("gso", gso)
            .put("pubkey", pubkey)
            .put("socksUser", socksUser)
            .put("socksPass", socksPass)
            .put("sshHost", sshHost)
            .put("sshPort", sshPort)
            .put("sshUser", sshUser)
            .put("sshPass", sshPass)
            .put("dohUrl", dohUrl)
            .put("dnsTransport", dnsTransport)
            .put("torBridges", torBridges)
            .put("naivePort", naivePort)
            .put("naiveUser", naiveUser)
            .put("naivePass", naivePass)
            .put("noizStealth", noizStealth)
            .put("maxPayload", maxPayload)
            .put("rawUri", line)

        val item = ProfileItem.create(EConfigType.SLIPNET)
        item.remarks = name
        item.method = tunnelType
        item.server = domain.ifBlank { host.ifBlank { sshHost.ifBlank { "slipnet" } } }
        item.serverPort = when {
            tunnelType.startsWith("naive") -> naivePort.toString()
            tunnelType == "ssh" -> sshPort.toString()
            else -> port.toString()
        }
        item.publicKey = pubkey
        item.seed = resolvers
        item.xhttpExtra = meta.toString()
        return item
    }

    fun toUri(config: ProfileItem): String {
        val meta = try { JSONObject(config.xhttpExtra.orEmpty()) } catch (_: Exception) { JSONObject() }
        val existingRaw = meta.optString("rawUri", "")
        if (existingRaw.startsWith("slipnet://") || existingRaw.startsWith("slipnet-enc://")) {
            return existingRaw
        }
        val tt = config.method ?: meta.optString("tunnelType", "dnstt")
        val pipe = listOf(
            "18", tt, config.remarks,
            config.server.orEmpty(),
            config.seed.orEmpty(),
            if (meta.optBoolean("authMode", false)) "1" else "0",
            meta.optInt("keepAlive", 5000).toString(),
            meta.optString("cc", "bbr"),
            config.serverPort ?: "53",
            meta.optString("host", ""),
            if (meta.optBoolean("gso", false)) "1" else "0",
            config.publicKey.orEmpty(),
            meta.optString("socksUser", ""),
            meta.optString("socksPass", "")
        ).joinToString("|")
        val b64 = Base64.encodeToString(pipe.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "slipnet://$b64"
    }
}
