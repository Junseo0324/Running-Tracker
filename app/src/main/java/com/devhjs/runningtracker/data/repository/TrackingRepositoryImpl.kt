package com.devhjs.runningtracker.data.repository

import android.content.Context
import android.location.Location
import com.devhjs.runningtracker.data.connectivity.GpsStatusDataSource
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import com.devhjs.runningtracker.service.Polylines
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gpsStatusDataSource: GpsStatusDataSource
) : TrackingRepository {

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<Polylines>(mutableListOf())
    override val pathPoints: StateFlow<Polylines> = _pathPoints.asStateFlow()

    private val _timeRunInMillis = MutableStateFlow(0L)
    override val timeRunInMillis: StateFlow<Long> = _timeRunInMillis.asStateFlow()
    
    private val _isGpsEnabled = MutableStateFlow(true)
    override val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    init {
        // GPS 상태 모니터링 시작
        // Repository는 싱글톤이므로 Application Scope에서 동작합니다.
        // GlobalScope보다는 독자적인 CoroutineScope를 주입받는 것이 좋지만,
        // 여기서는 간단히 Main Dispatcher를 가진 Scope를 생성하여 처리하거나
        // hilt로 주입된 scope가 없다면 flow.stateIn() 패턴을 사용할 수도 있습니다.
        // 하지만 기존 구조를 유지하며 init에서 job을 실행합니다.
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            gpsStatusDataSource.getGpsStatusFlow().collect { isEnabled ->
                _isGpsEnabled.value = isEnabled
            }
        }
    }

    override suspend fun setGpsEnabled(isEnabled: Boolean) {
        // DataSource로부터 업데이트되므로 외부에서 수동 설정은 필요 없을 수 있으나,
        // 인터페이스 호환성을 위해 남겨두거나 빈 구현으로 둡니다.
        // 혹은 테스트 등을 위해 남겨둘 수 있습니다.
        _isGpsEnabled.value = isEnabled
    }

    override suspend fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

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
        try {
            context.deleteFile(TEMP_RUN_FILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun persistState() {
        try {
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

    override suspend fun restoreState() {
        try {
            val file = java.io.File(context.filesDir, TEMP_RUN_FILE)
            if (file.exists()) {
                 val jsonString = context.openFileInput(TEMP_RUN_FILE).bufferedReader().use { it.readText() }
                 val state = Json.decodeFromString<TempRunState>(jsonString)
                 
                 _timeRunInMillis.value = state.timeInMillis
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

    @Serializable
    private data class TempRunState(
        val timeInMillis: Long,
        val pathPoints: List<List<SerializableLatLng>>
    )
    
    @Serializable
    private data class SerializableLatLng(
        val lat: Double,
        val lng: Double
    )
}
