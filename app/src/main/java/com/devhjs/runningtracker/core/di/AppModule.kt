package com.devhjs.runningtracker.core.di

import android.content.Context
import androidx.room.Room
import com.devhjs.runningtracker.core.Constants.RUNNING_DATABASE_NAME
import com.devhjs.runningtracker.data.local.RunningDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(app)

    @Singleton
    @Provides
    fun provideLocationRepository(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): com.devhjs.runningtracker.domain.repository.LocationRepository {
        return com.devhjs.runningtracker.data.repository.LocationRepositoryImpl(client)
    }
}
