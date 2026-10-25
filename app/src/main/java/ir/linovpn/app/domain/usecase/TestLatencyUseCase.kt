package ir.linovpn.app.domain.usecase

import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.repository.SettingsRepository
import ir.linovpn.xray.XrayConfigBuilder
import ir.linovpn.xray.XrayEngine
import ir.linovpn.xray.XrayServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TestLatencyUseCase @Inject constructor(
    private val serverRepository: ServerRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun ping(server: Server, timeoutMs: Int = 4500): Long = withContext(Dispatchers.IO) {
        try {
            val xrayConfig = XrayServerConfig(
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

            val configJson = XrayConfigBuilder.buildDelayTestConfig(xrayConfig)
            val testUrl = try {
                settingsRepository.getSpeedTestUrl().first().ifBlank { "https://www.google.com/generate_204" }
            } catch (_: Throwable) {
                "https://www.google.com/generate_204"
            }

            var realDelay = try {
                XrayEngine.measureDelay(configJson, testUrl)
            } catch (_: Throwable) { -1L }

            if (realDelay <= 0 && testUrl != "https://cp.cloudflare.com/generate_204") {
                // Secondary fallback check via Cloudflare
                realDelay = try {
                    XrayEngine.measureDelay(configJson, "https://cp.cloudflare.com/generate_204")
                } catch (_: Throwable) { -1L }
            }

            val result = if (realDelay > 0) realDelay else -1L
            serverRepository.updateLatency(server.id, result)
            result
        } catch (e: Throwable) {
            serverRepository.updateLatency(server.id, -1L)
            -1L
        }
    }

    suspend fun pingAll(servers: List<Server>, timeoutMs: Int = 5000) = coroutineScope {
        servers.map { server ->
            async(Dispatchers.IO) {
                ping(server, timeoutMs)
            }
        }.awaitAll()
    }
}
