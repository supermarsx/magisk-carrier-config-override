package com.supermarsx.carrierconfig.instrumentation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.google.gson.Gson
import javax.inject.Singleton

/**
 * Instrumentation Module
 * 
 * Provides dependencies for Frida and LSPosed integration
 */
@Module
@InstallIn(SingletonComponent::class)
object InstrumentationModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
