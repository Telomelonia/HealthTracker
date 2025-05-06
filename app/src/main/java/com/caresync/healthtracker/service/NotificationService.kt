package com.caresync.healthtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.caresync.healthtracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
  @ApplicationContext private val context: Context
) {
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      CHANNEL_NAME,
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = CHANNEL_DESCRIPTION
    }
    notificationManager.createNotificationChannel(channel)
  }

  fun showAnxietyAlert(level: String, score: Double) {
    if (score > 0.6) { // Only alert for High or Very High anxiety
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Anxiety Check")
        .setContentText("Your anxiety level is $level. Are you feeling okay?")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

      notificationManager.notify(NOTIFICATION_ID, notification)
    }
  }

  companion object {
    private const val CHANNEL_ID = "anxiety_alerts"
    private const val CHANNEL_NAME = "Anxiety Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for elevated anxiety levels"
    private const val NOTIFICATION_ID = 101
  }
}