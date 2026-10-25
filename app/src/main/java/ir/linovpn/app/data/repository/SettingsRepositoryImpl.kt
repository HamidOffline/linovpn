package ir.linovpn.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.linovpn.app.domain.model.DnsConfig
import ir.linovpn.app.domain.model.VpnCore
import ir.linovpn.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "linovpn_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val PREFERRED_CORE = stringPreferencesKey("preferred_core")
        val PRIMARY_DNS = stringPreferencesKey("primary_dns")
        val SECONDARY_DNS = stringPreferencesKey("secondary_dns")
        val DOH_URL = stringPreferencesKey("doh_url")
        val DNS_OVER_HTTPS = booleanPreferencesKey("dns_over_https")
        val FAKE_DNS = booleanPreferencesKey("fake_dns")
        val BYPASS_IRAN = booleanPreferencesKey("bypass_iran")
        val SPLIT_TUNNELING = booleanPreferencesKey("split_tunneling")
        val BYPASS_APPS = stringSetPreferencesKey("bypass_apps")
        val KILL_SWITCH = booleanPreferencesKey("kill_switch")
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val SOCKS_PORT = intPreferencesKey("socks_port")
        val HTTP_PORT = intPreferencesKey("http_port")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_MODE = stringPreferencesKey("font_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val REMOTE_DNS_NAME = stringPreferencesKey("remote_dns_name")
        val DIRECT_DNS_NAME = stringPreferencesKey("direct_dns_name")
        val SPEED_TEST_URL = stringPreferencesKey("speed_test_url")
        val ROUTING_MODE = stringPreferencesKey("routing_mode")
        val ROUTING_ENABLED = booleanPreferencesKey("routing_enabled")
        val PING_TIMEOUT = intPreferencesKey("ping_timeout")
        val PING_CONCURRENCY = intPreferencesKey("ping_concurrency")
        val AUTO_UPDATE_SUBS = booleanPreferencesKey("auto_update_subs")
        val MUX_ENABLED = booleanPreferencesKey("mux_enabled")
        val FRAGMENT_ENABLED = booleanPreferencesKey("fragment_enabled")
        val SNIFFING_ENABLED = booleanPreferencesKey("sniffing_enabled")
        val STRICT_ROUTE_ENABLED = booleanPreferencesKey("strict_route_enabled")
        val TUN_STACK = stringPreferencesKey("tun_stack")
        val MTU = intPreferencesKey("mtu")
        val AUTO_UPDATE_ON_OPEN = booleanPreferencesKey("auto_update_on_open")
        val SUB_UPDATE_INTERVAL = stringPreferencesKey("sub_update_interval")
    }

    override fun getPreferredCore(): Flow<VpnCore> {
        return context.dataStore.data.map { prefs ->
            val name = prefs[Keys.PREFERRED_CORE] ?: VpnCore.XRAY.name
            runCatching { VpnCore.valueOf(name) }.getOrDefault(VpnCore.XRAY)
        }
    }

    override suspend fun setPreferredCore(core: VpnCore) {
        context.dataStore.edit { it[Keys.PREFERRED_CORE] = core.name }
    }

    override fun getDnsConfig(): Flow<DnsConfig> {
        return context.dataStore.data.map { prefs ->
            DnsConfig(
                primaryDns = prefs[Keys.PRIMARY_DNS] ?: "1.1.1.1",
                secondaryDns = prefs[Keys.SECONDARY_DNS] ?: "8.8.8.8",
                dohUrl = prefs[Keys.DOH_URL] ?: "https://1.1.1.1/dns-query",
                dnsOverHttps = prefs[Keys.DNS_OVER_HTTPS] ?: true,
                fakeDns = prefs[Keys.FAKE_DNS] ?: false,
                bypassIran = prefs[Keys.BYPASS_IRAN] ?: true,
                remoteDnsName = prefs[Keys.REMOTE_DNS_NAME] ?: "Cloudflare DoH",
                directDnsName = prefs[Keys.DIRECT_DNS_NAME] ?: "پیش‌فرض سیستم (System)"
            )
        }
    }

    override suspend fun setDnsConfig(config: DnsConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRIMARY_DNS] = config.primaryDns
            prefs[Keys.SECONDARY_DNS] = config.secondaryDns
            prefs[Keys.DOH_URL] = config.dohUrl
            prefs[Keys.DNS_OVER_HTTPS] = config.dnsOverHttps
            prefs[Keys.FAKE_DNS] = config.fakeDns
            prefs[Keys.BYPASS_IRAN] = config.bypassIran
            prefs[Keys.REMOTE_DNS_NAME] = config.remoteDnsName
            prefs[Keys.DIRECT_DNS_NAME] = config.directDnsName
        }
    }

    override fun getThemeMode(): Flow<ir.linovpn.app.domain.model.AppThemeMode> {
        return context.dataStore.data.map { prefs ->
            val modeStr = prefs[Keys.THEME_MODE] ?: ir.linovpn.app.domain.model.AppThemeMode.DARK.name
            runCatching { ir.linovpn.app.domain.model.AppThemeMode.valueOf(modeStr) }
                .getOrDefault(ir.linovpn.app.domain.model.AppThemeMode.DARK)
        }
    }

    override suspend fun setThemeMode(mode: ir.linovpn.app.domain.model.AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    override fun isSplitTunnelingEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.SPLIT_TUNNELING] ?: false }
    }

    override suspend fun setSplitTunnelingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SPLIT_TUNNELING] = enabled }
    }

    override fun getBypassApps(): Flow<Set<String>> {
        return context.dataStore.data.map { it[Keys.BYPASS_APPS] ?: emptySet() }
    }

    override suspend fun setBypassApps(packageNames: Set<String>) {
        context.dataStore.edit { it[Keys.BYPASS_APPS] = packageNames }
    }

    override fun isKillSwitchEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.KILL_SWITCH] ?: false }
    }

    override suspend fun setKillSwitchEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KILL_SWITCH] = enabled }
    }

    override fun isAutoConnectEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.AUTO_CONNECT] ?: false }
    }

    override suspend fun setAutoConnectEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_CONNECT] = enabled }
    }

    override fun getSocksPort(): Flow<Int> {
        return context.dataStore.data.map { it[Keys.SOCKS_PORT] ?: 10808 }
    }

    override suspend fun setSocksPort(port: Int) {
        context.dataStore.edit { it[Keys.SOCKS_PORT] = port }
    }

    override fun getHttpPort(): Flow<Int> {
        return context.dataStore.data.map { it[Keys.HTTP_PORT] ?: 10809 }
    }

    override suspend fun setHttpPort(port: Int) {
        context.dataStore.edit { it[Keys.HTTP_PORT] = port }
    }

    override fun getAppFont(): Flow<ir.linovpn.app.domain.model.AppFont> {
        return context.dataStore.data.map { prefs ->
            val fontStr = prefs[Keys.FONT_MODE] ?: ir.linovpn.app.domain.model.AppFont.VAZIRMATN.name
            runCatching { ir.linovpn.app.domain.model.AppFont.valueOf(fontStr) }
                .getOrDefault(ir.linovpn.app.domain.model.AppFont.VAZIRMATN)
        }
    }

    override suspend fun setAppFont(font: ir.linovpn.app.domain.model.AppFont) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_MODE] = font.name
        }
    }

    override fun getAppLanguage(): Flow<ir.linovpn.app.domain.model.AppLanguage> {
        return context.dataStore.data.map { prefs ->
            val langStr = prefs[Keys.APP_LANGUAGE] ?: ir.linovpn.app.domain.model.AppLanguage.FA.name
            runCatching { ir.linovpn.app.domain.model.AppLanguage.valueOf(langStr) }
                .getOrDefault(ir.linovpn.app.domain.model.AppLanguage.FA)
        }
    }

    override suspend fun setAppLanguage(lang: ir.linovpn.app.domain.model.AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = lang.name
        }
    }

    override fun getSpeedTestUrl(): Flow<String> {
        return context.dataStore.data.map { it[Keys.SPEED_TEST_URL] ?: "https://www.google.com/generate_204" }
    }

    override suspend fun setSpeedTestUrl(url: String) {
        context.dataStore.edit { it[Keys.SPEED_TEST_URL] = url }
    }

    override fun getRoutingMode(): Flow<ir.linovpn.app.domain.model.RoutingMode> {
        return context.dataStore.data.map { prefs ->
            val mode = prefs[Keys.ROUTING_MODE] ?: ir.linovpn.app.domain.model.RoutingMode.BYPASS_IRAN.name
            runCatching { ir.linovpn.app.domain.model.RoutingMode.valueOf(mode) }.getOrDefault(ir.linovpn.app.domain.model.RoutingMode.BYPASS_IRAN)
        }
    }

    override suspend fun setRoutingMode(mode: ir.linovpn.app.domain.model.RoutingMode) {
        context.dataStore.edit { it[Keys.ROUTING_MODE] = mode.name }
    }

    override fun isRoutingEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.ROUTING_ENABLED] ?: true }
    }

    override suspend fun setRoutingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ROUTING_ENABLED] = enabled }
    }

    override fun getPingTimeout(): Flow<Int> {
        return context.dataStore.data.map { it[Keys.PING_TIMEOUT] ?: 3000 }
    }

    override suspend fun setPingTimeout(timeoutMs: Int) {
        context.dataStore.edit { it[Keys.PING_TIMEOUT] = timeoutMs }
    }

    override fun getPingConcurrency(): Flow<Int> {
        return context.dataStore.data.map { it[Keys.PING_CONCURRENCY] ?: 10 }
    }

    override suspend fun setPingConcurrency(count: Int) {
        context.dataStore.edit { it[Keys.PING_CONCURRENCY] = count }
    }

    override fun isAutoUpdateSubscriptions(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.AUTO_UPDATE_SUBS] ?: false }
    }

    override suspend fun setAutoUpdateSubscriptions(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_UPDATE_SUBS] = enabled }
    }

    override fun isMuxEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.MUX_ENABLED] ?: false }
    }

    override suspend fun setMuxEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUX_ENABLED] = enabled }
    }

    override fun isFragmentEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.FRAGMENT_ENABLED] ?: false }
    }

    override suspend fun setFragmentEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FRAGMENT_ENABLED] = enabled }
    }

    override fun isSniffingEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.SNIFFING_ENABLED] ?: true }
    }

    override suspend fun setSniffingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SNIFFING_ENABLED] = enabled }
    }

    override fun isStrictRouteEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.STRICT_ROUTE_ENABLED] ?: false }
    }

    override suspend fun setStrictRouteEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRICT_ROUTE_ENABLED] = enabled }
    }

    override fun getTunStack(): Flow<String> {
        return context.dataStore.data.map { it[Keys.TUN_STACK] ?: "gVisor" }
    }

    override suspend fun setTunStack(stack: String) {
        context.dataStore.edit { it[Keys.TUN_STACK] = stack }
    }

    override fun getMtu(): Flow<Int> {
        return context.dataStore.data.map { it[Keys.MTU] ?: 1500 }
    }

    override suspend fun setMtu(mtu: Int) {
        context.dataStore.edit { it[Keys.MTU] = mtu }
    }

    override fun isAutoUpdateOnOpen(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.AUTO_UPDATE_ON_OPEN] ?: false }
    }

    override suspend fun setAutoUpdateOnOpen(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_UPDATE_ON_OPEN] = enabled }
    }

    override fun getSubscriptionUpdateInterval(): Flow<String> {
        return context.dataStore.data.map { it[Keys.SUB_UPDATE_INTERVAL] ?: "12 Hours" }
    }

    override suspend fun setSubscriptionUpdateInterval(interval: String) {
        context.dataStore.edit { it[Keys.SUB_UPDATE_INTERVAL] = interval }
    }
}

