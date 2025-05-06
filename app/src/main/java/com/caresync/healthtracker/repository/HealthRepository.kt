package com.caresync.healthtracker.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.caresync.healthtracker.data.HealthData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
  @ApplicationContext private val context: Context
) {
  private val healthConnectClient by lazy {
    HealthConnectClient.getOrCreate(context)
  }

  // Define required permissions
  val permissions = setOf(
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(SleepSessionRecord::class)
  )

  // Check if Health Connect is available
  fun isHealthConnectAvailable(): Boolean {
    return HealthConnectClient.isProviderAvailable(context)
  }

  // Check if permissions are granted
  suspend fun hasAllPermissions(): Boolean {
    val granted = healthConnectClient.permissionController.getGrantedPermissions()
    return permissions.all { it in granted }
  }

  // Read health data for the last 24 hours
  fun readHealthData(): Flow<HealthData> = flow {
    val endTime = Instant.now()
    val startTime = endTime.minus(1, ChronoUnit.DAYS)

    val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

    // Read heart rate
    val heartRateRequest = ReadRecordsRequest(
      recordType = HeartRateRecord::class,
      timeRangeFilter = timeRangeFilter
    )
    val heartRateRecords = healthConnectClient.readRecords(heartRateRequest)
    val heartRateValues = heartRateRecords.records
      .flatMap { it.samples }
      .map { it.beatsPerMinute.toDouble() } // Convert Long to Double
    val avgHeartRate = if (heartRateValues.isNotEmpty()) {
      heartRateValues.sum() / heartRateValues.size
    } else {
      null
    }
    // Read steps
    val stepsRequest = ReadRecordsRequest(
      recordType = StepsRecord::class,
      timeRangeFilter = timeRangeFilter
    )
    val stepsRecords = healthConnectClient.readRecords(stepsRequest)
    val totalSteps = stepsRecords.records.sumOf { it.count.toInt() }

    // Read sleep
    val sleepRequest = ReadRecordsRequest(
      recordType = SleepSessionRecord::class,
      timeRangeFilter = timeRangeFilter
    )
    val sleepRecords = healthConnectClient.readRecords(sleepRequest)
    val totalSleepHours = sleepRecords.records
      .sumOf {
        val duration = java.time.Duration.between(it.startTime, it.endTime)
        duration.toMinutes() / 60.0
      }

    emit(HealthData(
      heartRate = avgHeartRate,
      steps = totalSteps,
      sleepHours = totalSleepHours
    ))
  }
}