package com.caresync.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caresync.healthtracker.data.HealthData
import com.caresync.healthtracker.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
  private val healthRepository: HealthRepository
) : ViewModel() {

  private val _healthData = MutableStateFlow<HealthData?>(null)
  val healthData: StateFlow<HealthData?> = _healthData

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error

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
            _error.value = "Error collecting health data: ${e.message}"
          }
          .collect { data ->
            _healthData.value = data
          }
      } catch (e: Exception) {
        _error.value = "Unexpected error: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }
}
