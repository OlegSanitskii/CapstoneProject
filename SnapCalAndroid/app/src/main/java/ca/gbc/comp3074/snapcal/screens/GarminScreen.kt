package ca.gbc.comp3074.snapcal.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.data.settings.ReportSettings
import ca.gbc.comp3074.snapcal.data.settings.ReportSettingsStore
import ca.gbc.comp3074.snapcal.data.user.User
import ca.gbc.comp3074.snapcal.data.user.UserRepository
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.reports.MonthlyReportService
import ca.gbc.comp3074.snapcal.reports.ReportScheduler
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
import ca.gbc.comp3074.snapcal.ui.state.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarminScreen(
    nav: NavHostController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val authVM: AuthViewModel = viewModel()
    val userId by authVM.userId.collectAsState()
    val email by authVM.userEmail.collectAsState()

    val userRepository = remember { UserRepository.get(context) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    val settingsStore = remember { ReportSettingsStore(context) }
    val reportService = remember { MonthlyReportService(context) }
    val reportSettings by settingsStore.settings.collectAsState(initial = ReportSettings())

    var hcAvailable by remember { mutableStateOf<Boolean?>(null) }
    var hcHasPermissions by remember { mutableStateOf(false) }
    var backgroundReadGranted by remember { mutableStateOf(false) }
    var historyReadGranted by remember { mutableStateOf(false) }
    var backgroundReadAvailable by remember { mutableStateOf(false) }
    var historyReadAvailable by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var reportMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val client: HealthConnectClient? = remember {
        HealthConnectManager.getClientOrNull(context)
    }

    LaunchedEffect(userId) {
        currentUser = userId?.let { id ->
            userRepository.getById(id)
        }
    }

    suspend fun refreshStatus(hcClient: HealthConnectClient) {
        error = null

        try {
            hcHasPermissions = HealthConnectManager.hasAllPermissions(hcClient)
            backgroundReadAvailable = HealthConnectManager.isBackgroundReadFeatureAvailable(hcClient)
            historyReadAvailable = HealthConnectManager.isHistoryReadFeatureAvailable(hcClient)
            backgroundReadGranted = HealthConnectManager.hasBackgroundReadPermission(hcClient)
            historyReadGranted = HealthConnectManager.hasHistoryPermission(hcClient)
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Failed to read Health Connect status."
        }
    }

    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted: Set<String> ->
        hcHasPermissions = granted.containsAll(HealthConnectManager.PERMISSIONS)

        backgroundReadGranted =
            granted.contains(HealthConnectManager.BACKGROUND_READ_PERMISSION) || backgroundReadGranted

        historyReadGranted =
            granted.contains(HealthConnectManager.HISTORY_READ_PERMISSION) || historyReadGranted

        if (client != null) {
            scope.launch {
                refreshStatus(client)
            }
        }
    }

    suspend fun requestMissingBasePermissions(hcClient: HealthConnectClient) {
        val granted = HealthConnectManager.getGrantedPermissions(hcClient)
        val missingPermissions = HealthConnectManager.PERMISSIONS - granted

        if (missingPermissions.isEmpty()) {
            reportMessage = "Base Health Connect permissions are already granted."
            refreshStatus(hcClient)
        } else {
            healthPermissionsLauncher.launch(missingPermissions)
        }
    }

    suspend fun requestMissingAutomationPermissions(hcClient: HealthConnectClient) {
        val granted = HealthConnectManager.getGrantedPermissions(hcClient)

        val availableAutomationPermissions =
            HealthConnectManager.automationPermissionsToRequest(hcClient)

        val missingAutomationPermissions =
            availableAutomationPermissions - granted

        if (missingAutomationPermissions.isEmpty()) {
            reportMessage = "Automation permissions are already granted or not available on this device."
            refreshStatus(hcClient)
        } else {
            healthPermissionsLauncher.launch(missingAutomationPermissions)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)

            scope.launch {
                settingsStore.setReportsFolderUri(uri.toString())
                reportMessage = "Reports folder saved."

                if (currentUser?.reportsEnabled == true) {
                    ReportScheduler.scheduleNextMonthlyReport(context)
                }
            }
        } catch (e: Exception) {
            reportMessage = e.localizedMessage ?: "Failed to save selected folder."
        }
    }

    LaunchedEffect(client) {
        hcAvailable = client != null

        if (client != null) {
            refreshStatus(client)
        }
    }

    fun folderLabel(uriString: String?): String {
        if (uriString.isNullOrBlank()) return "No folder selected yet"
        return Uri.parse(uriString).lastPathSegment ?: uriString
    }

    fun formatDate(millis: Long?): String {
        if (millis == null) return "Not generated yet"

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    val signedInUser = currentUser
    val healthEnabledForAccount = signedInUser?.healthConnectEnabled == true
    val reportsEnabledForAccount = signedInUser?.reportsEnabled == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppTopBar("Health & Activity", nav)
        }

        item {
            CardBlock(title = "Account health settings") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (email.isBlank()) {
                            "Guest account"
                        } else {
                            email
                        },
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (signedInUser == null) {
                        Text(
                            text = "Health Connect is disabled for guest mode. Sign in to enable activity data for an account.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Enable Health Connect for this account",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Text(
                                    "Allows this SnapCal account to use Health Connect data for dashboard activity and burned calories in reports.",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Switch(
                                checked = healthEnabledForAccount,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        userRepository.setHealthConnectEnabled(signedInUser.id, enabled)

                                        currentUser = signedInUser.copy(
                                            healthConnectEnabled = enabled
                                        )

                                        if (enabled) {
                                            if (client == null) {
                                                reportMessage = "Health Connect is not available on this device."
                                            } else {
                                                requestMissingBasePermissions(client)
                                                refreshStatus(client)
                                            }
                                        } else {
                                            reportMessage = "Health Connect disabled for this account."
                                        }
                                    }
                                }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Enable monthly reports for this account",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Text(
                                    "Allows manual and automatic reports only for this SnapCal account.",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Switch(
                                checked = reportsEnabledForAccount,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        userRepository.setReportsEnabled(signedInUser.id, enabled)

                                        currentUser = signedInUser.copy(
                                            reportsEnabled = enabled
                                        )

                                        settingsStore.setMonthlyReportsEnabled(enabled)

                                        if (enabled) {
                                            if (reportSettings.reportsFolderUri.isNullOrBlank()) {
                                                folderPickerLauncher.launch(null)
                                            }

                                            if (healthEnabledForAccount && client != null) {
                                                requestMissingAutomationPermissions(client)
                                            }

                                            ReportScheduler.scheduleNextMonthlyReport(context)
                                            reportMessage = "Monthly reports enabled for this account."
                                        } else {
                                            ReportScheduler.cancelMonthlyReports(context)
                                            reportMessage = "Monthly reports disabled for this account."
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            CardBlock(title = "Health Connect status") {
                when (hcAvailable) {
                    null -> {
                        Text("Checking Health Connect...", color = Color.Gray)
                    }

                    false -> {
                        Text(
                            "Health Connect is not available on this device.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    true -> {
                        Text("Health Connect is available ✅")

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (healthEnabledForAccount) {
                                "Enabled for this SnapCal account ✅"
                            } else {
                                "Disabled for this SnapCal account."
                            },
                            color = if (healthEnabledForAccount) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.Gray
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Base Health Connect permissions",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            "Required to read steps and active calories. Used for dashboard activity data and burned calories in monthly reports.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(6.dp))

                        if (hcHasPermissions) {
                            Text("Base permissions granted ✅")
                        } else {
                            Text(
                                "Base permissions not granted.",
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    client?.let { hcClient ->
                                        scope.launch {
                                            requestMissingBasePermissions(hcClient)
                                        }
                                    } ?: run {
                                        reportMessage = "Health Connect client is not available."
                                    }
                                },
                                enabled = signedInUser != null && healthEnabledForAccount
                            ) {
                                Text("Grant base permissions")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (backgroundReadAvailable || historyReadAvailable) {
                            Text(
                                "Automation permissions",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                "Used for automatic monthly reports. Background access lets SnapCal read Health Connect data when the app is not open. History access lets SnapCal export older report periods.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.height(6.dp))
                        }

                        if (backgroundReadAvailable) {
                            Text(
                                if (backgroundReadGranted) {
                                    "Background read access granted ✅"
                                } else {
                                    "Background read access not granted."
                                },
                                color = if (backgroundReadGranted) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }

                        if (historyReadAvailable) {
                            Text(
                                if (historyReadGranted) {
                                    "History read access granted ✅"
                                } else {
                                    "History read access not granted."
                                },
                                color = if (historyReadGranted) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }

                        if ((backgroundReadAvailable && !backgroundReadGranted) ||
                            (historyReadAvailable && !historyReadGranted)
                        ) {
                            Spacer(Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    client?.let { hcClient ->
                                        scope.launch {
                                            requestMissingAutomationPermissions(hcClient)
                                        }
                                    } ?: run {
                                        reportMessage = "Health Connect client is not available."
                                    }
                                },
                                enabled = signedInUser != null && healthEnabledForAccount
                            ) {
                                Text("Grant automation access")
                            }
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            CardBlock(title = "Monthly reports") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Reports are controlled per SnapCal account. Consumed calories come from this account's meals. Burned calories are included only if Health Connect is enabled and permissions are granted for this account.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = if (reportsEnabledForAccount) {
                            "Reports are enabled for this account ✅"
                        } else {
                            "Reports are disabled for this account."
                        },
                        color = if (reportsEnabledForAccount) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            Color.Gray
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider()

                    Text(
                        "Reports folder",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        folderLabel(reportSettings.reportsFolderUri),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedButton(
                        onClick = {
                            folderPickerLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = signedInUser != null
                    ) {
                        Text("Choose folder")
                    }

                    val manualReportEnabled =
                        signedInUser != null &&
                                reportsEnabledForAccount &&
                                reportSettings.reportsFolderUri != null &&
                                !isGenerating &&
                                (
                                        !healthEnabledForAccount ||
                                                (hcAvailable == true && hcHasPermissions)
                                        )

                    Button(
                        onClick = {
                            val folderUriString = reportSettings.reportsFolderUri ?: return@Button

                            scope.launch {
                                isGenerating = true

                                val result = reportService.generateCurrentMonthReport(
                                    folderUri = Uri.parse(folderUriString)
                                )

                                if (result.success) {
                                    settingsStore.recordGeneratedReport(
                                        month = result.month.toString(),
                                        generatedAtMillis = System.currentTimeMillis(),
                                        csvUri = result.csvUri?.toString(),
                                        pdfUri = result.pdfUri?.toString()
                                    )
                                }

                                reportMessage = result.message
                                isGenerating = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = manualReportEnabled
                    ) {
                        Text(
                            if (isGenerating) {
                                "Generating report..."
                            } else {
                                "Get report now"
                            }
                        )
                    }

                    Text(
                        "Manual export creates a report for the current month up to today. If Health Connect is disabled for this account, burned calories will be exported as 0.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider()

                    Text(
                        "Last generated month: ${reportSettings.lastGeneratedMonth ?: "None"}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        "Last generated at: ${formatDate(reportSettings.lastGeneratedAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (reportMessage != null) {
                        Spacer(Modifier.height(4.dp))

                        Text(
                            reportMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}