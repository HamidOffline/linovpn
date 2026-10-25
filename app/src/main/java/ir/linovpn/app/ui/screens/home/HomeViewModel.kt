package ir.linovpn.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.domain.repository.ProfileRepository
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.usecase.ParseSubscriptionUseCase
import ir.linovpn.app.domain.usecase.TestLatencyUseCase
import ir.linovpn.app.service.VpnManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val selectedServer: Server? = null,
    val allServers: List<Server> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val isVpnPermissionGranted: Boolean = false,
    val isPinging: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vpnManager: VpnManager,
    private val serverRepository: ServerRepository,
    private val profileRepository: ProfileRepository,
    private val parseSubscriptionUseCase: ParseSubscriptionUseCase,
    private val testLatencyUseCase: TestLatencyUseCase
) : ViewModel() {

    private val isPinging = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
            val servers = serverRepository.getAllServers().firstOrNull() ?: emptyList()
            // Check if any server has corrupted host with control chars or non-ascii
            val hasCorrupted = servers.any { srv -> srv.host.any { it.code < 32 || it.code > 126 } }
            if ((hasCorrupted || servers.size <= 2) && profiles.isNotEmpty()) {
                profiles.forEach { profile ->
                    runCatching {
                        profileRepository.fetchAndSaveSubscription(profile)
                    }
                }
            }
        }
    }

    fun updateServer(server: Server) {
        viewModelScope.launch {
            serverRepository.updateServer(server)
        }
    }

    fun pingServer(server: Server) {
        viewModelScope.launch {
            testLatencyUseCase.ping(server)
        }
    }

    fun importSubscription(url: String, onResult: (Boolean, String) -> Unit) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) {
            onResult(false, "آدرس ساب‌اسکریپشن نمی‌تواند خالی باشد")
            return
        }
        viewModelScope.launch {
            try {
                val profile = Profile(
                    id = UUID.randomUUID().toString(),
                    name = "subscription",
                    subscriptionUrl = cleanUrl,
                    autoUpdate = true,
                    lastUpdated = System.currentTimeMillis()
                )
                profileRepository.insertProfile(profile)
                val result = profileRepository.fetchAndSaveSubscription(profile)
                result.onSuccess { count ->
                    onResult(true, "ساب‌اسکریپشن با موفقیت بارگذاری شد ($count سرور اضافه شد)")
                }.onFailure { err ->
                    onResult(false, "خطا در اتصال به ساب‌اسکریپشن: ${err.message}")
                }
            } catch (e: Exception) {
                onResult(false, "خطا: ${e.message}")
            }
        }
    }

    fun importFromClipboard(text: String, onResult: (Boolean, String) -> Unit) {
        val clean = text.trim()
        if (clean.isBlank()) {
            onResult(false, "کلیپ‌بورد خالی است")
            return
        }
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            importSubscription(clean, onResult)
        } else {
            viewModelScope.launch {
                val servers = parseSubscriptionUseCase.parseSubscriptionContent(clean)
                if (servers.isNotEmpty()) {
                    serverRepository.insertServers(servers)
                    serverRepository.setSelectedServer(servers.first().id)
                    onResult(true, "${servers.size} سرور از کلیپ‌بورد اضافه شد")
                } else {
                    onResult(false, "سرور معتبری در کلیپ‌بورد یافت نشد")
                }
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        vpnManager.vpnState,
        serverRepository.getSelectedServer(),
        serverRepository.getAllServers(),
        profileRepository.getAllProfiles(),
        isPinging
    ) { vpnState, selectedServer, allServers, profiles, pinging ->
        HomeUiState(
            vpnState = vpnState,
            selectedServer = selectedServer,
            allServers = allServers,
            profiles = profiles,
            isVpnPermissionGranted = vpnManager.isVpnPermissionGranted(),
            isPinging = pinging
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
            val remaining = serverRepository.getAllServers().firstOrNull() ?: emptyList()
            val currentSelected = serverRepository.getSelectedServer().firstOrNull()
            if (currentSelected == null && remaining.isNotEmpty()) {
                serverRepository.setSelectedServer(remaining.first().id)
            }
        }
    }

    fun selectServer(server: Server) {
        viewModelScope.launch {
            val currentState = uiState.value.vpnState
            val currentSelected = uiState.value.selectedServer
            serverRepository.setSelectedServer(server.id)

            if (currentState is VpnState.Connected) {
                if (currentState.server.id != server.id) {
                    vpnManager.connect(server)
                }
            } else if (currentState is VpnState.Connecting) {
                if (currentSelected?.id != server.id) {
                    vpnManager.connect(server)
                }
            }
        }
    }

    fun toggleConnection() {
        val current = uiState.value.vpnState
        when (current) {
            is VpnState.Connected, is VpnState.Connecting -> {
                vpnManager.disconnect()
            }
            is VpnState.Disconnected, is VpnState.Error -> {
                val server = uiState.value.selectedServer ?: uiState.value.allServers.firstOrNull()
                if (server != null) {
                    selectServer(server)
                    vpnManager.connect(server)
                }
            }
            else -> Unit
        }
    }

    fun editSubscription(profileId: String, newName: String, newUrl: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfileById(profileId)
                if (profile != null) {
                    val urlChanged = profile.subscriptionUrl.trim() != newUrl.trim()
                    val updated = profile.copy(
                        name = newName.trim().ifBlank { "subscription" },
                        subscriptionUrl = newUrl.trim()
                    )
                    profileRepository.updateProfile(updated)
                    if (urlChanged && newUrl.isNotBlank()) {
                        val fetchResult = profileRepository.fetchAndSaveSubscription(updated)
                        fetchResult.onSuccess { count ->
                            onResult(true, "اشتراک با موفقیت ویرایش و بروزرسانی شد ($count سرور دریافت شد)")
                        }.onFailure { err ->
                            onResult(true, "نام و لینک ذخیره شد اما دریافت سرورها با خطا مواجه شد: ${err.message}")
                        }
                    } else {
                        onResult(true, "اطلاعات اشتراک با موفقیت ذخیره شد")
                    }
                } else {
                    onResult(false, "اشتراک مورد نظر یافت نشد")
                }
            } catch (e: Exception) {
                onResult(false, "خطا: ${e.message}")
            }
        }
    }


    enum class SortMode { PING, NAME }
    private val _sortMode = MutableStateFlow(SortMode.PING)
    val sortMode: StateFlow<SortMode> = _sortMode

    fun toggleSortMode() {
        _sortMode.value = if (_sortMode.value == SortMode.PING) SortMode.NAME else SortMode.PING
    }

    fun pingSelectedServer() {
        val server = uiState.value.selectedServer ?: return
        viewModelScope.launch {
            isPinging.value = true
            testLatencyUseCase.ping(server)
            isPinging.value = false
        }
    }

    fun pingAllServers() {
        val servers = uiState.value.allServers
        if (servers.isEmpty()) return
        viewModelScope.launch {
            isPinging.value = true
            testLatencyUseCase.pingAll(servers)
            isPinging.value = false
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            serverRepository.deleteServer(serverId)
            val remaining = serverRepository.getAllServers().firstOrNull() ?: emptyList()
            val currentSelected = serverRepository.getSelectedServer().firstOrNull()
            if (currentSelected == null && remaining.isNotEmpty()) {
                serverRepository.setSelectedServer(remaining.first().id)
            }
        }
    }

    fun updateSubscription(profileId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfileById(profileId)
                if (profile != null) {
                    val result = profileRepository.fetchAndSaveSubscription(profile)
                    result.onSuccess { count ->
                        onResult(true, "بروزرسانی انجام شد ($count سرور دریافت شد)")
                    }.onFailure { err ->
                        onResult(false, "خطا در بروزرسانی: ${err.message}")
                    }
                } else {
                    onResult(false, "پروفایل یافت نشد")
                }
            } catch (e: Exception) {
                onResult(false, "خطا: ${e.message}")
            }
        }
    }

    fun isPermissionGranted(): Boolean = vpnManager.isVpnPermissionGranted()
}
