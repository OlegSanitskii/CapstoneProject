package ca.gbc.comp3074.snapcal.reports

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

class CsvReportWriter {

    fun write(
        context: Context,
        folderUri: Uri,
        report: MonthlyReportData
    ): Uri {
        val directory = DocumentFile.fromTreeUri(context, folderUri)
            ?: error("Selected reports folder is invalid.")

        val fileName = "snapcal-report-${report.month}.csv"

        directory.findFile(fileName)?.delete()

        val file = directory.createFile("text/csv", fileName)
            ?: error("Failed to create CSV file.")

        context.contentResolver.openOutputStream(file.uri)?.bufferedWriter()?.use { writer ->
            writer.appendLine("SnapCal Monthly Report")
            writer.appendLine("Month,${report.month}")
            writer.appendLine("Total consumed kcal,${report.summary.totalConsumedKcal}")
            writer.appendLine("Total burned kcal,${report.summary.totalBurnedKcal}")
            writer.appendLine("Total balance kcal,${report.summary.totalBalanceKcal}")
            writer.appendLine("Average consumed kcal,${report.summary.averageConsumedKcal}")
            writer.appendLine("Average burned kcal,${report.summary.averageBurnedKcal}")
            writer.appendLine()
            writer.appendLine("date,consumed_kcal,burned_kcal,balance_kcal")

            report.entries.forEach { entry ->
                writer.appendLine(
                    listOf(
                        entry.date.toString(),
                        entry.consumedKcal.toString(),
                        entry.burnedKcal.toString(),
                        entry.balanceKcal.toString()
                    ).joinToString(",")
                )
            }
        } ?: error("Failed to open output stream for CSV file.")

        return file.uri
    }
}