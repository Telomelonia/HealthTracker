package com.caresync.healthtracker.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor() {
  val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  companion object {
    const val GRAPHQL_ENDPOINT = "http://10.0.2.2:3000/graphql"
    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
  }
}