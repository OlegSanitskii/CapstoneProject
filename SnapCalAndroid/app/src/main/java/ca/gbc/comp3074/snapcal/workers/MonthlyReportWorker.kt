package ca.gbc.comp3074.snapcal.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.gbc.comp3074.snapcal.data.settings.ReportSettingsStore
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import ca.gbc.comp3074.snapcal.reports.MonthlyReportService
import ca.gbc.comp3074.snapcal.reports.ReportScheduler
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class MonthlyReportWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settingsStore = ReportSettingsStore(applicationContext)
        val settings = settingsStore.settings.first()

        if (!settings.monthlyReportsEnabled) {
            return Result.success()
        }

        val folderUriString = settings.reportsFolderUri
            ?: return Result.failure()

        val client = HealthConnectManager.getClientOrNull(applicationContext)
            ?: return Result.retry()

        if (!HealthConnectManager.hasAllPermissions(client)) {
            return Result.retry()
        }

        if (HealthConnectManager.isBackgroundReadFeatureAvailable(client) &&
            !HealthConnectManager.hasBackgroundReadPermission(client)
        ) {
            return Result.retry()
        }

        if (HealthConnectManager.isHistoryReadFeatureAvailable(client) &&
            !HealthConnectManager.hasHistoryPermission(client)
        ) {
            return Result.retry()
        }

        val targetMonth = YearMonth.now().minusMonths(1)

        if (settings.lastGeneratedMonth == targetMonth.toString()) {
            ReportScheduler.scheduleNextMonthlyReport(applicationContext)
            return Result.success()
        }

        val service = MonthlyReportService(applicationContext)
        val result = service.generateMonthReport(
            month = targetMonth,
            folderUri = Uri.parse(folderUriString)
        )

        return if (result.success) {
            settingsStore.recordGeneratedReport(
                month = targetMonth.toString(),
                generatedAtMillis = System.currentTimeMillis(),
                csvUri = result.csvUri?.toString(),
                pdfUri = result.pdfUri?.toString()
            )

            ReportScheduler.scheduleNextMonthlyReport(applicationContext)
            Result.success()
        } else {
            Result.retry()
        }
    }
}