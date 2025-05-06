package com.caresync.healthtracker.utils

import com.caresync.healthtracker.data.HealthData

class AnxietyPredictor {

  // Basic anxiety prediction based on health metrics
  fun predictAnxiety(healthData: HealthData): Double {
    var score = 0.0
    var factorsCount = 0

    // Heart rate analysis (weight: 0.4)
    if (healthData.heartRate != null) {
      if (healthData.heartRate > 80) {
        score += 0.4 * ((healthData.heartRate - 80) / 40.0).coerceIn(0.0, 1.0)
      }
      factorsCount++
    }

    // Activity level analysis (weight: 0.3)
    if (healthData.steps != null) {
      val dailyTarget = 8000
      if (healthData.steps < dailyTarget) {
        score += 0.3 * ((dailyTarget - healthData.steps) / dailyTarget.toDouble()).coerceIn(0.0, 1.0)
      }
      factorsCount++
    }

    // Sleep quality analysis (weight: 0.3)
    if (healthData.sleepHours != null) {
      val optimalSleep = 8.0
      if (healthData.sleepHours < optimalSleep) {
        score += 0.3 * ((optimalSleep - healthData.sleepHours) / optimalSleep).coerceIn(0.0, 1.0)
      }
      factorsCount++
    }

    return if (factorsCount > 0) score / factorsCount else 0.0
  }

  fun getAnxietyLevel(score: Double): String {
    return when(score) {
      in 0.0..0.2 -> "Normal"
      in 0.2..0.4 -> "Slightly Elevated"
      in 0.4..0.6 -> "Moderate"
      in 0.6..0.8 -> "High"
      else -> "Very High"
    }
  }
}