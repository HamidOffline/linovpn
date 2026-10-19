package ir.linovpn.app.ui.screens.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.linovpn.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean = false
)

@HiltViewModel
class SplitTunnelingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isSplitTunneling: StateFlow<Boolean> = settingsRepository.isSplitTunnelingEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bypassApps: StateFlow<Set<String>> = settingsRepository.getBypassApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSplitTunneling(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSplitTunnelingEnabled(enabled)
        }
    }

    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            val current = bypassApps.value.toMutableSet()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            settingsRepository.setBypassApps(current)
        }
    }

    fun selectAll(visiblePackages: List<String>) {
        viewModelScope.launch {
            val current = bypassApps.value.toMutableSet()
            current.addAll(visiblePackages)
            settingsRepository.setBypassApps(current)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            settingsRepository.setBypassApps(emptySet())
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                list.filter { it.packageName != context.packageName }
                    .map { appInfo ->
                        val isSys = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val name = pm.getApplicationLabel(appInfo).toString()
                        InstalledAppItem(
                            packageName = appInfo.packageName,
                            appName = name,
                            isSystem = isSys
                        )
                    }
                    .sortedWith(compareBy({ it.isSystem }, { it.appName.lowercase() }))
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }
}
