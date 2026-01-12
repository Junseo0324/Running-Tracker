package com.devhjs.runningtracker.core.di


import com.devhjs.runningtracker.data.datasource.TempRunDataSourceImpl
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource
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
    abstract fun bindTempRunDataSource(
        tempRunDataSourceImpl: TempRunDataSourceImpl
    ): TempRunDataSource
}
