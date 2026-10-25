package ir.linovpn.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import ir.linovpn.app.ui.screens.settings.*

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
        // Main Tabs
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                onRequestVpnPermission = onRequestVpnPermission
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // ================= STREISAND SUB-SCREENS WITH iOS PUSH/POP ANIMATION =================
        composable(
            route = Screen.DnsSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            DnsSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.RoutingSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            RoutingSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.SubscriptionSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            SubscriptionSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.TunnelSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            TunnelSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.PingSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            PingSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.StatisticsSettings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            StatisticsSettingsScreen(navController = navController)
        }

        composable(
            route = Screen.AboutLino.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350, easing = FastOutSlowInEasing)) + fadeOut(tween(350))
            }
        ) {
            AboutLinoScreen(navController = navController)
        }

        // Legacy routes
        composable(Screen.Servers.route) {
            val serversViewModel: ServersViewModel = hiltViewModel()
            ServersScreen(navController = navController, viewModel = serversViewModel)
        }
        composable(Screen.Profiles.route) {
            val profilesViewModel: ProfilesViewModel = hiltViewModel()
            ProfilesScreen(navController = navController, viewModel = profilesViewModel)
        }
        composable(Screen.Logs.route) {
            LogsScreen()
        }
        composable(Screen.AddServer.route) {
            val serversViewModel: ServersViewModel = hiltViewModel()
            AddServerScreen(navController = navController, serversViewModel = serversViewModel)
        }
        composable(Screen.SplitTunneling.route) {
            SplitTunnelingScreen(navController = navController)
        }
    }
}
