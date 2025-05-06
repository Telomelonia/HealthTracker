package com.caresync.healthtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.caresync.healthtracker.data.HealthData
import com.caresync.healthtracker.repository.HealthRepository
import com.caresync.healthtracker.repository.SyncRepository
import com.caresync.healthtracker.service.NotificationService
import com.caresync.healthtracker.utils.AnxietyPredictor
import com.caresync.healthtracker.workers.HealthDataSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
  private val healthRepository: HealthRepository,
  private val syncRepository: SyncRepository,
  private val notificationService: NotificationService,
  private val workManager: WorkManager
) : ViewModel() {
  private val TAG = "HealthViewModel"
  private val anxietyPredictor = AnxietyPredictor()

  private val _healthData = MutableStateFlow<HealthData?>(null)
  val healthData: StateFlow<HealthData?> = _healthData

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error

  private val _anxietyScore = MutableStateFlow<Double?>(null)
  val anxietyScore: StateFlow<Double?> = _anxietyScore

  private val _anxietyLevel = MutableStateFlow<String?>(null)
  val anxietyLevel: StateFlow<String?> = _anxietyLevel

  init {
    scheduleSyncWork()
  }

  fun checkHealthConnectAvailability(): Boolean {
    return healthRepository.isHealthConnectAvailable()
  }

  fun checkPermissions() {
    viewModelScope.launch {
      try {
        val hasPermissions = healthRepository.hasAllPermissions()
        if (!hasPermissions) {
          _error.value = "Please grant health permissions"
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error checking permissions", e)
        _error.value = "Error checking permissions: ${e.message}"
      }
    }
  }

  fun collectHealthData() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _error.value = null

        healthRepository.readHealthData()
          .catch { e ->
            Log.e(TAG, "Error collecting health data", e)
            _error.value = "Error collecting health data: ${e.message}"
          }
          .collect { data ->
            _healthData.value = data

            // Calculate anxiety score
            data?.let {
              val score = anxietyPredictor.predictAnxiety(it)
              _anxietyScore.value = score

              val level = anxietyPredictor.getAnxietyLevel(score)
              _anxietyLevel.value = level

              // Show notification if anxiety is high
              notificationService.showAnxietyAlert(level, score)

              // Upload to backend
              viewModelScope.launch {
                try {
                  val syncResult = syncRepository.uploadHealthData(it)
                  if (!syncResult) {
                    Log.w(TAG, "Failed to sync health data")
                  }
                } catch (e: Exception) {
                  Log.e(TAG, "Error syncing health data", e)
                }
              }
            }
          }
      } catch (e: Exception) {
        Log.e(TAG, "Unexpected error in health data collection", e)
        _error.value = "Unexpected error: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun scheduleSyncWork() {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val syncRequest = PeriodicWorkRequestBuilder<HealthDataSyncWorker>(
      15, TimeUnit.MINUTES
    )
      .setConstraints(constraints)
      .build()

    workManager.enqueueUniquePeriodicWork(
      "health_sync_work",
      ExistingPeriodicWorkPolicy.KEEP,
      syncRequest
    )
  }
}