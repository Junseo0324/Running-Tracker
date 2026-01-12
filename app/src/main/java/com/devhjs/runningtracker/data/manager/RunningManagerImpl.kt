package com.devhjs.runningtracker.data.manager

import android.location.Location
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource
import com.devhjs.runningtracker.domain.location.GpsStatusClient
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.service.Polylines
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningManagerImpl @Inject constructor(
    gpsStatusClient: GpsStatusClient,
    private val tempRunDataSource: TempRunDataSource
) : RunningManager {

    // 매니저의 생명주기를 관리할 자체 Scope (Application Scope와 유사하게 동작)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<Polylines>(mutableListOf())
    override val pathPoints: StateFlow<Polylines> = _pathPoints.asStateFlow()

    private val _durationInMillis = MutableStateFlow(0L)
    override val durationInMillis: StateFlow<Long> = _durationInMillis.asStateFlow()

    override val isGpsEnabled: StateFlow<Boolean> = gpsStatusClient.getGpsStatusFlow()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override suspend fun startResumeRun() {
        _isTracking.value = true
    }

    override suspend fun pauseRun() {
        _isTracking.value = false
    }

    override suspend fun stopRun() {
        _isTracking.value = false
        _pathPoints.value = mutableListOf()
        _durationInMillis.value = 0L
        tempRunDataSource.clearData()
    }

    override suspend fun addLocation(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            _pathPoints.update { currentPoints ->
                val newPoints = currentPoints.map { it.toMutableList() }.toMutableList()
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

    override suspend fun updateDuration(timeInMillis: Long) {
        _durationInMillis.value = timeInMillis
    }

    override suspend fun persistState() {
        val serializablePathPoints = _pathPoints.value.map { polyline ->
            polyline.map { latLng -> SerializableLatLng(latLng.latitude, latLng.longitude) }
        }
        tempRunDataSource.persistState(_durationInMillis.value, serializablePathPoints)
    }

    override suspend fun restoreState() {
        tempRunDataSource.restoreState()?.let { state ->
            _durationInMillis.value = state.timeInMillis
            _pathPoints.value = state.pathPoints.map { polyline ->
                polyline.map { sLatLng -> LatLng(sLatLng.lat, sLatLng.lng) }.toMutableList()
            }.toMutableList()

            // 복구 시 데이터가 있다면 일시정지 상태나 다름 없지만 명시적으로 정지는 안함 (서비스 로직에 따름)
        }
    }
}
