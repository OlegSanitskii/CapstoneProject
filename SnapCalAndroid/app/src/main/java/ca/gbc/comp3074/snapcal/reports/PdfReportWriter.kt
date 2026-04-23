package ca.gbc.comp3074.snapcal.reports

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

class PdfReportWriter {

    fun write(
        context: Context,
        folderUri: Uri,
        report: MonthlyReportData
    ): Uri {
        val directory = DocumentFile.fromTreeUri(context, folderUri)
            ?: error("Selected reports folder is invalid.")

        val fileName = "snapcal-report-${report.month}.pdf"

        directory.findFile(fileName)?.delete()

        val file = directory.createFile("application/pdf", fileName)
            ?: error("Failed to create PDF file.")

        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val margin = 40

        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val sectionPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint().apply {
            textSize = 11f
        }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = page.canvas
        var y = margin.toFloat()

        fun newPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            canvas = page.canvas
            y = margin.toFloat()
        }

        fun drawLine(text: String, paint: Paint, extraSpace: Float = 18f) {
            if (y > pageHeight - margin) {
                newPage()
            }
            canvas.drawText(text, margin.toFloat(), y, paint)
            y += extraSpace
        }

        drawLine("SnapCal Monthly Report", titlePaint, 28f)
        drawLine("Month: ${report.month}", bodyPaint)
        drawLine("", bodyPaint, 8f)

        drawLine("Summary", sectionPaint, 22f)
        drawLine("Total consumed: ${report.summary.totalConsumedKcal} kcal", bodyPaint)
        drawLine("Total burned: ${report.summary.totalBurnedKcal} kcal", bodyPaint)
        drawLine("Total balance: ${report.summary.totalBalanceKcal} kcal", bodyPaint)
        drawLine("Average consumed/day: ${report.summary.averageConsumedKcal} kcal", bodyPaint)
        drawLine("Average burned/day: ${report.summary.averageBurnedKcal} kcal", bodyPaint)
        drawLine("", bodyPaint, 10f)

        drawLine("Daily breakdown", sectionPaint, 22f)
        drawLine("Date        Consumed    Burned    Balance", bodyPaint)

        report.entries.forEach { entry ->
            val row = buildString {
                append(entry.date.toString().padEnd(12))
                append(entry.consumedKcal.toString().padEnd(12))
                append(entry.burnedKcal.toString().padEnd(10))
                append(entry.balanceKcal.toString())
            }
            drawLine(row, bodyPaint, 16f)
        }

        document.finishPage(page)

        context.contentResolver.openOutputStream(file.uri)?.use { stream ->
            document.writeTo(stream)
        } ?: error("Failed to open output stream for PDF file.")

        document.close()
        return file.uri
    }
}