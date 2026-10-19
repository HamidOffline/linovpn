package ir.linovpn.app.service

import android.content.Context
import android.content.Intent
import android.net.VpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VpnManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("سیستم LinoVPN آماده به کار است"))
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun log(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _logs.value = (_logs.value + "[$time] $message").takeLast(500)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun updateState(state: VpnState) {
        _vpnState.value = state
        when (state) {
            is VpnState.Connecting -> log("در حال برقراری اتصال...")
            is VpnState.Connected -> log("اتصال برقرار شد (${state.server.name})")
            is VpnState.Disconnecting -> log("در حال قطع اتصال...")
            is VpnState.Disconnected -> log("اتصال قطع شد")
            is VpnState.Error -> log("خطا: ${state.message}")
        }
    }

    fun isVpnPermissionGranted(): Boolean {
        return VpnService.prepare(context) == null
    }

    fun getVpnPermissionIntent(): Intent? {
        return VpnService.prepare(context)
    }

    fun connect(server: Server) {
        _vpnState.value = VpnState.Connecting
        val intent = Intent(context, LinoVpnService::class.java).apply {
            action = LinoVpnService.ACTION_CONNECT
            putExtra(LinoVpnService.EXTRA_SERVER_ID, server.id)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun disconnect() {
        _vpnState.value = VpnState.Disconnecting
        val intent = Intent(context, LinoVpnService::class.java).apply {
            action = LinoVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}
