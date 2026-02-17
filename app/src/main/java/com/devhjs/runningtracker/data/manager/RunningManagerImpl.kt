package com.devhjs.runningtracker.data.manager

import android.location.Location
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource
import com.devhjs.runningtracker.domain.location.GpsStatusClient
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.service.Polylines
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope

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
    private val tempRunDataSource: TempRunDataSource,
    externalScope: CoroutineScope
) : RunningManager {



    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<Polylines>(mutableListOf())
    override val pathPoints: StateFlow<Polylines> = _pathPoints.asStateFlow()

    private val _durationInMillis = MutableStateFlow(0L)
    override val durationInMillis: StateFlow<Long> = _durationInMillis.asStateFlow()

    /**
     * GPS 상태 감지
     * SharingStarted.Eagerly : 구독자가 없어도 항상 감시
     * stateIn : Flow -> StateFlow 로 변환해 UI 가 구독하기 편하게 변경
     */
    override val isGpsEnabled: StateFlow<Boolean> = gpsStatusClient.getGpsStatusFlow()
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override suspend fun startResumeRun() {
        _isTracking.value = true
    }

    override suspend fun pauseRun() {
        _isTracking.value = false
    }

    // 기록 종료 & 데이터 초기화
    override suspend fun stopRun() {
        _isTracking.value = false
        _pathPoints.value = mutableListOf()
        _durationInMillis.value = 0L
        tempRunDataSource.clearData()
    }

    // 경로 추가
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

    /**
     * 빈 경로 추가 - 선 끊기
     */
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


    // 현재까지의 기록을 임시 저장 (앱이 죽어도 복구할 수 있도록 처리)
    override suspend fun persistState() {
        val serializablePathPoints = _pathPoints.value.map { polyline ->
            polyline.map { latLng -> SerializableLatLng(latLng.latitude, latLng.longitude) }
        }
        tempRunDataSource.persistState(_durationInMillis.value, serializablePathPoints)
    }

    // 앱이 다시 켜질 경우 저장해준 데이터를 불러와 데이터를 넣음.
    override suspend fun restoreState() {
        tempRunDataSource.restoreState()?.let { state ->
            _durationInMillis.value = state.timeInMillis
            _pathPoints.value = state.pathPoints.map { polyline ->
                polyline.map { sLatLng -> LatLng(sLatLng.lat, sLatLng.lng) }.toMutableList()
            }.toMutableList()
        }
    }
}
