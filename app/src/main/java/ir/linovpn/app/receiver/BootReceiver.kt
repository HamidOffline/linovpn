package ir.linovpn.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.repository.SettingsRepository
import ir.linovpn.app.service.VpnManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var serverRepository: ServerRepository
    @Inject lateinit var vpnManager: VpnManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val autoConnect = settingsRepository.isAutoConnectEnabled().first()
                if (autoConnect && vpnManager.isVpnPermissionGranted()) {
                    val server = serverRepository.getSelectedServer().first()
                    if (server != null) {
                        vpnManager.connect(server)
                    }
                }
            }
        }
    }
}
