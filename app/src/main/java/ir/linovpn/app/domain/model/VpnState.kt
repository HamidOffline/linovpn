package ir.linovpn.app.domain.model

sealed class VpnState {
    data object Disconnected : VpnState()
    data object Connecting : VpnState()
    data class Connected(
        val server: Server,
        val connectedAt: Long = System.currentTimeMillis(),
        val uploadBytes: Long = 0L,
        val downloadBytes: Long = 0L,
        val uploadSpeed: Long = 0L,
        val downloadSpeed: Long = 0L
    ) : VpnState()
    data class Error(val message: String) : VpnState()
    data object Disconnecting : VpnState()
}

data class Profile(
    val id: String,
    val name: String,
    val subscriptionUrl: String,
    val serverCount: Int = 0,
    val lastUpdated: Long = 0L,
    val autoUpdate: Boolean = true,
    val updateIntervalHours: Int = 24
)

data class DnsConfig(
    val primaryDns: String = "1.1.1.1",
    val secondaryDns: String = "8.8.8.8",
    val dohUrl: String = "https://1.1.1.1/dns-query",
    val dnsOverHttps: Boolean = true,
    val fakeDns: Boolean = true,
    val bypassIran: Boolean = true
)

data class AppProxy(
    val packageName: String,
    val appName: String,
    val isBypassed: Boolean = false
)

enum class AppThemeMode(val title: String) {
    SYSTEM("پیروی از سیستم"),
    DARK("تیره (Dark)"),
    LIGHT("روشن (Light)")
}
