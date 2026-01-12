package com.devhjs.runningtracker.core.di

import com.devhjs.runningtracker.BuildConfig
import com.devhjs.runningtracker.data.location.DefaultGpsStatusClient
import com.devhjs.runningtracker.data.location.DefaultLocationClient
import com.devhjs.runningtracker.data.location.MockGpsStatusClient
import com.devhjs.runningtracker.data.location.MockLocationClient
import com.devhjs.runningtracker.domain.location.GpsStatusClient
import com.devhjs.runningtracker.domain.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Singleton
    @Provides
    fun provideLocationClient(
        mockClient: MockLocationClient,
        defaultClient: DefaultLocationClient
    ): LocationClient {
        return if (BuildConfig.FLAVOR.contains("dev")) {
            mockClient
        } else {
            defaultClient
        }
    }

    @Singleton
    @Provides
    fun provideGpsStatusClient(
        mockClient: MockGpsStatusClient,
        defaultClient: DefaultGpsStatusClient
    ): GpsStatusClient {
        return if (BuildConfig.FLAVOR.contains("dev")) {
            mockClient
        } else {
            defaultClient
        }
    }
}
