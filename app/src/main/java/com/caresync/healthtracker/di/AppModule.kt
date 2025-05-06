package com.caresync.healthtracker.di

import android.content.Context
import androidx.work.WorkManager
import com.caresync.healthtracker.network.ApiClient
import com.caresync.healthtracker.repository.HealthRepository
import com.caresync.healthtracker.repository.SyncRepository
import com.caresync.healthtracker.service.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideHealthRepository(
    @ApplicationContext context: Context
  ): HealthRepository {
    return HealthRepository(context)
  }

  @Provides
  @Singleton
  fun provideApiClient(): ApiClient {
    return ApiClient()
  }

  @Provides
  @Singleton
  fun provideSyncRepository(apiClient: ApiClient): SyncRepository {
    return SyncRepository(apiClient)
  }

  @Provides
  @Singleton
  fun provideNotificationService(
    @ApplicationContext context: Context
  ): NotificationService {
    return NotificationService(context)
  }

  @Provides
  @Singleton
  fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
    return WorkManager.getInstance(context)
  }
}