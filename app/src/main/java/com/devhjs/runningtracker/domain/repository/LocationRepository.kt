package com.devhjs.runningtracker.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationFlow(): Flow<Location>
}
