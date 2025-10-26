package ca.gbc.comp3074.snapcal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.navigation.Screen
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel

@Composable
fun LoginScreen(
    nav: NavHostController? = null,
    onSignUp: (() -> Unit)? = null
) {
    val vm: AuthViewModel = viewModel()

    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SnapCal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text("Log in to continue", color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                // --- Email / Password ---
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                // --- Remember Me ---
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Remember me")
                }

                // --- Error message ---
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- Login Button ---
                Button(
                    onClick = {
                        if (!loading) {
                            loading = true
                            error = null
                            vm.signIn(email.trim(), pass, rememberMe) { e ->
                                loading = false
                                if (e != null) {
                                    error = e.message ?: "Login failed"
                                } else {
                                    nav?.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp),
                    enabled = !loading
                ) {
                    Text(if (loading) "Signing in…" else "Login")
                }

                // --- Continue as Guest ---
                TextButton(
                    onClick = {
                        if (!loading) {
                            loading = true
                            vm.continueAsGuest {
                                loading = false
                                nav?.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue as guest")
                }

                // --- Create Account ---
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (onSignUp != null) {
                            onSignUp()
                        } else if (nav != null) {
                            nav.navigate(Screen.SignUp.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create an account")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
