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
                fakeDns = prefs[Keys.FAKE_DNS] ?: true,
                bypassIran = prefs[Keys.BYPASS_IRAN] ?: true
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
}
