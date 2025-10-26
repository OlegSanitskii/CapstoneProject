package ca.gbc.comp3074.snapcal.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ca.gbc.comp3074.snapcal.navigation.Screen

@Composable
fun SnapCalBottomBar(nav: NavHostController) {
    val items = listOf(Screen.Dashboard, Screen.Manual, Screen.Progress, Screen.Garmin)
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    nav.navigate(screen.route) {
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { screen.icon?.invoke() },
                label = { Text(screen.label) }
            )
        }
    }
}
