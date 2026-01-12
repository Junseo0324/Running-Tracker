package com.devhjs.runningtracker.data.repository

import android.content.Context
import android.location.Location
import com.devhjs.runningtracker.data.connectivity.GpsStatusDataSource
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import com.devhjs.runningtracker.service.Polylines
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실시간 러닝 데이터를 관리하는 저장소([TrackingRepository])의 구현체입니다.
 *
 * 이 클래스는 앱 전역에서 유일(Singleton)하게 유지되며, 다음과 같은 핵심 역할을 수행합니다:
 * 1. 현재 진행 중인 러닝의 상태(시간, 경로, 추적 여부)를 실시간으로 관리 및 [StateFlow]로 노출
 * 2. 시스템 GPS 상태 변화를 감지하고 전파
 * 3. 앱 프로세스 종료를 대비한 임시 데이터 저장 및 복구 (Persistence)
 */
@Singleton
class TrackingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gpsStatusDataSource: GpsStatusDataSource
) : TrackingRepository {

    // 현재 추적 중인지 여부 (true: 기록 중, false: 일시정지)
    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // 경로 좌표 리스트 (Polylines: 여러 개의 Polyline(좌표 리스트)의 리스트)
    // 일시정지 후 다시 시작하면 새로운 Polyline이 추가됩니다.
    private val _pathPoints = MutableStateFlow<Polylines>(mutableListOf())
    override val pathPoints: StateFlow<Polylines> = _pathPoints.asStateFlow()

    // 현재 러닝 진행 시간 (밀리초 단위)
    private val _timeRunInMillis = MutableStateFlow(0L)
    override val timeRunInMillis: StateFlow<Long> = _timeRunInMillis.asStateFlow()

    // GPS 활성화 여부
    private val _isGpsEnabled = MutableStateFlow(true)
    override val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    init {
        // [초기화] GPS 상태 데이터 소스를 구독합니다.
        // 이 Repository는 Singleton이므로 앱 생명주기 동안 계속 동작합니다.
        // GpsStatusDataSource로부터 실시간으로 GPS On/Off 상태를 받아 _isGpsEnabled 값을 업데이트합니다.
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            gpsStatusDataSource.getGpsStatusFlow().collect { isEnabled ->
                _isGpsEnabled.value = isEnabled
            }
        }
    }

    /**
     * GPS 상태를 수동으로 설정합니다.
     * (현재는 DataSource 구독을 통해 자동으로 관리되므로 주로 테스트나 호환성 용도로 사용됩니다.)
     */
    override suspend fun setGpsEnabled(isEnabled: Boolean) {
        _isGpsEnabled.value = isEnabled
    }

    /**
     * 추적 상태(시작/일시정지)를 변경합니다.
     */
    override suspend fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    /**
     * 새로운 위치 좌표를 경로에 추가합니다.
     * 현재 활성화된(마지막) Polyline에 좌표를 덧붙입니다.
     */
    override suspend fun addPathPoint(location: Location?) {
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
     * 빈 Polyline을 추가합니다.
     * 주로 러닝을 일시정지 했다가 다시 시작할 때, 이전 경로와 이어지지 않도록 끊어주는 역할을 합니다.
     */
    override suspend fun addEmptyPolyline() {
        _pathPoints.update { currentPoints ->
            val newPoints = currentPoints.toMutableList()
            newPoints.add(mutableListOf())
            newPoints
        }
    }

    /**
     * 러닝 시간을 업데이트합니다.
     */
    override suspend fun updateTimeRunInMillis(time: Long) {
        _timeRunInMillis.value = time
    }

    /**
     * 저장된 모든 임시 데이터를 초기화합니다. (러닝 종료 또는 취소 시 사용)
     * 내부 변수를 초기화하고 임시 파일도 삭제합니다.
     */
    override suspend fun clearData() {
        _isTracking.value = false
        _pathPoints.value = mutableListOf()
        _timeRunInMillis.value = 0L
        try {
            context.deleteFile(TEMP_RUN_FILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 현재 러닝 상태(시간, 경로)를 파일로 저장합니다.
     * 프로세스가 예기치 않게 종료되었을 때 데이터를 복구하기 위한 안전장치입니다.
     */
    override suspend fun persistState() {
        try {
            // LatLng 객체는 직렬화가 불가능하므로, SerializableLatLng로 변환하여 저장
            val serializablePathPoints = _pathPoints.value.map { polyline ->
                polyline.map { latLng -> SerializableLatLng(latLng.latitude, latLng.longitude) }
            }

            val state = TempRunState(
                timeInMillis = _timeRunInMillis.value,
                pathPoints = serializablePathPoints
            )
            val jsonString = Json.encodeToString(state)
            context.openFileOutput(TEMP_RUN_FILE, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 파일에 저장된 임시 러닝 상태를 복구합니다.
     * 앱 재실행 시 호출되어 이전 러닝 기록을 불러옵니다.
     */
    override suspend fun restoreState() {
        try {
            val file = java.io.File(context.filesDir, TEMP_RUN_FILE)
            if (file.exists()) {
                val jsonString =
                    context.openFileInput(TEMP_RUN_FILE).bufferedReader().use { it.readText() }
                val state = Json.decodeFromString<TempRunState>(jsonString)

                _timeRunInMillis.value = state.timeInMillis
                // SerializableLatLng -> LatLng로 다시 변환하여 복구
                _pathPoints.value = state.pathPoints.map { polyline ->
                    polyline.map { sLatLng -> LatLng(sLatLng.lat, sLatLng.lng) }.toMutableList()
                }.toMutableList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TEMP_RUN_FILE = "temp_run_state.json"
    }
}


