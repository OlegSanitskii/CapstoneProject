@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ca.gbc.comp3074.snapcal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.gbc.comp3074.snapcal.navigation.Screen
import ca.gbc.comp3074.snapcal.screens.*
import ca.gbc.comp3074.snapcal.ui.components.SnapCalBottomBar
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel

@Composable
fun SnapCalApp() {
    val nav = rememberNavController()
    val authVM: AuthViewModel = viewModel()
    val loggedIn by authVM.isLoggedIn.collectAsState()


    LaunchedEffect(loggedIn) {
        val current = nav.currentBackStackEntry?.destination?.route
        if (!loggedIn && current != Screen.Login.route && current != "splash") {
            nav.navigate(Screen.Login.route) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            bottomBar = {
                val current = nav.currentBackStackEntryAsState().value?.destination?.route
                if (current != "splash" && current != Screen.Login.route && current != Screen.SignUp.route) {
                    SnapCalBottomBar(nav)
                }
            }
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = "splash",
                modifier = Modifier.padding(inner)
            ) {
                // --- Splash ---
                composable("splash") {
                    SplashScreen(
                        onFinished = {
                            if (loggedIn) {
                                nav.navigate(Screen.Dashboard.route) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                nav.navigate(Screen.Login.route) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                // --- Auth ---
                composable(Screen.Login.route) {
                    LoginScreen(
                        nav = nav,
                        onSignUp = { nav.navigate(Screen.SignUp.route) }
                    )
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(nav)
                }

                // --- Main ---
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onScan = { nav.navigate(Screen.Scan.route) },
                        onManual = { nav.navigate(Screen.Manual.route) },
                        onProgress = { nav.navigate(Screen.Progress.route) },
                        onSignOut = {
                            nav.navigate(Screen.Login.route) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Scan.route)     { ScanScreen(onSaved = { nav.navigateUp() }) }
                composable(Screen.Manual.route)   { ManualMealScreen(onSaved = { nav.navigateUp() }) }
                composable(Screen.Progress.route) { ProgressScreen() }
                composable(Screen.Garmin.route)   { GarminScreen(nav) }
            }
        }
    }
}
