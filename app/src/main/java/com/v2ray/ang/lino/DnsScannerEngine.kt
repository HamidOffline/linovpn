package com.v2ray.ang.lino

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

data class ScannedResolver(
    val ip: String,
    val port: Int = 53,
    val latencyMs: Int,
    val ednsSupported: Boolean,
    val cleanNxDomain: Boolean
) {
    val address: String get() = "$ip:$port"
}

data class DnsScanState(
    val isScanning: Boolean = false,
    val totalTargets: Int = 0,
    val scannedCount: Int = 0,
    val results: List<ScannedResolver> = emptyList()
)

/**
 * Multi-threaded SlipNet-compatible Iranian DNS Resolver Scanner.
 * Probes bundled Unbar 2,307 IR resolvers, SlipNet IR DNS CIDRs (lite/full), or custom CIDRs/IPs
 * with real UDP/53 EDNS0 queries + NXDOMAIN hijack verification.
 */
object DnsScannerEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null

    private val _state = MutableStateFlow(DnsScanState())
    val state: StateFlow<DnsScanState> = _state.asStateFlow()

    fun startScan(
        context: Context,
        source: String,
        customInput: String,
        testDomain: String,
        threads: Int,
        timeoutMs: Int,
        maxTargets: Int = 800
    ) {
        stopScan()
        scanJob = scope.launch {
            val targets = loadTargets(context, source, customInput, maxTargets)
            if (targets.isEmpty()) {
                _state.value = DnsScanState(isScanning = false, totalTargets = 0, scannedCount = 0, results = emptyList())
                return@launch
            }
            _state.value = DnsScanState(isScanning = true, totalTargets = targets.size, scannedCount = 0, results = emptyList())

            val sem = Semaphore(threads.coerceIn(4, 128))
            val done = AtomicInteger(0)
            val found = java.util.Collections.synchronizedList(mutableListOf<ScannedResolver>())

            targets.map { ip ->
                async {
                    sem.withPermit {
                        if (!isActive) return@withPermit
                        val res = probeResolver(ip, 53, testDomain, timeoutMs)
                        val count = done.incrementAndGet()
                        if (res != null) {
                            found.add(res)
                        }
                        if (res != null || count % 12 == 0 || count == targets.size) {
                            val sorted = synchronized(found) { found.sortedBy { it.latencyMs } }
                            _state.value = DnsScanState(
                                isScanning = count < targets.size,
                                totalTargets = targets.size,
                                scannedCount = count,
                                results = sorted
                            )
                        }
                    }
                }
            }.awaitAll()

            val finalSorted = synchronized(found) { found.sortedBy { it.latencyMs } }
            _state.value = DnsScanState(
                isScanning = false,
                totalTargets = targets.size,
                scannedCount = targets.size,
                results = finalSorted
            )
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _state.value = _state.value.copy(isScanning = false)
    }

    private fun loadTargets(context: Context, source: String, customInput: String, maxTargets: Int): List<String> {
        val rawLines = when (source) {
            "custom" -> customInput.lines()
            "ir_lite" -> readAssetLines(context, "geo/ir_dns_lite.cidr")
            "ir_full" -> readAssetLines(context, "geo/ir_dns.cidr")
            else -> readAssetLines(context, "unbar_ir_resolvers.txt")
        }

        val ips = LinkedHashSet<String>()
        for (raw in rawLines) {
            val line = raw.trim().substringBefore("#").trim()
            if (line.isEmpty()) continue
            if (line.contains("/")) {
                expandCidr(line, maxPerCidr = 64).forEach {
                    if (ips.size < maxTargets) ips.add(it)
                }
            } else {
                val ip = line.substringBefore(":").trim()
                if (isValidIpv4(ip)) ips.add(ip)
            }
            if (ips.size >= maxTargets) break
        }
        return ips.toList().shuffled()
    }

    private fun readAssetLines(context: Context, assetPath: String): List<String> {
        return try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readLines() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun expandCidr(cidr: String, maxPerCidr: Int): List<String> {
        val parts = cidr.split("/")
        if (parts.size != 2) return emptyList()
        val ipParts = parts[0].split(".").mapNotNull { it.toIntOrNull() }
        val prefix = parts[1].toIntOrNull() ?: return emptyList()
        if (ipParts.size != 4 || prefix !in 16..32) return emptyList()

        val base = ((ipParts[0].toLong() shl 24) or (ipParts[1].toLong() shl 16) or (ipParts[2].toLong() shl 8) or ipParts[3].toLong())
        val hostBits = 32 - prefix
        val total = if (hostBits == 0) 1L else (1L shl hostBits)
        val start = if (total > 2) base + 1 else base
        val count = if (total > 2) (total - 2).coerceAtMost(maxPerCidr.toLong()) else total
        val out = mutableListOf<String>()
        for (i in 0 until count) {
            val v = start + i
            out.add("${(v shr 24) and 0xFF}.${(v shr 16) and 0xFF}.${(v shr 8) and 0xFF}.${v and 0xFF}")
        }
        return out
    }

    private fun isValidIpv4(ip: String): Boolean {
        val p = ip.split(".")
        if (p.size != 4) return false
        return p.all { it.toIntOrNull()?.let { n -> n in 0..255 } == true }
    }

    private fun probeResolver(ip: String, port: Int, domain: String, timeoutMs: Int): ScannedResolver? {
        return try {
            DatagramSocket().use { sock ->
                sock.soTimeout = timeoutMs
                val addr = InetAddress.getByName(ip)

                // 1. Send EDNS0 A query for testDomain
                val txId = Random.nextInt(1, 65535)
                val query = buildDnsQuery(txId, domain, qType = 1, includeEdns = true)
                val t0 = System.currentTimeMillis()
                sock.send(DatagramPacket(query, query.size, addr, port))

                val buf = ByteArray(1500)
                val resp = DatagramPacket(buf, buf.size)
                sock.receive(resp)
                val latency = (System.currentTimeMillis() - t0).toInt().coerceAtLeast(1)

                if (resp.length < 12) return null
                val rId = ((buf[0].toInt() and 0xFF) shl 8) or (buf[1].toInt() and 0xFF)
                if (rId != txId) return null
                val rcode = buf[3].toInt() and 0x0F
                val anCount = ((buf[6].toInt() and 0xFF) shl 8) or (buf[7].toInt() and 0xFF)
                val arCount = ((buf[10].toInt() and 0xFF) shl 8) or (buf[11].toInt() and 0xFF)
                if (rcode != 0 || anCount == 0) return null
                val ednsOk = arCount > 0

                // 2. Quick NXDOMAIN hijack probe (random non-existent domain should return rcode=3)
                val nxId = Random.nextInt(1, 65535)
                val nxDomain = "lino${Random.nextInt(100000, 999999)}.invalid"
                val nxQuery = buildDnsQuery(nxId, nxDomain, qType = 1, includeEdns = false)
                var cleanNx = true
                try {
                    sock.send(DatagramPacket(nxQuery, nxQuery.size, addr, port))
                    val nxResp = DatagramPacket(buf, buf.size)
                    sock.receive(nxResp)
                    if (nxResp.length >= 12) {
                        val nxRcode = buf[3].toInt() and 0x0F
                        val nxAn = ((buf[6].toInt() and 0xFF) shl 8) or (buf[7].toInt() and 0xFF)
                        if (nxRcode == 0 && nxAn > 0) cleanNx = false // DNS hijacker!
                    }
                } catch (_: Exception) {
                }

                ScannedResolver(
                    ip = ip,
                    port = port,
                    latencyMs = latency,
                    ednsSupported = ednsOk,
                    cleanNxDomain = cleanNx
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildDnsQuery(txId: Int, domain: String, qType: Int, includeEdns: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        // Header (12 bytes)
        out.write((txId shr 8) and 0xFF)
        out.write(txId and 0xFF)
        out.write(0x01); out.write(0x00) // Standard query, RD=1
        out.write(0x00); out.write(0x01) // QDCOUNT=1
        out.write(0x00); out.write(0x00) // ANCOUNT=0
        out.write(0x00); out.write(0x00) // NSCOUNT=0
        out.write(0x00); out.write(if (includeEdns) 0x01 else 0x00) // ARCOUNT=1 for EDNS0

        // QNAME
        for (label in domain.split(".")) {
            if (label.isEmpty()) continue
            val bytes = label.toByteArray(Charsets.US_ASCII)
            out.write(bytes.size)
            out.write(bytes)
        }
        out.write(0x00)
        out.write((qType shr 8) and 0xFF); out.write(qType and 0xFF)
        out.write(0x00); out.write(0x01) // QCLASS=IN

        // OPT RR for EDNS0 (UDP payload 1232)
        if (includeEdns) {
            out.write(0x00) // root
            out.write(0x00); out.write(0x29) // TYPE=OPT(41)
            out.write(0x04); out.write(0xD0) // UDP payload = 1232
            out.write(0x00); out.write(0x00); out.write(0x00); out.write(0x00) // ext RCODE & flags
            out.write(0x00); out.write(0x00) // RDLEN=0
        }
        return out.toByteArray()
    }
}
