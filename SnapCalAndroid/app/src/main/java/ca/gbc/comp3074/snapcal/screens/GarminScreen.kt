package ca.gbc.comp3074.snapcal.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarminScreen(
    nav: NavHostController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hcAvailable by remember { mutableStateOf<Boolean?>(null) }
    var hcHasPermissions by remember { mutableStateOf(false) }
    var todaySteps by remember { mutableStateOf<Long?>(null) }
    var todayCalories by remember { mutableStateOf<Int?>(null) }
    var weeklyCalories by remember { mutableStateOf<List<Int>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val client: HealthConnectClient? = remember {
        HealthConnectManager.getClientOrNull(context)
    }

    suspend fun refreshData(hcClient: HealthConnectClient) {
        error = null
        try {
            hcHasPermissions = HealthConnectManager.hasAllPermissions(hcClient)
            if (hcHasPermissions) {
                todaySteps = HealthConnectManager.readTodaySteps(hcClient)
                todayCalories = HealthConnectManager.readTodayCalories(hcClient)
                weeklyCalories = HealthConnectManager.readWeeklyCalories(hcClient)
            }
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Failed to read Health Connect data"
        }
    }

    // Лаунчер именно с Set<String>
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted: Set<String> ->
        hcHasPermissions = granted.containsAll(HealthConnectManager.PERMISSIONS)
        if (hcHasPermissions && client != null) {
            scope.launch { refreshData(client) }
        }
    }

    LaunchedEffect(client) {
        hcAvailable = client != null
        if (client != null) {
            refreshData(client)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Health & Activity", nav) }

        // --- Status ---
        item {
            CardBlock(title = "Health Connect status") {
                when (hcAvailable) {
                    null -> Text("Checking Health Connect...", color = Color.Gray)
                    false -> Text(
                        "Health Connect is not available on this device.",
                        color = MaterialTheme.colorScheme.error
                    )
                    true -> {
                        Text("Health Connect is available ✅")
                        Spacer(Modifier.height(4.dp))
                        if (hcHasPermissions) {
                            Text("Permissions granted ✅")
                        } else {
                            Text(
                                "Permissions not granted.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        permissionsLauncher.launch(HealthConnectManager.PERMISSIONS)
                                    } catch (e: Exception) {
                                        error = "Failed to launch permissions: ${e.localizedMessage}"
                                    }
                                }
                            ) {
                                Text("Grant permissions")
                            }
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- Today summary ---
        item {
            CardBlock(title = "Today summary") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Steps today: ${todaySteps ?: 0}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Calories burned: ${todayCalories ?: 0} kcal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "This data is read live from Health Connect.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // --- Weekly calories chart ---
        item {
            CardBlock(title = "Weekly calories burned") {
                if (hcHasPermissions && weeklyCalories != null) {
                    val labels = HealthConnectManager.weekDayLabels()
                    val values = weeklyCalories!!
                    val maxVal = (values.maxOrNull() ?: 0).coerceAtLeast(1)

                    Column {
                        Text(
                            "Last 7 days",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
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

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Bars show total calories burned per day.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text(
                        "Grant Health Connect permissions to see weekly calories.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
