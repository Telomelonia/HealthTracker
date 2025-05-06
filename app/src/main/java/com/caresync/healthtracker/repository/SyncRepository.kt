package com.caresync.healthtracker.repository

import android.util.Log
import com.caresync.healthtracker.data.HealthData
import com.caresync.healthtracker.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class SyncRepository @Inject constructor(
  private val apiClient: ApiClient
) {
  private val TAG = "SyncRepository"

  suspend fun uploadHealthData(healthData: HealthData): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        val requestJson = JSONObject().apply {
          put("query", CREATE_HEALTH_METRIC_MUTATION)
          put("variables", JSONObject().apply {
            put("userId", "1") // Replace with actual user ID
            healthData.heartRate?.let { put("heartRate", it) }
            healthData.steps?.let { put("steps", it) }
            healthData.sleepHours?.let { put("sleepHours", it) }
            put("recordedAt", Instant.ofEpochMilli(healthData.recordedAt).toString())
          })
        }

        val request = okhttp3.Request.Builder()
          .url(ApiClient.GRAPHQL_ENDPOINT)
          .post(requestJson.toString().toRequestBody(ApiClient.JSON_MEDIA_TYPE))
          .build()

        val response = apiClient.okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        Log.d(TAG, "Upload response: $responseBody")

        response.isSuccessful && !responseBody.isNullOrEmpty() &&
            !JSONObject(responseBody).has("errors")
      } catch (e: Exception) {
        Log.e(TAG, "Error uploading health data", e)
        false
      }
    }
  }

  suspend fun batchUploadHealthData(healthDataList: List<HealthData>): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        val metricsArray = JSONArray()
        healthDataList.forEach { data ->
          metricsArray.put(JSONObject().apply {
            data.heartRate?.let { put("heartRate", it) }
            data.steps?.let { put("steps", it) }
            data.sleepHours?.let { put("sleepHours", it) }
            put("recordedAt", Instant.ofEpochMilli(data.recordedAt).toString())
          })
        }

        val requestJson = JSONObject().apply {
          put("query", BATCH_CREATE_HEALTH_METRICS_MUTATION)
          put("variables", JSONObject().apply {
            put("userId", "1") // Replace with actual user ID
            put("metrics", metricsArray)
          })
        }

        val request = okhttp3.Request.Builder()
          .url(ApiClient.GRAPHQL_ENDPOINT)
          .post(requestJson.toString().toRequestBody(ApiClient.JSON_MEDIA_TYPE))
          .build()

        val response = apiClient.okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        Log.d(TAG, "Batch upload response: $responseBody")

        response.isSuccessful && !responseBody.isNullOrEmpty() &&
            !JSONObject(responseBody).has("errors")
      } catch (e: Exception) {
        Log.e(TAG, "Error batch uploading health data", e)
        false
      }
    }
  }

  companion object {
    private const val CREATE_HEALTH_METRIC_MUTATION = """
            mutation CreateHealthMetric(${'$'}userId: ID!, ${'$'}heartRate: Float, ${'$'}steps: Int, ${'$'}sleepHours: Float, ${'$'}recordedAt: String!) {
                createHealthMetric(
                    userId: ${'$'}userId, 
                    heartRate: ${'$'}heartRate, 
                    steps: ${'$'}steps, 
                    sleepHours: ${'$'}sleepHours, 
                    recordedAt: ${'$'}recordedAt
                ) {
                    healthMetric {
                        id
                        heartRate
                        steps
                        sleepHours
                        anxietyScore
                        anxietyLevel
                        recordedAt
                    }
                    errors
                }
            }
        """

    private const val BATCH_CREATE_HEALTH_METRICS_MUTATION = """
            mutation BatchCreateHealthMetrics(${'$'}userId: ID!, ${'$'}metrics: [HealthMetricInput!]!) {
                batchCreateHealthMetrics(
                    userId: ${'$'}userId, 
                    metrics: ${'$'}metrics
                ) {
                    healthMetrics {
                        id
                        heartRate
                        steps
                        sleepHours
                        anxietyScore
                        anxietyLevel
                        recordedAt
                    }
                    successCount
                    errors
                }
            }
        """
  }
}