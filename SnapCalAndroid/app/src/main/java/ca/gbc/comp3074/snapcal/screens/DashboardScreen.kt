package ca.gbc.comp3074.snapcal.screens

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.snapcal.data.user.User
import ca.gbc.comp3074.snapcal.data.user.UserRepository
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel
import ca.gbc.comp3074.snapcal.ui.state.MealsViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun DashboardScreen(
    onScan: () -> Unit,
    onManual: () -> Unit,
    onProgress: () -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current

    val authVM: AuthViewModel = viewModel()
    val email by authVM.userEmail.collectAsState()
    val userId by authVM.userId.collectAsState()

    val userRepository = remember { UserRepository.get(context) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {
        currentUser = userId?.let { id ->
            userRepository.getById(id)
        }
    }

    val healthEnabledForThisAccount = currentUser?.healthConnectEnabled == true

    val mealsVM: MealsViewModel = viewModel()

    val totalTodayKcal by remember(mealsVM) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (from, to) = mealsVM.todayBounds()
            mealsVM.observeTotalCalories(from, to)
        } else {
            flowOf(0)
        }
    }.collectAsState(initial = 0)

    var hcAvailable by remember { mutableStateOf<Boolean?>(null) }
    var hcHasPermissions by remember { mutableStateOf(false) }

    var todaySteps by remember { mutableStateOf<Long?>(null) }
    var weeklySteps by remember { mutableStateOf<List<Long>?>(null) }

    var todayBurned by remember { mutableStateOf<Int?>(null) }
    var weeklyBurned by remember { mutableStateOf<List<Int>?>(null) }

    var hcError by remember { mutableStateOf<String?>(null) }

    val client = remember { HealthConnectManager.getClientOrNull(context) }

    LaunchedEffect(client, healthEnabledForThisAccount) {
        hcAvailable = client != null
        hcHasPermissions = false

        todaySteps = null
        weeklySteps = null
        todayBurned = null
        weeklyBurned = null
        hcError = null

        if (!healthEnabledForThisAccount) {
            return@LaunchedEffect
        }

        if (client != null) {
            try {
                hcHasPermissions = HealthConnectManager.hasAllPermissions(client)

                if (hcHasPermissions) {
                    todaySteps = HealthConnectManager.readTodaySteps(client)
                    weeklySteps = HealthConnectManager.readWeeklySteps(client)

                    todayBurned = HealthConnectManager.readTodayCalories(client)
                    weeklyBurned = HealthConnectManager.readWeeklyCalories(client)
                }
            } catch (e: Exception) {
                hcError = e.localizedMessage ?: "Failed to read Health Connect data"
            }
        }
    }

    val kcalGoal = 2200
    val consumed = totalTodayKcal
    val burned = if (healthEnabledForThisAccount) todayBurned ?: 0 else 0
    val remaining = (kcalGoal - consumed).coerceAtLeast(0)
    val balance = consumed - burned

    val progress = if (kcalGoal > 0) {
        consumed.coerceAtMost(kcalGoal).toFloat() / kcalGoal.toFloat()
    } else {
        0f
    }

    val weeklyStepsTotal = if (healthEnabledForThisAccount) weeklySteps?.sum() ?: 0L else 0L
    val weeklyBurnedTotal = if (healthEnabledForThisAccount) weeklyBurned?.sum() ?: 0 else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Dashboard") }

        item {
            CardBlock(title = "Calories (in vs out)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            val diameter = size.minDimension
                            val topLeft = Offset(
                                (size.width - diameter) / 2f,
                                (size.height - diameter) / 2f
                            )

                            drawArc(
                                color = Color(0xFFE5E5E5),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = Color(0xFF4A90E2),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatNumber(consumed),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "kcal in",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryLine(
                            label = "Out",
                            value = "${formatNumber(burned)} kcal"
                        )

                        SummaryLine(
                            label = "Balance",
                            value = (if (balance > 0) "+" else "") + "${formatNumber(balance)} kcal",
                            bold = true
                        )

                        SummaryLine(
                            label = "Goal",
                            value = "${formatNumber(kcalGoal)} kcal"
                        )

                        SummaryLine(
                            label = "Remaining",
                            value = "${formatNumber(remaining)} kcal",
                            valueColor = Color.Gray
                        )

                        Text(
                            text = if (healthEnabledForThisAccount) {
                                "Out is read live from Health Connect."
                            } else {
                                "Health data is disabled for this account."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardBlock(
                    modifier = Modifier.weight(1f),
                    title = "Steps"
                ) {
                    when {
                        !healthEnabledForThisAccount -> {
                            SmallMutedText("Health data is disabled for this account.")
                        }

                        hcAvailable == false -> {
                            SmallMutedText("Health Connect not available.")
                        }

                        hcHasPermissions && todaySteps != null -> {
                            MetricValue(formatNumber(todaySteps ?: 0L))
                            MetricLabel("Today")

                            Spacer(Modifier.height(12.dp))

                            SecondaryMetricValue(formatNumber(weeklyStepsTotal))
                            MetricLabel("This week")
                        }

                        hcHasPermissions && todaySteps == null -> {
                            SmallMutedText("Loading steps...")
                        }

                        hcAvailable == true && !hcHasPermissions -> {
                            SmallMutedText("Connect Health Connect in Settings.")
                        }

                        else -> {
                            SmallMutedText("Steps data not available.")
                        }
                    }

                    if (hcError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = hcError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                CardBlock(
                    modifier = Modifier.weight(1f),
                    title = "Activity"
                ) {
                    when {
                        !healthEnabledForThisAccount -> {
                            SmallMutedText("Health data is disabled for this account.")
                        }

                        hcAvailable == false -> {
                            SmallMutedText("Health Connect not available.")
                        }

                        hcHasPermissions -> {
                            MetricValue("${formatNumber(burned)} kcal")
                            MetricLabel("Burned today")

                            Spacer(Modifier.height(12.dp))

                            SecondaryMetricValue("${formatNumber(weeklyBurnedTotal)} kcal")
                            MetricLabel("Last 7 days")
                        }

                        hcAvailable == true && !hcHasPermissions -> {
                            SmallMutedText("Connect Health Connect in Settings.")
                        }

                        else -> {
                            SmallMutedText("Activity data not available.")
                        }
                    }
                }
            }
        }

        item {
            CardBlock(title = "Steps") {
                if (healthEnabledForThisAccount && hcHasPermissions && weeklySteps != null) {
                    val labels = HealthConnectManager.weekDayLabels()
                    val values = weeklySteps!!
                    val maxVal = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Current week",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            values.forEachIndexed { index, value ->
                                val fraction =
                                    (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)

                                Column(
                                    modifier = Modifier.width(34.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = if (value > 0) compactNumber(value) else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .height(92.dp)
                                            .width(18.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        if (value > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(fraction.coerceAtLeast(0.08f))
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = labels[index],
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    SmallMutedText(
                        if (healthEnabledForThisAccount) {
                            "Connect Health Connect to see weekly steps."
                        } else {
                            "Health data is disabled for this account."
                        }
                    )
                }
            }
        }

        item {
            CardBlock(title = "") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Nutrition Facts Label")
                    }
                }
            }
        }

        item {
            CardBlock(title = "Account") {
                if (email.isNotBlank()) {
                    Text(
                        text = email,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                } else {
                    Text(
                        text = "Guest account",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        authVM.signOut { onSignOut() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = if (valueColor == Color.Unspecified) {
                MaterialTheme.colorScheme.onSurface
            } else {
                valueColor
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MetricValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SecondaryMetricValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun MetricLabel(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun SmallMutedText(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun formatNumber(value: Int): String = "%,d".format(value)

private fun formatNumber(value: Long): String = "%,d".format(value)

private fun compactNumber(value: Long): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format("%.1fk", value / 1_000f)
        else -> value.toString()
    }
}