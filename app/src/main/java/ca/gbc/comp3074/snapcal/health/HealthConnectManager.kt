package ca.gbc.comp3074.snapcal.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object HealthConnectManager {


    val PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    )

    fun getClientOrNull(context: Context): HealthConnectClient? {
        return when (HealthConnectClient.getSdkStatus(context)) {
            SDK_AVAILABLE -> HealthConnectClient.getOrCreate(context)
            SDK_UNAVAILABLE,
            SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> null
            else -> null
        }
    }

    suspend fun hasAllPermissions(client: HealthConnectClient): Boolean {
        val granted: Set<String> =
            client.permissionController.getGrantedPermissions()

        return granted.containsAll(PERMISSIONS)
    }

    // --- Today steps ---
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

    // --- Today calories (kcal) ---
    suspend fun readTodayCalories(client: HealthConnectClient): Int {
        val now = ZonedDateTime.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    startOfDay.toInstant(),
                    now.toInstant()
                )
            )
        )

        val energy: Energy =
            response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                ?: Energy.kilocalories(0.0)

        return energy.inKilocalories.roundToInt()
    }

    // --- Weekly steps ---
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

    // --- Weekly calories (kcal) ---
    suspend fun readWeeklyCalories(client: HealthConnectClient): List<Int> {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val startOfWeek = now.minusDays(((now.dayOfWeek.value + 6) % 7).toLong())

        val result = mutableListOf<Int>()
        for (i in 0 until 7) {
            val dayStart = startOfWeek.plusDays(i.toLong())
            val dayEnd = dayStart.plusDays(1)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        dayStart.toInstant(),
                        dayEnd.toInstant()
                    )
                )
            )
            val energy: Energy =
                response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                    ?: Energy.kilocalories(0.0)
            result.add(energy.inKilocalories.roundToInt())
        }
        return result
    }

    fun weekDayLabels(): List<String> =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
}
