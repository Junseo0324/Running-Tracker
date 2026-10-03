package com.devhjs.runningtracker.core.di

import com.devhjs.runningtracker.data.region.DefaultCountryProvider
import com.devhjs.runningtracker.domain.region.CountryProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RegionModule {

    @Singleton
    @Binds
    abstract fun bindCountryProvider(
        defaultCountryProvider: DefaultCountryProvider
    ): CountryProvider
}
