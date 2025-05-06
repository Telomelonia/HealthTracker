package com.caresync.healthtracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caresync.healthtracker.repository.HealthRepository
import com.caresync.healthtracker.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
class HealthDataSyncWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val healthRepository: HealthRepository,
  private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {

  private val TAG = "HealthDataSyncWorker"

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    try {
      Log.d(TAG, "Starting health data sync")

      // Check permissions first
      if (!healthRepository.hasAllPermissions()) {
        Log.w(TAG, "Missing health permissions, cannot sync")
        return@withContext Result.failure()
      }

      // Collect health data
      val healthData = healthRepository.readHealthData()
        .catch { e ->
          Log.e(TAG, "Error reading health data", e)
          null
        }
        .firstOrNull()

      if (healthData == null) {
        Log.w(TAG, "No health data available to sync")
        return@withContext Result.failure()
      }

      // Upload to backend
      val result = syncRepository.uploadHealthData(healthData)

      return@withContext if (result) {
        Log.d(TAG, "Health data sync successful")
        Result.success()
      } else {
        Log.w(TAG, "Health data sync failed, will retry")
        Result.retry()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error in health data sync", e)
      Result.failure()
    }
  }
}