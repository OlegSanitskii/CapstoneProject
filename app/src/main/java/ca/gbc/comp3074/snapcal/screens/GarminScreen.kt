package ca.gbc.comp3074.snapcal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.navigation.Screen
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun GarminScreen(nav: NavHostController? = null) {
    val authVM: AuthViewModel = viewModel()
    val email = authVM.userEmail.collectAsState().value

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Garmin Integration") }

        // Подключение к Garmin (плейсхолдер переключателя)
        item {
            CardBlock {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Connect to Garmin", fontWeight = FontWeight.Bold)
                        Text("Sync workouts, steps, heart rate", color = Color.Gray)
                    }
                    var enabled by remember { mutableStateOf(true) }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        }

        // Блок аккаунта + выход из сессии
        item {
            CardBlock(title = "Account") {
                if (email.isNotBlank()) {
                    Text("Signed in as: $email", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                } else {
                    Text("Guest session", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        authVM.signOut {
                            // если есть NavController — возвращаемся на Login и чистим back stack
                            nav?.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }

        // Недавние тренировки (заглушка)
        item {
            CardBlock(title = "Recent Workouts") {
                val items = listOf(
                    "Sep 26 • Cycling" to "45 min • 520 kcal",
                    "Sep 25 • Running" to "30 min • 340 kcal",
                    "Sep 23 • Strength" to "40 min • 280 kcal"
                )
                items.forEach { (title, meta) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(meta, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Пульс (заглушка)
        item {
            CardBlock(title = "Heart Rate Summary") {
                Text("Resting HR: 58 bpm")
                Text("Average HR (workouts): 138 bpm")
                Text("Max HR (last workout): 172 bpm")
            }
        }

        // Калории за неделю (заглушка)
        item {
            CardBlock(title = "Calories Burned (Weekly)") {
                val days = listOf("M","T","W","T","F","S","S")
                val vals = listOf(520,340,280,600,450,700,300)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.indices.forEach { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .width(16.dp)
                                    .height((vals[i] / 10).dp)
                                    .background(Color(0xFF3B82F6))
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(days[i], color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
