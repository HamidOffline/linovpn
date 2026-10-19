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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val selectedServer: Server? = null,
    val allServers: List<Server> = emptyList(),
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
                    name = "Linovpn VIP",
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
        isPinging
    ) { vpnState, selectedServer, allServers, pinging ->
        HomeUiState(
            vpnState = vpnState,
            selectedServer = selectedServer,
            allServers = allServers,
            isVpnPermissionGranted = vpnManager.isVpnPermissionGranted(),
            isPinging = pinging
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectServer(server: Server) {
        viewModelScope.launch {
            serverRepository.setSelectedServer(server.id)
        }
    }

    fun toggleConnection() {
        val current = uiState.value.vpnState
        when (current) {
            is VpnState.Connected, is VpnState.Connecting -> {
                vpnManager.disconnect()
            }
            is VpnState.Disconnected, is VpnState.Error -> {
                val server = uiState.value.selectedServer
                if (server != null) {
                    vpnManager.connect(server)
                }
            }
            else -> Unit
        }
    }

    fun pingSelectedServer() {
        val server = uiState.value.selectedServer ?: return
        viewModelScope.launch {
            isPinging.value = true
            testLatencyUseCase.ping(server)
            isPinging.value = false
        }
    }

    fun isPermissionGranted(): Boolean = vpnManager.isVpnPermissionGranted()
}
