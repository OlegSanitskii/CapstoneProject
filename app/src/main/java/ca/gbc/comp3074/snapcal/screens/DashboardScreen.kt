package ca.gbc.comp3074.snapcal.screens

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel
import ca.gbc.comp3074.snapcal.ui.state.MealsViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onScan: () -> Unit,
    onManual: () -> Unit,
    onProgress: () -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Auth / email ---
    val authVM: AuthViewModel = viewModel()
    val email by authVM.userEmail.collectAsState()

    // --- Meals / today calories ---
    val mealsVM: MealsViewModel = viewModel()

    val totalTodayKcal by remember(mealsVM) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (from, to) = mealsVM.todayBounds()
            mealsVM.observeTotalCalories(from, to)
        } else {
            flowOf(0)
        }
    }.collectAsState(initial = 0)

    val kcalGoal = 2200
    val burned = 700          // пока демо-значение
    val consumed = totalTodayKcal
    val remaining = (kcalGoal - consumed).coerceAtLeast(0)
    val balance = consumed - burned

    val progress = if (kcalGoal > 0) {
        consumed.coerceAtMost(kcalGoal).toFloat() / kcalGoal
    } else 0f
    val sweepAngle = 360f * progress

    // --- Health Connect / steps (today + weekly) ---
    var hcAvailable by remember { mutableStateOf<Boolean?>(null) }
    var hcHasPermissions by remember { mutableStateOf(false) }
    var todaySteps by remember { mutableStateOf<Long?>(null) }
    var weeklySteps by remember { mutableStateOf<List<Long>?>(null) }
    var hcError by remember { mutableStateOf<String?>(null) }

    val client = remember { HealthConnectManager.getClientOrNull(context) }

    LaunchedEffect(client) {
        hcAvailable = client != null
        if (client != null) {
            try {
                hcHasPermissions = HealthConnectManager.hasAllPermissions(client)
                if (hcHasPermissions) {
                    todaySteps = HealthConnectManager.readTodaySteps(client)
                    weeklySteps = HealthConnectManager.readWeeklySteps(client)
                }
            } catch (e: Exception) {
                hcError = e.localizedMessage ?: "Failed to read Health Connect data"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Dashboard") }

        // --- Calories card ---
        item {
            CardBlock(title = "Calories (in vs out)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = 14.dp.toPx()
                        drawArc(
                            color = Color(0xFFE5E5E5),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = Color(0xFF4A90E2),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("In:  $consumed kcal")
                        Text("Out: $burned kcal")

                        val balanceLabel =
                            (if (balance > 0) "+" else "") + "$balance kcal"
                        Text(
                            "Balance: $balanceLabel",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF4A90E2), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Consumed (goal $kcalGoal)",
                                color = Color.Gray
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFE5E5E5), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Remaining: $remaining kcal",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // --- Steps today + weekly ---
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CardBlock(modifier = Modifier.weight(1f), title = "Steps") {
                    when {
                        hcAvailable == false -> {
                            Text(
                                "Health Connect not available on this device.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        hcHasPermissions && todaySteps != null -> {
                            Text(
                                "${todaySteps}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Steps today (from Health Connect)",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        hcHasPermissions && todaySteps == null -> {
                            Text(
                                "Loading steps...",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        hcAvailable == true && !hcHasPermissions -> {
                            Text(
                                "Connect Health Connect in Settings to see real steps.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Sample: 6,842",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        else -> {
                            Text(
                                "Steps data not available.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (hcError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = hcError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                CardBlock(modifier = Modifier.weight(1f), title = "Workouts") {
                    Text("Cycling 45 min", fontWeight = FontWeight.SemiBold)
                    Text("Burned: 520 kcal", color = Color.Gray)
                }
            }
        }

        // --- Weekly steps chart ---
        item {
            CardBlock(title = "Steps (weekly)") {
                if (hcHasPermissions && weeklySteps != null) {
                    val labels = HealthConnectManager.weekDayLabels()
                    val values = weeklySteps!!
                    val maxVal = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)

                    Column {
                        Text(
                            "Current week",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            values.forEachIndexed { index, v ->
                                val fraction =
                                    (v.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .fillMaxHeight(fraction)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        labels[index],
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Connect Health Connect to see weekly steps.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // --- Quick actions ---
        item {
            CardBlock(title = "Quick Actions") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onScan, modifier = Modifier.weight(1f)) {
                        Text("Scan Label")
                    }
                    OutlinedButton(onManual, modifier = Modifier.weight(1f)) {
                        Text("Log Meal")
                    }
                    OutlinedButton(onProgress, modifier = Modifier.weight(1f)) {
                        Text("Progress")
                    }
                }
            }
        }

        // --- Account ---
        item {
            CardBlock(title = "Account") {
                if (email.isNotBlank()) {
                    Text("Signed in as: $email", color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                }
                Button(
                    onClick = {
                        authVM.signOut { onSignOut() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}
