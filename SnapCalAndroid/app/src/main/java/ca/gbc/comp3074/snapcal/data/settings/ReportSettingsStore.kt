package ca.gbc.comp3074.snapcal.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reportSettingsDataStore by preferencesDataStore(name = "report_settings")

data class ReportSettings(
    val monthlyReportsEnabled: Boolean = false,
    val reportsFolderUri: String? = null,
    val lastGeneratedMonth: String? = null,
    val lastGeneratedAtMillis: Long? = null,
    val lastCsvUri: String? = null,
    val lastPdfUri: String? = null
)

class ReportSettingsStore(private val context: Context) {

    private object Keys {
        val MONTHLY_REPORTS_ENABLED = booleanPreferencesKey("monthly_reports_enabled")
        val REPORTS_FOLDER_URI = stringPreferencesKey("reports_folder_uri")
        val LAST_GENERATED_MONTH = stringPreferencesKey("last_generated_month")
        val LAST_GENERATED_AT_MILLIS = longPreferencesKey("last_generated_at_millis")
        val LAST_CSV_URI = stringPreferencesKey("last_csv_uri")
        val LAST_PDF_URI = stringPreferencesKey("last_pdf_uri")
    }

    val settings: Flow<ReportSettings> =
        context.reportSettingsDataStore.data.map { prefs ->
            ReportSettings(
                monthlyReportsEnabled = prefs[Keys.MONTHLY_REPORTS_ENABLED] ?: false,
                reportsFolderUri = prefs[Keys.REPORTS_FOLDER_URI],
                lastGeneratedMonth = prefs[Keys.LAST_GENERATED_MONTH],
                lastGeneratedAtMillis = prefs[Keys.LAST_GENERATED_AT_MILLIS],
                lastCsvUri = prefs[Keys.LAST_CSV_URI],
                lastPdfUri = prefs[Keys.LAST_PDF_URI]
            )
        }

    suspend fun setMonthlyReportsEnabled(enabled: Boolean) {
        context.reportSettingsDataStore.edit { prefs ->
            prefs[Keys.MONTHLY_REPORTS_ENABLED] = enabled
        }
    }

    suspend fun setReportsFolderUri(uri: String?) {
        context.reportSettingsDataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(Keys.REPORTS_FOLDER_URI)
            } else {
                prefs[Keys.REPORTS_FOLDER_URI] = uri
            }
        }
    }

    suspend fun recordGeneratedReport(
        month: String,
        generatedAtMillis: Long,
        csvUri: String?,
        pdfUri: String?
    ) {
        context.reportSettingsDataStore.edit { prefs ->
            prefs[Keys.LAST_GENERATED_MONTH] = month
            prefs[Keys.LAST_GENERATED_AT_MILLIS] = generatedAtMillis

            if (csvUri == null) prefs.remove(Keys.LAST_CSV_URI) else prefs[Keys.LAST_CSV_URI] = csvUri
            if (pdfUri == null) prefs.remove(Keys.LAST_PDF_URI) else prefs[Keys.LAST_PDF_URI] = pdfUri
        }
    }

    suspend fun clearLastGeneratedInfo() {
        context.reportSettingsDataStore.edit { prefs ->
            prefs.remove(Keys.LAST_GENERATED_MONTH)
            prefs.remove(Keys.LAST_GENERATED_AT_MILLIS)
            prefs.remove(Keys.LAST_CSV_URI)
            prefs.remove(Keys.LAST_PDF_URI)
        }
    }
}