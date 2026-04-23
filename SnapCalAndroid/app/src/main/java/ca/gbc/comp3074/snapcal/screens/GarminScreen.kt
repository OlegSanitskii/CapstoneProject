package ca.gbc.comp3074.snapcal.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.navigation.NavHostController
import ca.gbc.comp3074.snapcal.data.settings.ReportSettings
import ca.gbc.comp3074.snapcal.data.settings.ReportSettingsStore
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.reports.MonthlyReportService
import ca.gbc.comp3074.snapcal.reports.ReportScheduler
import ca.gbc.comp3074.snapcal.ui.components.AppTopBar
import ca.gbc.comp3074.snapcal.ui.components.CardBlock
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
        backgroundReadGranted = granted.contains(HealthConnectManager.BACKGROUND_READ_PERMISSION) || backgroundReadGranted
        historyReadGranted = granted.contains(HealthConnectManager.HISTORY_READ_PERMISSION) || historyReadGranted

        if (client != null) {
            scope.launch { refreshStatus(client) }
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
                if (reportSettings.monthlyReportsEnabled) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppTopBar("Health & Activity", nav) }

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
                            Text("Base permissions granted ✅")
                        } else {
                            Text(
                                "Base permissions not granted.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    healthPermissionsLauncher.launch(HealthConnectManager.PERMISSIONS)
                                }
                            ) {
                                Text("Grant permissions")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

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
                                        val extraPermissions =
                                            HealthConnectManager.automationPermissionsToRequest(hcClient)

                                        if (extraPermissions.isNotEmpty()) {
                                            healthPermissionsLauncher.launch(extraPermissions)
                                        } else {
                                            reportMessage = "No additional Health Connect permissions are available on this device."
                                        }
                                    } ?: run {
                                        reportMessage = "Health Connect client is not available."
                                    }
                                }
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
                        "Do you want SnapCal to automatically generate monthly reports for your consumed and burned calories?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Enable monthly reports",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "The app will save a CSV and a PDF every month.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Switch(
                            checked = reportSettings.monthlyReportsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    settingsStore.setMonthlyReportsEnabled(enabled)

                                    if (enabled) {
                                        if (reportSettings.reportsFolderUri.isNullOrBlank()) {
                                            folderPickerLauncher.launch(null)
                                        }

                                        client?.let { hcClient ->
                                            val extraPermissions =
                                                HealthConnectManager.automationPermissionsToRequest(hcClient)

                                            if (extraPermissions.isNotEmpty()) {
                                                healthPermissionsLauncher.launch(extraPermissions)
                                            }
                                        }

                                        ReportScheduler.scheduleNextMonthlyReport(context)
                                        reportMessage = "Monthly report automation enabled."
                                    } else {
                                        ReportScheduler.cancelMonthlyReports(context)
                                        reportMessage = "Monthly report automation disabled."
                                    }
                                }
                            }
                        )
                    }

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
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose folder")
                    }

                    val manualReportEnabled =
                        reportSettings.reportsFolderUri != null &&
                                hcAvailable == true &&
                                hcHasPermissions &&
                                !isGenerating

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
                            if (isGenerating) "Generating report..." else "Get report now"
                        )
                    }

                    Text(
                        "Manual export creates a report for the current month up to today. Automatic export creates the previous month's report on the next scheduled run.",
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