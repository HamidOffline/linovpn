package ir.linovpn.common

sealed class EngineResult {
    data object Success : EngineResult()
    data class Failure(val error: String, val throwable: Throwable? = null) : EngineResult()
}

data class EngineStats(
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
    val uploadSpeed: Long = 0L,     // Bytes per second
    val downloadSpeed: Long = 0L    // Bytes per second
)

data class TunConfig(
    val fd: Int,
    val mtu: Int = 1500,
    val addressV4: String = "172.19.0.1",
    val prefixV4: Int = 30,
    val addressV6: String = "fdfe:dcba:9876::1",
    val prefixV6: Int = 126,
    val dnsV4: String = "8.8.8.8",
    val dnsV6: String = "2001:4860:4860::8888"
)

interface VpnEngine {
    val name: String
    val version: String
    fun isLibraryLoaded(): Boolean
    suspend fun start(configJsonOrYaml: String, tunConfig: TunConfig? = null): EngineResult
    suspend fun stop(): EngineResult
    fun isRunning(): Boolean
    fun getStats(): EngineStats
    fun getLogs(): List<String>
    fun clearLogs()
}
