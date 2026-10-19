package ir.linovpn.app.domain.repository

import ir.linovpn.app.domain.model.AppProxy
import ir.linovpn.app.domain.model.DnsConfig
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnCore
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    fun getAllServers(): Flow<List<Server>>
    fun getServersByProfile(profileId: String): Flow<List<Server>>
    fun getSelectedServer(): Flow<Server?>
    suspend fun getServerById(id: String): Server?
    suspend fun insertServer(server: Server)
    suspend fun insertServers(servers: List<Server>)
    suspend fun updateServer(server: Server)
    suspend fun deleteServer(id: String)
    suspend fun deleteAllServers()
    suspend fun deleteServersByProfile(profileId: String)
    suspend fun setSelectedServer(id: String)
    suspend fun updateLatency(id: String, latency: Long)
    suspend fun toggleFavorite(id: String)
}

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<Profile>>
    suspend fun getProfileById(id: String): Profile?
    suspend fun insertProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(id: String)
    suspend fun fetchAndSaveSubscription(profile: Profile): Result<Int>
}

interface SettingsRepository {
    fun getPreferredCore(): Flow<VpnCore>
    suspend fun setPreferredCore(core: VpnCore)
    fun getDnsConfig(): Flow<DnsConfig>
    suspend fun setDnsConfig(config: DnsConfig)
    fun isSplitTunnelingEnabled(): Flow<Boolean>
    suspend fun setSplitTunnelingEnabled(enabled: Boolean)
    fun getBypassApps(): Flow<Set<String>>
    suspend fun setBypassApps(packageNames: Set<String>)
    fun isKillSwitchEnabled(): Flow<Boolean>
    suspend fun setKillSwitchEnabled(enabled: Boolean)
    fun isAutoConnectEnabled(): Flow<Boolean>
    suspend fun setAutoConnectEnabled(enabled: Boolean)
    fun getSocksPort(): Flow<Int>
    suspend fun setSocksPort(port: Int)
    fun getHttpPort(): Flow<Int>
    suspend fun setHttpPort(port: Int)
    fun getThemeMode(): Flow<ir.linovpn.app.domain.model.AppThemeMode>
    suspend fun setThemeMode(mode: ir.linovpn.app.domain.model.AppThemeMode)
}
