package ir.linovpn.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.usecase.TestLatencyUseCase
import ir.linovpn.app.service.VpnManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val testLatencyUseCase: TestLatencyUseCase
) : ViewModel() {

    private val isPinging = MutableStateFlow(false)

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
