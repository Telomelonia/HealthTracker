package com.caresync.healthtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthTrackerApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    // Add crash logging
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
      println("Uncaught exception in thread ${thread.name}")
      exception.printStackTrace()
    }
  }
}