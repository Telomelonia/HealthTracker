package com.caresync.healthtracker.data

import java.util.Date

data class HealthData(
  val heartRate: Double? = null,
  val steps: Int? = null,
  val sleepHours: Double? = null,
  val recordedAt: Long = System.currentTimeMillis()
)