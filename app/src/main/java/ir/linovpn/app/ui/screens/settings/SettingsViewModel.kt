package ir.linovpn.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.domain.model.DnsConfig
import ir.linovpn.app.domain.model.VpnCore
import ir.linovpn.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val preferredCore: StateFlow<VpnCore> = settingsRepository.getPreferredCore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VpnCore.XRAY)

    val dnsConfig: StateFlow<DnsConfig> = settingsRepository.getDnsConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DnsConfig())

    val isSplitTunneling: StateFlow<Boolean> = settingsRepository.isSplitTunnelingEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bypassApps: StateFlow<Set<String>> = settingsRepository.getBypassApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isKillSwitch: StateFlow<Boolean> = settingsRepository.isKillSwitchEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAutoConnect: StateFlow<Boolean> = settingsRepository.isAutoConnectEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val socksPort: StateFlow<Int> = settingsRepository.getSocksPort()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10808)

    val httpPort: StateFlow<Int> = settingsRepository.getHttpPort()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10809)

    val themeMode: StateFlow<ir.linovpn.app.domain.model.AppThemeMode> = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ir.linovpn.app.domain.model.AppThemeMode.DARK)

    fun setThemeMode(mode: ir.linovpn.app.domain.model.AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setPreferredCore(core: VpnCore) {
        viewModelScope.launch {
            settingsRepository.setPreferredCore(core)
        }
    }

    fun setDnsConfig(config: DnsConfig) {
        viewModelScope.launch {
            settingsRepository.setDnsConfig(config)
        }
    }

    fun setSplitTunneling(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSplitTunnelingEnabled(enabled)
        }
    }

    fun setKillSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setKillSwitchEnabled(enabled)
        }
    }

    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoConnectEnabled(enabled)
        }
    }

    fun setPorts(socks: Int, http: Int) {
        viewModelScope.launch {
            settingsRepository.setSocksPort(socks)
            settingsRepository.setHttpPort(http)
        }
    }
}
