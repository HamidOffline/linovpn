package ir.linovpn.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ir.linovpn.app.ui.screens.home.HomeScreen
import ir.linovpn.app.ui.screens.home.HomeViewModel
import ir.linovpn.app.ui.screens.logs.LogsScreen
import ir.linovpn.app.ui.screens.profiles.ProfilesScreen
import ir.linovpn.app.ui.screens.profiles.ProfilesViewModel
import ir.linovpn.app.ui.screens.servers.AddServerScreen
import ir.linovpn.app.ui.screens.servers.ServersScreen
import ir.linovpn.app.ui.screens.servers.ServersViewModel
import ir.linovpn.app.ui.screens.settings.SettingsScreen
import ir.linovpn.app.ui.screens.settings.SplitTunnelingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    onRequestVpnPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                onRequestVpnPermission = onRequestVpnPermission
            )
        }
        composable(Screen.Servers.route) {
            val serversViewModel: ServersViewModel = hiltViewModel()
            ServersScreen(
                navController = navController,
                viewModel = serversViewModel
            )
        }
        composable(Screen.Profiles.route) {
            val profilesViewModel: ProfilesViewModel = hiltViewModel()
            ProfilesScreen(
                navController = navController,
                viewModel = profilesViewModel
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Logs.route) {
            LogsScreen()
        }
        composable(Screen.AddServer.route) {
            val serversViewModel: ServersViewModel = hiltViewModel()
            AddServerScreen(
                navController = navController,
                serversViewModel = serversViewModel
            )
        }
        composable(Screen.SplitTunneling.route) {
            SplitTunnelingScreen(navController = navController)
        }
    }
}
