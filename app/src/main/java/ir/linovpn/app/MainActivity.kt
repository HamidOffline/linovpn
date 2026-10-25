package ir.linovpn.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.repository.SettingsRepository
import ir.linovpn.app.service.VpnManager
import ir.linovpn.app.ui.components.BottomNavBar
import ir.linovpn.app.ui.navigation.AppNavHost
import ir.linovpn.app.ui.theme.LinoDarkNavy
import ir.linovpn.app.ui.theme.LinoVpnTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var vpnManager: VpnManager
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var serverRepository: ServerRepository

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "مجوز اتصال صادر شد", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                val selected = serverRepository.getSelectedServer().firstOrNull()
                val server: Server? = selected ?: serverRepository.getAllServers().firstOrNull()?.firstOrNull()
                if (server != null) {
                    vpnManager.connect(server)
                }
            }
        } else {
            Toast.makeText(this, "مجوز اتصال برای کارکرد VPN الزامی است", Toast.LENGTH_LONG).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onResume() {
        super.onResume()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "ir.linovpn.app.ACTION_CONNECT_TOGGLE") {
            intent.action = null
            lifecycleScope.launch {
                val selected = serverRepository.getSelectedServer().firstOrNull()
                val server: Server? = selected ?: serverRepository.getAllServers().firstOrNull()?.firstOrNull()
                if (server != null) {
                    if (vpnManager.vpnState.value is VpnState.Connected) {
                        vpnManager.disconnect()
                    } else {
                        vpnManager.connect(server)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsState(initial = ir.linovpn.app.domain.model.AppThemeMode.DARK)
            val fontMode by settingsRepository.getAppFont().collectAsState(initial = ir.linovpn.app.domain.model.AppFont.VAZIRMATN)
            val appLanguage by settingsRepository.getAppLanguage().collectAsState(initial = ir.linovpn.app.domain.model.AppLanguage.FA)

            LinoVpnTheme(themeMode = themeMode, fontMode = fontMode) {
                ir.linovpn.app.ui.theme.ProvideAppLocale(appLanguage = appLanguage) {
                    val navController = rememberNavController()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                        bottomBar = {
                            BottomNavBar(navController = navController)
                        }
                    ) { innerPadding ->
                        AppNavHost(
                            navController = navController,
                            onRequestVpnPermission = { requestVpnPermission() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
