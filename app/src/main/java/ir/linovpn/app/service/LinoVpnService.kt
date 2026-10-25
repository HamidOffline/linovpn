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
    private var connectJob: Job? = null
    private var tunInterface: ParcelFileDescriptor? = null
    private var activeEngine: VpnEngine? = null
    private var currentServer: Server? = null

    override fun onCreate() {
        super.onCreate()
        runCatching { ir.linovpn.xray.XrayEngine.initCore(this) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID)
                connectJob?.cancel()
                connectJob = serviceScope.launch {
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
                connectJob?.cancel()
                connectJob = null
                disconnectVpn()
            }
            NotificationHelper.ACTION_DISCONNECT -> {
                connectJob?.cancel()
                connectJob = null
                disconnectVpn()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun connectVpn(server: Server) {
        // Clean up previous engine, interface, and stats polling if switching or reconnecting
        statsJob?.cancel()
        statsJob = null
        try {
            activeEngine?.stop()
        } catch (e: Throwable) {
            android.util.Log.w("LinoVPN", "Error stopping previous engine during reconnect", e)
        }
        activeEngine = null
        try {
            tunInterface?.close()
        } catch (e: Throwable) {
            android.util.Log.w("LinoVPN", "Error closing previous tunInterface during reconnect", e)
        }
        tunInterface = null
        delay(150)

        currentServer = server
        vpnManager.updateState(VpnState.Connecting)
        android.util.Log.i("LinoVPN", "connectVpn initiated for server: ${server.name} (${server.host}:${server.port})")

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

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    setMetered(false)
                }

                // Exclude LinoVPN itself from the VPN tunnel to prevent outbound routing loops
                runCatching { addDisallowedApplication(packageName) }

                if (splitTunneling && bypassApps.isNotEmpty()) {
                    vpnManager.log("اسپلیت تونلینگ فعال شد (${bypassApps.size} برنامه مستثنی شدند)")
                    for (pkg in bypassApps) {
                        runCatching { addDisallowedApplication(pkg) }
                    }
                }
            }

            tunInterface = builder.establish()
            if (tunInterface == null) {
                val errMsg = "ایجاد رابط TUN با شکست مواجه شد (احتمالاً مجوز VPN صادر نشده است)"
                android.util.Log.e("LinoVPN", errMsg)
                vpnManager.updateState(VpnState.Error(errMsg))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }

            val tunFd = tunInterface!!.fd
            vpnManager.log("رابط تونل TUN ایجاد شد (FD: $tunFd)")
            android.util.Log.i("LinoVPN", "TUN established with FD: $tunFd")

            val tunConfig = TunConfig(fd = tunFd, dnsV4 = dnsConfig.primaryDns)

            val engine = engineSelector.selectEngine(server, preferredCore)
            activeEngine = engine
            vpnManager.log("هسته انتخابی: ${engine.name} | پروتکل: ${server.protocol.name}")
            android.util.Log.i("LinoVPN", "Selected engine: ${engine.name}")

            val configContent = engineSelector.buildConfig(server, engine, tunConfig, dnsConfig.bypassIran)

            val result = engine.start(configContent, tunConfig)
            if (result is EngineResult.Success) {
                android.util.Log.i("LinoVPN", "VPN connected successfully to ${server.name}")
                val connectedState = VpnState.Connected(
                    server = server,
                    connectedAt = System.currentTimeMillis()
                )
                vpnManager.updateState(connectedState)
                startStatsPolling(server)
            } else if (result is EngineResult.Failure) {
                android.util.Log.e("LinoVPN", "Engine start failure: ${result.error}", result.throwable)
                vpnManager.updateState(VpnState.Error(result.error))
                activeEngine?.stop()
                activeEngine = null
                runCatching { tunInterface?.close() }
                tunInterface = null
                currentServer = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        } catch (e: Throwable) {
            android.util.Log.e("LinoVPN", "connectVpn exception: ${e.message}", e)
            vpnManager.updateState(VpnState.Error(e.message ?: "VPN establishment failed"))
            activeEngine?.stop()
            activeEngine = null
            runCatching { tunInterface?.close() }
            tunInterface = null
            currentServer = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
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
