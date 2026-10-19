package ir.linovpn.app.ui.screens.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.ServerProtocol
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.usecase.ParseSubscriptionUseCase
import ir.linovpn.app.domain.usecase.TestLatencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServersUiState(
    val servers: List<Server> = emptyList(),
    val filteredServers: List<Server> = emptyList(),
    val searchQuery: String = "",
    val selectedProtocol: ServerProtocol? = null,
    val isPingingAll: Boolean = false,
    val pingingServerIds: Set<String> = emptySet()
)

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val testLatencyUseCase: TestLatencyUseCase,
    private val parseSubscriptionUseCase: ParseSubscriptionUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedProtocol = MutableStateFlow<ServerProtocol?>(null)
    private val isPingingAll = MutableStateFlow(false)
    private val pingingServerIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<ServersUiState> = combine(
        serverRepository.getAllServers(),
        searchQuery,
        selectedProtocol,
        isPingingAll,
        pingingServerIds
    ) { servers, query, proto, pingingAll, pingingIds ->
        val filtered = servers.filter { server ->
            val matchesQuery = query.isBlank() ||
                server.name.contains(query, ignoreCase = true) ||
                server.host.contains(query, ignoreCase = true)
            val matchesProto = proto == null || server.protocol == proto
            matchesQuery && matchesProto
        }
        ServersUiState(
            servers = servers,
            filteredServers = filtered,
            searchQuery = query,
            selectedProtocol = proto,
            isPingingAll = pingingAll,
            pingingServerIds = pingingIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServersUiState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onProtocolSelected(proto: ServerProtocol?) {
        selectedProtocol.value = if (selectedProtocol.value == proto) null else proto
    }

    fun selectServer(server: Server) {
        viewModelScope.launch {
            serverRepository.setSelectedServer(server.id)
        }
    }

    fun deleteServer(server: Server) {
        viewModelScope.launch {
            serverRepository.deleteServer(server.id)
        }
    }

    fun deleteAllServers() {
        viewModelScope.launch {
            serverRepository.deleteAllServers()
        }
    }

    fun toggleFavorite(server: Server) {
        viewModelScope.launch {
            serverRepository.toggleFavorite(server.id)
        }
    }

    fun pingServer(server: Server) {
        viewModelScope.launch {
            pingingServerIds.value = pingingServerIds.value + server.id
            testLatencyUseCase.ping(server)
            pingingServerIds.value = pingingServerIds.value - server.id
        }
    }

    fun pingAll() {
        viewModelScope.launch {
            isPingingAll.value = true
            val list = uiState.value.filteredServers
            testLatencyUseCase.pingAll(list)
            isPingingAll.value = false
        }
    }

    fun importFromText(text: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val servers = parseSubscriptionUseCase.parseSubscriptionContent(text)
            if (servers.isNotEmpty()) {
                serverRepository.insertServers(servers)
                onResult(servers.size)
            } else {
                onResult(0)
            }
        }
    }
}
