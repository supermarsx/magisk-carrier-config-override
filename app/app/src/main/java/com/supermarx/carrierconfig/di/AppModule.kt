package com.supermarx.carrierconfig.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.supermarx.carrierconfig.data.repository.DeviceRepository
import javax.inject.Singleton

/**
 * Hilt dependency injection module for the app
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context
    ): DeviceRepository {
        return DeviceRepository(context)
    }
}
