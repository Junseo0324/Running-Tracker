package com.devhjs.runningtracker.data.connectivity

import kotlinx.coroutines.flow.Flow

interface GpsStatusDataSource {
    fun getGpsStatusFlow(): Flow<Boolean>
}
