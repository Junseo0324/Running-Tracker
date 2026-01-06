package com.devhjs.runningtracker.domain.repository

import android.location.Location
import com.devhjs.runningtracker.service.Polylines
import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val isTracking: StateFlow<Boolean>
    val pathPoints: StateFlow<Polylines>
    val timeRunInMillis: StateFlow<Long>
    
    suspend fun setIsTracking(isTracking: Boolean)
    suspend fun addPathPoint(location: Location?)
    suspend fun addEmptyPolyline()
    suspend fun updateTimeRunInMillis(time: Long)
    suspend fun clearData()
    suspend fun persistState()
    suspend fun restoreState()
}
