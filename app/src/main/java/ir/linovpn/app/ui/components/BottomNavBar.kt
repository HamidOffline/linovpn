package ir.linovpn.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ir.linovpn.app.ui.navigation.Screen
import ir.linovpn.app.ui.theme.LinoDarkNavy
import ir.linovpn.app.ui.theme.LinoPrimaryCyan
import ir.linovpn.app.ui.theme.LinoSurface
import ir.linovpn.app.ui.theme.LinoTextMuted
import ir.linovpn.app.ui.theme.LinoTextPrimary

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Settings
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = stringResource(screen.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.titleResId),
                        color = if (selected) LinoPrimaryCyan else LinoTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = LinoPrimaryCyan,
                    indicatorColor = LinoPrimaryCyan,
                    unselectedIconColor = LinoTextMuted,
                    unselectedTextColor = LinoTextMuted
                )
            )
        }
    }
}
