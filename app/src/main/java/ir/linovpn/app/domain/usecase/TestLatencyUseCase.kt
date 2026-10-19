package ir.linovpn.app.domain.usecase

import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.xray.XrayConfigBuilder
import ir.linovpn.xray.XrayEngine
import ir.linovpn.xray.XrayServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TestLatencyUseCase @Inject constructor(
    private val serverRepository: ServerRepository
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
                host = server.hostHeader,
                serviceName = server.serviceName,
                method = server.method,
                password = server.password
            )

            val configJson = XrayConfigBuilder.buildConfig(xrayConfig)
            // Test real delay by fetching 204 response through the proxy tunnel
            var realDelay = XrayEngine.measureDelay(configJson, "https://www.google.com/generate_204")
            if (realDelay <= 0) {
                // Secondary check
                realDelay = XrayEngine.measureDelay(configJson, "https://cp.cloudflare.com/generate_204")
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
