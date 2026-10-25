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
    data object Settings : Screen("settings", ir.linovpn.app.R.string.nav_settings, Icons.Default.Settings)
    data object DnsSettings : Screen("dns_settings", ir.linovpn.app.R.string.dns_settings, Icons.Default.Dns)
    data object RoutingSettings : Screen("routing_settings", ir.linovpn.app.R.string.routing_settings, Icons.Default.Settings)
    data object SubscriptionSettings : Screen("subscription_settings", ir.linovpn.app.R.string.subscription_settings, Icons.Default.Subscriptions)
    data object TunnelSettings : Screen("tunnel_settings", ir.linovpn.app.R.string.tunnel_settings, Icons.Default.Settings)
    data object PingSettings : Screen("ping_settings", ir.linovpn.app.R.string.ping_settings, Icons.Default.Settings)
    data object StatisticsSettings : Screen("statistics_settings", ir.linovpn.app.R.string.statistics_settings, Icons.Default.Settings)
    data object AboutLino : Screen("about_lino", ir.linovpn.app.R.string.about_lino, Icons.Default.Settings)
    
    // Legacy support
    data object Servers : Screen("servers", ir.linovpn.app.R.string.nav_servers, Icons.Default.Dns)
    data object Profiles : Screen("profiles", ir.linovpn.app.R.string.nav_profiles, Icons.Default.Subscriptions)
    data object Logs : Screen("logs", ir.linovpn.app.R.string.nav_logs, Icons.Default.Description)
    data object AddServer : Screen("add_server", ir.linovpn.app.R.string.add_server, Icons.Default.Dns)
    data object SplitTunneling : Screen("split_tunneling", ir.linovpn.app.R.string.split_tunneling, Icons.Default.Settings)
}
