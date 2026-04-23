package ca.gbc.comp3074.snapcal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.navigation.Screen
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel

@Composable
fun SignUpScreen(
    nav: NavHostController? = null
) {
    val vm: AuthViewModel = viewModel()

    var name  by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pass  by rememberSaveable { mutableStateOf("") }
    var pass2 by rememberSaveable { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (!email.contains("@")) return "Invalid email"
        if (pass.length < 4) return "Password must be at least 4 chars"
        if (pass != pass2) return "Passwords do not match"
        return null
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create account", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

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
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = pass2,
                    onValueChange = { pass2 = it },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (loading) return@Button
                        val msg = validate()
                        if (msg != null) {
                            error = msg
                            return@Button
                        }
                        loading = true; error = null
                        vm.signUp(email.trim(), pass, name.ifBlank { null }, remember = true) { e ->
                            loading = false
                            if (e != null) {
                                error = e.message ?: "Registration failed"
                            } else {
                                nav?.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp),
                    enabled = !loading
                ) {
                    Text(if (loading) "Creating…" else "Create account")
                }

                TextButton(
                    onClick = { nav?.navigateUp() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to login", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
