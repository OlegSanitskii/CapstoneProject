package ca.gbc.comp3074.snapcal.reports

import android.net.Uri
import java.time.LocalDate
import java.time.YearMonth

data class DailyReportEntry(
    val date: LocalDate,
    val consumedKcal: Int,
    val burnedKcal: Int
) {
    val balanceKcal: Int
        get() = consumedKcal - burnedKcal
}

data class MonthlyReportSummary(
    val totalConsumedKcal: Int,
    val totalBurnedKcal: Int,
    val averageConsumedKcal: Int,
    val averageBurnedKcal: Int,
    val totalBalanceKcal: Int
)

data class MonthlyReportData(
    val month: YearMonth,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val entries: List<DailyReportEntry>,
    val summary: MonthlyReportSummary
) {
    val periodLabel: String
        get() = "$startDate to $endDate"

    val fileSuffix: String
        get() = if (startDate == month.atDay(1) && endDate == month.atEndOfMonth()) {
            month.toString()
        } else {
            "${month}-through-$endDate"
        }
}

data class ReportGenerationResult(
    val success: Boolean,
    val message: String,
    val month: YearMonth? = null,
    val csvUri: Uri? = null,
    val pdfUri: Uri? = null
)