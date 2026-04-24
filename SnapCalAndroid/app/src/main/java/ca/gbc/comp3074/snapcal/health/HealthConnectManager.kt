package ca.gbc.comp3074.snapcal.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object HealthConnectManager {

    const val BACKGROUND_READ_PERMISSION = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"
    const val HISTORY_READ_PERMISSION = "android.permission.health.READ_HEALTH_DATA_HISTORY"

    val PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    )

    fun getClientOrNull(context: Context): HealthConnectClient? {
        return when (HealthConnectClient.getSdkStatus(context)) {
            SDK_AVAILABLE -> HealthConnectClient.getOrCreate(context)
            SDK_UNAVAILABLE, SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> null
            else -> null
        }
    }

    suspend fun getGrantedPermissions(client: HealthConnectClient): Set<String> {
        return client.permissionController.getGrantedPermissions()
    }

    suspend fun hasAllPermissions(client: HealthConnectClient): Boolean {
        return getGrantedPermissions(client).containsAll(PERMISSIONS)
    }

    suspend fun hasHistoryPermission(client: HealthConnectClient): Boolean {
        return getGrantedPermissions(client).contains(HISTORY_READ_PERMISSION)
    }

    suspend fun hasBackgroundReadPermission(client: HealthConnectClient): Boolean {
        return getGrantedPermissions(client).contains(BACKGROUND_READ_PERMISSION)
    }

    fun isBackgroundReadFeatureAvailable(client: HealthConnectClient): Boolean {
        return client.features.getFeatureStatus(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
        ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }

    fun isHistoryReadFeatureAvailable(client: HealthConnectClient): Boolean {
        return client.features.getFeatureStatus(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY
        ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }

    fun automationPermissionsToRequest(client: HealthConnectClient): Set<String> {
        val extra = mutableSetOf<String>()

        if (isBackgroundReadFeatureAvailable(client)) {
            extra += BACKGROUND_READ_PERMISSION
        }

        if (isHistoryReadFeatureAvailable(client)) {
            extra += HISTORY_READ_PERMISSION
        }

        return extra
    }

    suspend fun readTodaySteps(client: HealthConnectClient): Long {
        val now = ZonedDateTime.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    startOfDay.toInstant(),
                    now.toInstant()
                )
            )
        )

        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readTodayCalories(client: HealthConnectClient): Int {
        val now = ZonedDateTime.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    startOfDay.toInstant(),
                    now.toInstant()
                )
            )
        )

        val energy: Energy =
            response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                ?: Energy.kilocalories(0.0)

        return energy.inKilocalories.roundToInt()
    }

    suspend fun readWeeklySteps(client: HealthConnectClient): List<Long> {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val startOfWeek = now.minusDays(((now.dayOfWeek.value + 6) % 7).toLong())

        val result = mutableListOf<Long>()

        for (i in 0 until 7) {
            val dayStart = startOfWeek.plusDays(i.toLong())
            val dayEnd = dayStart.plusDays(1)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        dayStart.toInstant(),
                        dayEnd.toInstant()
                    )
                )
            )

            result.add(response[StepsRecord.COUNT_TOTAL] ?: 0L)
        }

        return result
    }

    suspend fun readWeeklyCalories(client: HealthConnectClient): List<Int> {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val startOfWeek = now.minusDays(((now.dayOfWeek.value + 6) % 7).toLong())

        val result = mutableListOf<Int>()

        for (i in 0 until 7) {
            val dayStart = startOfWeek.plusDays(i.toLong())
            val dayEnd = dayStart.plusDays(1)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        dayStart.toInstant(),
                        dayEnd.toInstant()
                    )
                )
            )

            val energy: Energy =
                response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                    ?: Energy.kilocalories(0.0)

            result.add(energy.inKilocalories.roundToInt())
        }

        return result
    }

    suspend fun readDailyCaloriesForMonth(
        client: HealthConnectClient,
        month: YearMonth
    ): Map<LocalDate, Int> {
        val zoneId = ZoneId.systemDefault()
        val start = month.atDay(1)
        val endExclusive = month.plusMonths(1).atDay(1)

        return readDailyCaloriesForRange(client, start, endExclusive, zoneId)
    }

    suspend fun readDailyCaloriesForRange(
        client: HealthConnectClient,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Map<LocalDate, Int> {
        val result = mutableMapOf<LocalDate, Int>()
        var current = startInclusive

        while (current.isBefore(endExclusive)) {
            val dayStart = current.atStartOfDay(zoneId)
            val dayEnd = current.plusDays(1).atStartOfDay(zoneId)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        dayStart.toInstant(),
                        dayEnd.toInstant()
                    )
                )
            )

            val activeEnergy = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]

            if (activeEnergy != null && activeEnergy.inKilocalories > 0.0) {
                result[current] = activeEnergy.inKilocalories.roundToInt()
            }

            current = current.plusDays(1)
        }

        return result
    }

    fun weekDayLabels(): List<String> =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
}