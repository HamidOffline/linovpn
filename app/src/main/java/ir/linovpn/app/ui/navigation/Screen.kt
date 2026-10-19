package ir.linovpn.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", ir.linovpn.app.R.string.nav_home, Icons.Default.Home)
    data object Servers : Screen("servers", ir.linovpn.app.R.string.nav_servers, Icons.Default.Dns)
    data object Profiles : Screen("profiles", ir.linovpn.app.R.string.nav_profiles, Icons.Default.Subscriptions)
    data object Settings : Screen("settings", ir.linovpn.app.R.string.nav_settings, Icons.Default.Settings)
    data object Logs : Screen("logs", ir.linovpn.app.R.string.nav_logs, Icons.Default.Description)
    data object AddServer : Screen("add_server", ir.linovpn.app.R.string.add_server, Icons.Default.Dns)
    data object SplitTunneling : Screen("split_tunneling", ir.linovpn.app.R.string.split_tunneling, Icons.Default.Settings)
}
