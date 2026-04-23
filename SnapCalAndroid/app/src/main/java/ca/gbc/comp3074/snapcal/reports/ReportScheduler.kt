package ca.gbc.comp3074.snapcal.reports

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ca.gbc.comp3074.snapcal.workers.MonthlyReportWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReportScheduler {

    private const val UNIQUE_MONTHLY_REPORT_WORK = "snapcal_monthly_report_work"

    fun scheduleNextMonthlyReport(context: Context) {
        val now = LocalDateTime.now()
        val nextRun = now
            .plusMonths(1)
            .withDayOfMonth(1)
            .withHour(8)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val delayMillis = Duration.between(
            now.atZone(ZoneId.systemDefault()).toInstant(),
            nextRun.atZone(ZoneId.systemDefault()).toInstant()
        ).toMillis().coerceAtLeast(0L)

        val request = OneTimeWorkRequestBuilder<MonthlyReportWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_MONTHLY_REPORT_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelMonthlyReports(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_MONTHLY_REPORT_WORK)
    }
}