package com.devhjs.runningtracker.domain.location

import kotlinx.coroutines.flow.Flow

interface GpsStatusClient {
    fun getGpsStatusFlow(): Flow<Boolean>
}
