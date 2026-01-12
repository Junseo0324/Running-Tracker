package com.devhjs.runningtracker.core.di

import com.devhjs.runningtracker.data.connectivity.GpsStatusDataSource
import com.devhjs.runningtracker.data.connectivity.GpsStatusDataSourceImpl
import com.devhjs.runningtracker.data.datasource.TempRunDataSource
import com.devhjs.runningtracker.data.datasource.TempRunDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun bindGpsStatusDataSource(
        gpsStatusDataSourceImpl: GpsStatusDataSourceImpl
    ): GpsStatusDataSource

    @Singleton
    @Binds
    abstract fun bindTempRunDataSource(
        tempRunDataSourceImpl: TempRunDataSourceImpl
    ): TempRunDataSource
}
