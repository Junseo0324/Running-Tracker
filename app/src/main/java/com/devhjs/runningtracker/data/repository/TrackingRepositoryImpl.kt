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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : TrackingRepository {

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
            context.openFileOutput(TEMP_RUN_FILE, android.content.Context.MODE_PRIVATE).use {
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

    @kotlinx.serialization.Serializable
    private data class TempRunState(
        val timeInMillis: Long,
        val pathPoints: List<List<SerializableLatLng>>
    )
    
    @kotlinx.serialization.Serializable
    private data class SerializableLatLng(
        val lat: Double,
        val lng: Double
    )
}
