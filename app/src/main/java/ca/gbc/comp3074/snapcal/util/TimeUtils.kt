package ca.gbc.comp3074.snapcal.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*

@RequiresApi(Build.VERSION_CODES.O)
fun startOfDayMillis(epochMillis: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val dt = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().atStartOfDay(zone)
    return dt.toInstant().toEpochMilli()
}

@RequiresApi(Build.VERSION_CODES.O)
fun endOfDayMillisExclusive(epochMillis: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val tomorrowStart = Instant.ofEpochMilli(startOfDayMillis(epochMillis))
        .atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone)
    return tomorrowStart.toInstant().toEpochMilli()
}
