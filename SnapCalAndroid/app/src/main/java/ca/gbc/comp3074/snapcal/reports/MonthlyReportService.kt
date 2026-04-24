package ca.gbc.comp3074.snapcal.reports

import android.content.Context
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import ca.gbc.comp3074.snapcal.data.auth.SessionStore
import ca.gbc.comp3074.snapcal.data.db.DBProvider
import ca.gbc.comp3074.snapcal.data.repo.MealsRepository
import ca.gbc.comp3074.snapcal.data.user.UserRepository
import ca.gbc.comp3074.snapcal.health.HealthConnectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class MonthlyReportService(
    context: Context
) {
    private val appContext = context.applicationContext
    private val session = SessionStore(appContext)
    private val userRepository = UserRepository.get(appContext)
    private val mealsRepository = MealsRepository(DBProvider.get(appContext).mealDao())
    private val csvWriter = CsvReportWriter()
    private val pdfWriter = PdfReportWriter()

    suspend fun generateCurrentMonthReport(folderUri: Uri): ReportGenerationResult {
        val today = LocalDate.now()
        val month = YearMonth.from(today)

        return generateReportForPeriod(
            month = month,
            startDate = month.atDay(1),
            endDate = today,
            folderUri = folderUri
        )
    }

    suspend fun generatePreviousMonthReport(folderUri: Uri): ReportGenerationResult {
        val previousMonth = YearMonth.now().minusMonths(1)

        return generateReportForPeriod(
            month = previousMonth,
            startDate = previousMonth.atDay(1),
            endDate = previousMonth.atEndOfMonth(),
            folderUri = folderUri
        )
    }

    suspend fun generateMonthReport(
        month: YearMonth,
        folderUri: Uri
    ): ReportGenerationResult {
        return generateReportForPeriod(
            month = month,
            startDate = month.atDay(1),
            endDate = month.atEndOfMonth(),
            folderUri = folderUri
        )
    }

    private suspend fun generateReportForPeriod(
        month: YearMonth,
        startDate: LocalDate,
        endDate: LocalDate,
        folderUri: Uri
    ): ReportGenerationResult = withContext(Dispatchers.IO) {
        try {
            val currentUserId = session.userId.first()
                ?: return@withContext ReportGenerationResult(
                    success = false,
                    message = "Please sign in to generate account reports."
                )

            val currentUser = userRepository.getById(currentUserId)
                ?: return@withContext ReportGenerationResult(
                    success = false,
                    message = "Current user was not found."
                )

            if (!currentUser.reportsEnabled) {
                return@withContext ReportGenerationResult(
                    success = false,
                    message = "Reports are disabled for this account."
                )
            }

            val client = if (currentUser.healthConnectEnabled) {
                HealthConnectManager.getClientOrNull(appContext)
                    ?: return@withContext ReportGenerationResult(
                        success = false,
                        message = "Health Connect is enabled for this account, but it is not available on this device."
                    )
            } else {
                null
            }

            if (currentUser.healthConnectEnabled && client != null) {
                if (!HealthConnectManager.hasAllPermissions(client)) {
                    return@withContext ReportGenerationResult(
                        success = false,
                        message = "Health Connect read permissions are not granted."
                    )
                }

                val today = LocalDate.now()
                val needsHistory = startDate.isBefore(today.minusDays(30))

                if (needsHistory && !HealthConnectManager.hasHistoryPermission(client)) {
                    return@withContext ReportGenerationResult(
                        success = false,
                        message = "History access is required to export this report period."
                    )
                }
            }

            val report = buildMonthlyReport(
                month = month,
                startDate = startDate,
                endDate = endDate,
                userId = currentUserId,
                healthConnectEnabled = currentUser.healthConnectEnabled,
                client = client
            )

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
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Int,
        healthConnectEnabled: Boolean,
        client: HealthConnectClient?
    ): MonthlyReportData {
        val zoneId = ZoneId.systemDefault()

        val startMillis = startDate
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val endExclusive = endDate.plusDays(1)

        val endMillisExclusive = endExclusive
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val meals = mealsRepository.getInRange(
            userId = userId,
            fromMillis = startMillis,
            toMillis = endMillisExclusive
        )

        val consumedByDate = meals
            .groupBy { meal ->
                Instant.ofEpochMilli(meal.createdAt)
                    .atZone(zoneId)
                    .toLocalDate()
            }
            .mapValues { (_, dayMeals) ->
                dayMeals.sumOf { meal -> meal.calories }
            }

        val burnedByDate = if (healthConnectEnabled && client != null) {
            HealthConnectManager.readDailyCaloriesForRange(
                client = client,
                startInclusive = startDate,
                endExclusive = endExclusive,
                zoneId = zoneId
            )
        } else {
            emptyMap()
        }

        val entries = mutableListOf<DailyReportEntry>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val consumed = consumedByDate[currentDate] ?: 0
            val burned = burnedByDate[currentDate] ?: 0

            entries += DailyReportEntry(
                date = currentDate,
                consumedKcal = consumed,
                burnedKcal = burned
            )

            currentDate = currentDate.plusDays(1)
        }

        val totalConsumed = entries.sumOf { entry -> entry.consumedKcal }
        val totalBurned = entries.sumOf { entry -> entry.burnedKcal }
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
            startDate = startDate,
            endDate = endDate,
            entries = entries,
            summary = summary
        )
    }
}