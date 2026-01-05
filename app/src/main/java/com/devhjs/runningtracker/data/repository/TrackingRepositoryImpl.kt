package com.devhjs.runningtracker.data.repository

import android.location.Location
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import com.devhjs.runningtracker.service.Polyline
import com.devhjs.runningtracker.service.Polylines
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor() : TrackingRepository {

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<Polylines>(mutableListOf())
    override val pathPoints: StateFlow<Polylines> = _pathPoints.asStateFlow()

    private val _timeRunInMillis = MutableStateFlow(0L)
    override val timeRunInMillis: StateFlow<Long> = _timeRunInMillis.asStateFlow()

    override suspend fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    override suspend fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            _pathPoints.update { currentPoints ->
                val newPoints = currentPoints.toMutableList()
                if (newPoints.isEmpty()) {
                    newPoints.add(mutableListOf())
                }
                newPoints.last().add(pos)
                newPoints
            }
        }
    }

    override suspend fun addEmptyPolyline() {
        _pathPoints.update { currentPoints ->
            val newPoints = currentPoints.toMutableList()
            newPoints.add(mutableListOf())
            newPoints
        }
    }

    override suspend fun updateTimeRunInMillis(time: Long) {
        _timeRunInMillis.value = time
    }
    
    override suspend fun clearData() {
        _isTracking.value = false
        _pathPoints.value = mutableListOf()
        _timeRunInMillis.value = 0L
    }
}
