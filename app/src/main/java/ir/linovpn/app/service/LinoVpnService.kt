package ir.linovpn.app.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import dagger.hilt.android.AndroidEntryPoint
import ir.linovpn.app.R
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnCore
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.repository.SettingsRepository
import ir.linovpn.common.EngineResult
import ir.linovpn.common.TunConfig
import ir.linovpn.common.VpnEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LinoVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "ir.linovpn.app.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "ir.linovpn.app.ACTION_DISCONNECT"
        const val EXTRA_SERVER_ID = "extra_server_id"
    }

    @Inject lateinit var vpnManager: VpnManager
    @Inject lateinit var engineSelector: EngineSelector
    @Inject lateinit var serverRepository: ServerRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var statsJob: Job? = null
    private var tunInterface: ParcelFileDescriptor? = null
    private var activeEngine: VpnEngine? = null
    private var currentServer: Server? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                serviceScope.launch {
                    val server = if (serverId != null) {
                        serverRepository.getServerById(serverId)
                    } else {
                        serverRepository.getSelectedServer().first()
                    }
                    if (server != null) {
                        connectVpn(server)
                    } else {
                        vpnManager.updateState(VpnState.Error("No server selected"))
                        stopSelf()
                    }
                }
            }
            ACTION_DISCONNECT -> {
                disconnectVpn()
            }
            NotificationHelper.ACTION_DISCONNECT -> {
                disconnectVpn()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun connectVpn(server: Server) {
        currentServer = server
        vpnManager.updateState(VpnState.Connecting)

        val initialNotif = notificationHelper.buildNotification(
            server,
            getString(R.string.vpn_connecting)
        )
        startForeground(NotificationHelper.NOTIFICATION_ID, initialNotif)

        try {
            val preferredCore = settingsRepository.getPreferredCore().first()
            val dnsConfig = settingsRepository.getDnsConfig().first()
            val splitTunneling = settingsRepository.isSplitTunnelingEnabled().first()
            val bypassApps = settingsRepository.getBypassApps().first()

            val builder = Builder().apply {
                setSession(server.name)
                setMtu(1500)
                addAddress("172.19.0.1", 30)
                addRoute("0.0.0.0", 0)
                addDnsServer(dnsConfig.primaryDns)
                addDnsServer(dnsConfig.secondaryDns)

                if (splitTunneling && bypassApps.isNotEmpty()) {
                    vpnManager.log("اسپلیت تونلینگ فعال شد (${bypassApps.size} برنامه مستثنی شدند)")
                    for (pkg in bypassApps) {
                        runCatching { addDisallowedApplication(pkg) }
                    }
                }
            }

            tunInterface = builder.establish()
            val tunFd = tunInterface?.fd ?: -1
            vpnManager.log("رابط تونل TUN ایجاد شد (FD: $tunFd)")

            val tunConfig = if (tunFd > 0) {
                TunConfig(fd = tunFd, dnsV4 = dnsConfig.primaryDns)
            } else null

            val engine = engineSelector.selectEngine(server, preferredCore)
            activeEngine = engine
            vpnManager.log("هسته انتخابی: ${engine.name} | پروتکل: ${server.protocol.name}")

            val configContent = engineSelector.buildConfig(server, engine, tunConfig, dnsConfig.bypassIran)

            val result = engine.start(configContent, tunConfig)
            if (result is EngineResult.Success) {
                val connectedState = VpnState.Connected(
                    server = server,
                    connectedAt = System.currentTimeMillis()
                )
                vpnManager.updateState(connectedState)
                startStatsPolling(server)
            } else if (result is EngineResult.Failure) {
                vpnManager.updateState(VpnState.Error(result.error))
                disconnectVpn()
            }
        } catch (e: Throwable) {
            vpnManager.updateState(VpnState.Error(e.message ?: "VPN establishment failed"))
            disconnectVpn()
        }
    }

    private fun startStatsPolling(server: Server) {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                val stats = activeEngine?.getStats()
                if (stats != null) {
                    val duration = System.currentTimeMillis() - startTime
                    val updated = VpnState.Connected(
                        server = server,
                        connectedAt = startTime,
                        uploadBytes = stats.uploadBytes,
                        downloadBytes = stats.downloadBytes,
                        uploadSpeed = stats.uploadSpeed,
                        downloadSpeed = stats.downloadSpeed
                    )
                    vpnManager.updateState(updated)

                    val notif = notificationHelper.buildNotification(
                        server,
                        getString(R.string.vpn_connected),
                        uploadSpeed = stats.uploadSpeed,
                        downloadSpeed = stats.downloadSpeed
                    )
                    val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                    manager.notify(NotificationHelper.NOTIFICATION_ID, notif)
                }
            }
        }
    }

    private fun disconnectVpn() {
        vpnManager.updateState(VpnState.Disconnecting)
        statsJob?.cancel()
        statsJob = null

        serviceScope.launch {
            activeEngine?.stop()
            activeEngine = null

            runCatching { tunInterface?.close() }
            tunInterface = null
            currentServer = null

            vpnManager.updateState(VpnState.Disconnected)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        activeEngine = null
        tunInterface?.close()
    }

    override fun onRevoke() {
        disconnectVpn()
        super.onRevoke()
    }
}
