package com.devhjs.runningtracker.core.di

import com.devhjs.runningtracker.data.repository.MainRepositoryImpl
import com.devhjs.runningtracker.data.repository.TrackingRepositoryImpl
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindMainRepository(
        mainRepositoryImpl: MainRepositoryImpl
    ): MainRepository

    @Singleton
    @Binds
    abstract fun bindTrackingRepository(
        trackingRepositoryImpl: TrackingRepositoryImpl
    ): TrackingRepository

}
