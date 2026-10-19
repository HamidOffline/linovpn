package ir.linovpn.app.ui.screens.logs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.linovpn.app.service.VpnManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val vpnManager: VpnManager
) : ViewModel() {

    val logs: StateFlow<List<String>> = vpnManager.logs

    fun clearLogs() {
        vpnManager.clearLogs()
    }

    fun getAllLogsText(): String {
        return logs.value.joinToString("\n")
    }
}
