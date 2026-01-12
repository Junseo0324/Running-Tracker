package com.devhjs.runningtracker.core.di

import com.devhjs.runningtracker.data.manager.RunningManagerImpl
import com.devhjs.runningtracker.domain.manager.RunningManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Singleton
    @Binds
    abstract fun bindRunningManager(
        runningManagerImpl: RunningManagerImpl
    ): RunningManager
}
