package ca.gbc.comp3074.snapcal.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

sealed class Screen(val route: String, val label: String, val icon: @Composable (() -> Unit)?) {
    data object Login      : Screen("login",      "Login",      null)
    data object SignUp : Screen("signup", "Sign Up", null)
    data object Dashboard  : Screen("dashboard",  "Home",       { Icon(Icons.Default.Home, null) })
    data object Scan       : Screen("scan",       "Scan",       { Icon(Icons.Default.CameraAlt, null) })
    data object Manual     : Screen("manual",     "Log",        { Icon(Icons.Default.Edit, null) })
    data object Progress   : Screen("progress",   "Progress",   { Icon(Icons.Default.Timeline, null) })
    data object Garmin     : Screen("garmin",     "Settings",   { Icon(Icons.Default.Settings, null) })
}
