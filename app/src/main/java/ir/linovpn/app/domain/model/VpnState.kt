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
    val updateIntervalHours: Int = 24,
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val expireTimestamp: Long = 0L,
    val trafficSummary: String? = null
) {
    fun getTrafficDisplay(): String? {
        if (!trafficSummary.isNullOrBlank()) {
            return trafficSummary
        }
        if (totalBytes > 0) {
            val used = uploadBytes + downloadBytes
            val remaining = maxOf(0L, totalBytes - used)
            val remGb = String.format(java.util.Locale.US, "%.2f", remaining.toDouble() / (1024 * 1024 * 1024))
            val totalGb = String.format(java.util.Locale.US, "%.1f", totalBytes.toDouble() / (1024 * 1024 * 1024))

            val daysLeft = if (expireTimestamp > 0) {
                val nowSec = System.currentTimeMillis() / 1000
                val diffSec = expireTimestamp - nowSec
                if (diffSec > 0) (diffSec / 86400).toInt() else 0
            } else -1

            return if (daysLeft >= 0) {
                "$remGb GB / $totalGb GB • $daysLeft روز باقی‌مانده"
            } else {
                "$remGb GB / $totalGb GB باقی‌مانده"
            }
        }
        return null
    }
}

data class DnsConfig(
    val primaryDns: String = "1.1.1.1",
    val secondaryDns: String = "8.8.8.8",
    val dohUrl: String = "https://1.1.1.1/dns-query",
    val dnsOverHttps: Boolean = true,
    val fakeDns: Boolean = false,
    val bypassIran: Boolean = true,
    val remoteDnsName: String = "Cloudflare DoH",
    val directDnsName: String = "پیش‌فرض سیستم (System)"
)

data class AppProxy(
    val packageName: String,
    val appName: String,
    val isBypassed: Boolean = false
)

enum class AppThemeMode(val title: String) {
    SYSTEM("پیروی از سیستم"),
    DARK("مشکی اولد"),
    LIGHT("تم روشن")
}

enum class AppFont(val title: String, val displayName: String) {
    VAZIRMATN("وزیرمتن", "وزیرمتن (Vazirmatn)"),
    YEKAN("یکان‌بخ", "یکان‌بخ (Yekan Bakh)"),
    SYSTEM("سیستم", "پیش‌فرض سیستم (System)")
}

enum class AppLanguage(val code: String, val title: String, val displayName: String) {
    FA("fa", "فارسی", "فارسی (Persian)"),
    EN("en", "English", "English (انگلیسی)"),
    SYSTEM("system", "سیستم", "پیروی از سیستم (System Default)")
}

enum class RoutingMode(val title: String) {
    BYPASS_LAN("Bypass LAN"),
    BYPASS_IRAN("Bypass Iranian Websites"),
    BYPASS_LAN_AND_IRAN("Bypass LAN & Iran"),
    GLOBAL("Global")
}
