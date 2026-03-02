package com.supermarsx.carrierconfig.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt dependency injection module for the app.
 *
 * Repositories that carry @Inject constructor + @Singleton self-provide;
 * only add @Provides methods here for types that cannot use constructor
 * injection (interfaces, third-party classes, etc.).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
