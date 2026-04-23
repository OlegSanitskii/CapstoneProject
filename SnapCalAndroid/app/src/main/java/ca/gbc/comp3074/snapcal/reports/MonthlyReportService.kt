package ca.gbc.comp3074.snapcal.reports

import android.content.Context
import android.net.Uri
import ca.gbc.comp3074.snapcal.data.db.DBProvider
import ca.gbc.comp3074.snapcal.data.repo.MealsRepository
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class MonthlyReportService(
    context: Context
) {
    private val appContext = context.applicationContext
    private val mealsRepository = MealsRepository(DBProvider.get(appContext).mealDao())
    private val csvWriter = CsvReportWriter()
    private val pdfWriter = PdfReportWriter()

    suspend fun generateCurrentMonthReport(folderUri: Uri): ReportGenerationResult =
        generateMonthReport(month = YearMonth.now(), folderUri = folderUri)

    suspend fun generatePreviousMonthReport(folderUri: Uri): ReportGenerationResult =
        generateMonthReport(month = YearMonth.now().minusMonths(1), folderUri = folderUri)

    suspend fun generateMonthReport(
        month: YearMonth,
        folderUri: Uri
    ): ReportGenerationResult = withContext(Dispatchers.IO) {
        try {
            val client = HealthConnectManager.getClientOrNull(appContext)
                ?: return@withContext ReportGenerationResult(
                    success = false,
                    message = "Health Connect is not available on this device."
                )

            if (!HealthConnectManager.hasAllPermissions(client)) {
                return@withContext ReportGenerationResult(
                    success = false,
                    message = "Health Connect read permissions are not granted."
                )
            }

            val needsHistory = month != YearMonth.now()
            if (needsHistory && !HealthConnectManager.hasHistoryPermission(client)) {
                return@withContext ReportGenerationResult(
                    success = false,
                    message = "History access is required to export a past month."
                )
            }

            val report = buildMonthlyReport(month, client)
            val csvUri = csvWriter.write(appContext, folderUri, report)
            val pdfUri = pdfWriter.write(appContext, folderUri, report)

            ReportGenerationResult(
                success = true,
                message = "Monthly report created successfully.",
                month = month,
                csvUri = csvUri,
                pdfUri = pdfUri
            )
        } catch (e: Exception) {
            ReportGenerationResult(
                success = false,
                message = e.localizedMessage ?: "Failed to create monthly report."
            )
        }
    }

    private suspend fun buildMonthlyReport(
        month: YearMonth,
        client: androidx.health.connect.client.HealthConnectClient
    ): MonthlyReportData {
        val zoneId = ZoneId.systemDefault()
        val monthStart = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEndExclusive = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val meals = mealsRepository.getInRange(monthStart, monthEndExclusive)
        val consumedByDate = meals.groupBy { meal ->
            Instant.ofEpochMilli(meal.createdAt).atZone(zoneId).toLocalDate()
        }.mapValues { (_, dayMeals) ->
            dayMeals.sumOf { it.calories }
        }

        val burnedByDay = HealthConnectManager.readDailyCaloriesForMonth(client, month)

        val entries = mutableListOf<DailyReportEntry>()
        var currentDate = month.atDay(1)

        for (i in 0 until month.lengthOfMonth()) {
            val date = currentDate.plusDays(i.toLong())
            val consumed = consumedByDate[date] ?: 0
            val burned = burnedByDay.getOrElse(i) { 0 }

            entries += DailyReportEntry(
                date = date,
                consumedKcal = consumed,
                burnedKcal = burned
            )
        }

        val totalConsumed = entries.sumOf { it.consumedKcal }
        val totalBurned = entries.sumOf { it.burnedKcal }
        val days = entries.size.coerceAtLeast(1)

        val summary = MonthlyReportSummary(
            totalConsumedKcal = totalConsumed,
            totalBurnedKcal = totalBurned,
            averageConsumedKcal = totalConsumed / days,
            averageBurnedKcal = totalBurned / days,
            totalBalanceKcal = totalConsumed - totalBurned
        )

        return MonthlyReportData(
            month = month,
            entries = entries,
            summary = summary
        )
    }
}