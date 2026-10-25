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

    val appFont: StateFlow<ir.linovpn.app.domain.model.AppFont> = settingsRepository.getAppFont()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ir.linovpn.app.domain.model.AppFont.VAZIRMATN)

    val appLanguage: StateFlow<ir.linovpn.app.domain.model.AppLanguage> = settingsRepository.getAppLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ir.linovpn.app.domain.model.AppLanguage.FA)

    val routingMode: StateFlow<ir.linovpn.app.domain.model.RoutingMode> = settingsRepository.getRoutingMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ir.linovpn.app.domain.model.RoutingMode.BYPASS_IRAN)

    val isRoutingEnabled: StateFlow<Boolean> = settingsRepository.isRoutingEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val speedTestUrl: StateFlow<String> = settingsRepository.getSpeedTestUrl()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://www.google.com/generate_204")

    val pingTimeout: StateFlow<Int> = settingsRepository.getPingTimeout()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3000)

    val pingConcurrency: StateFlow<Int> = settingsRepository.getPingConcurrency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val autoUpdateSubscriptions: StateFlow<Boolean> = settingsRepository.isAutoUpdateSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val muxEnabled: StateFlow<Boolean> = settingsRepository.isMuxEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fragmentEnabled: StateFlow<Boolean> = settingsRepository.isFragmentEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sniffingEnabled: StateFlow<Boolean> = settingsRepository.isSniffingEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val strictRouteEnabled: StateFlow<Boolean> = settingsRepository.isStrictRouteEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val tunStack: StateFlow<String> = settingsRepository.getTunStack()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gVisor")

    val mtu: StateFlow<Int> = settingsRepository.getMtu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500)

    val isAutoUpdateOnOpen: StateFlow<Boolean> = settingsRepository.isAutoUpdateOnOpen()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val subscriptionUpdateInterval: StateFlow<String> = settingsRepository.getSubscriptionUpdateInterval()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "12 Hours")

    fun setTunStack(stack: String) {
        viewModelScope.launch { settingsRepository.setTunStack(stack) }
    }

    fun setMtu(mtuValue: Int) {
        viewModelScope.launch { settingsRepository.setMtu(mtuValue) }
    }

    fun setAutoUpdateOnOpen(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoUpdateOnOpen(enabled) }
    }

    fun setSubscriptionUpdateInterval(interval: String) {
        viewModelScope.launch { settingsRepository.setSubscriptionUpdateInterval(interval) }
    }

    fun setSniffingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSniffingEnabled(enabled) }
    }

    fun setStrictRouteEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setStrictRouteEnabled(enabled) }
    }


    fun setRoutingMode(mode: ir.linovpn.app.domain.model.RoutingMode) {
        viewModelScope.launch {
            settingsRepository.setRoutingMode(mode)
        }
    }

    fun setRoutingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRoutingEnabled(enabled)
        }
    }

    fun setSpeedTestUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setSpeedTestUrl(url.trim())
        }
    }

    fun setPingTimeout(timeoutMs: Int) {
        viewModelScope.launch {
            settingsRepository.setPingTimeout(timeoutMs)
        }
    }

    fun setPingConcurrency(count: Int) {
        viewModelScope.launch {
            settingsRepository.setPingConcurrency(count)
        }
    }

    fun setAutoUpdateSubscriptions(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoUpdateSubscriptions(enabled)
        }
    }

    fun setMuxEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMuxEnabled(enabled)
        }
    }

    fun setFragmentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFragmentEnabled(enabled)
        }
    }

    fun setThemeMode(mode: ir.linovpn.app.domain.model.AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setAppFont(font: ir.linovpn.app.domain.model.AppFont) {
        viewModelScope.launch {
            settingsRepository.setAppFont(font)
        }
    }

    fun setAppLanguage(lang: ir.linovpn.app.domain.model.AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(lang)
        }
    }

    fun setRemoteDns(name: String, primary: String, secondary: String, dohUrl: String, isDoh: Boolean) {
        viewModelScope.launch {
            val current = dnsConfig.value
            settingsRepository.setDnsConfig(
                current.copy(
                    remoteDnsName = name,
                    primaryDns = primary,
                    secondaryDns = secondary,
                    dohUrl = dohUrl,
                    dnsOverHttps = isDoh
                )
            )
        }
    }

    fun setDirectDns(name: String, primary: String, secondary: String) {
        viewModelScope.launch {
            val current = dnsConfig.value
            settingsRepository.setDnsConfig(
                current.copy(
                    directDnsName = name,
                    primaryDns = if (!current.dnsOverHttps) primary else current.primaryDns,
                    secondaryDns = secondary
                )
            )
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

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.setPreferredCore(VpnCore.SINGBOX)
            settingsRepository.setDnsConfig(DnsConfig())
            settingsRepository.setThemeMode(ir.linovpn.app.domain.model.AppThemeMode.DARK)
            settingsRepository.setAppFont(ir.linovpn.app.domain.model.AppFont.VAZIRMATN)
            settingsRepository.setAppLanguage(ir.linovpn.app.domain.model.AppLanguage.FA)
            settingsRepository.setKillSwitchEnabled(false)
            settingsRepository.setAutoConnectEnabled(false)
            settingsRepository.setSplitTunnelingEnabled(false)
        }
    }

    fun setPorts(socks: Int, http: Int) {
        viewModelScope.launch {
            settingsRepository.setSocksPort(socks)
            settingsRepository.setHttpPort(http)
        }
    }
}
