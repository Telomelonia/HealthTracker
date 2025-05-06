package com.caresync.healthtracker.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.caresync.healthtracker.repository.HealthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val healthRepository: HealthRepository
) {
  private val healthConnectClient = HealthConnectClient.getOrCreate(context)

  fun createPermissionRequestContract() = PermissionController.createRequestPermissionResultContract()

  suspend fun checkAndRequestPermissions() {
    if (!healthRepository.hasAllPermissions()) {
      // Permissions need to be requested
      return
    }
  }
}