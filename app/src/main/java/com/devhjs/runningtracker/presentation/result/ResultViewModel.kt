package com.devhjs.runningtracker.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.MapUtils
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.presentation.navigation.Screen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    val mainRepository: MainRepository,
    private val runningManager: RunningManager
) : ViewModel() {

    private val _state = MutableStateFlow(ResultState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<ResultEvent>()
    val event = _event.asSharedFlow()

    // Assuming ResultScreen is navigated to AFTER tracking is done or just before saving.
    // However, if TrackingService is still running, we can read values from it.
    // If ResultScreen is shown, typically tracking is PAUSED or implicitly STOPPED capturing but service alive.
    
    init {
        // Observe Repository Data to populate ResultState
        viewModelScope.launch {
            runningManager.pathPoints.collect { pathPoints ->
                _state.update { it.copy(pathPoints = pathPoints) }
                calculateStats()
            }
        }
        
        viewModelScope.launch {
            runningManager.durationInMillis.collect { timeInMillis ->
                _state.update { it.copy(timeInMillis = timeInMillis) }
                calculateStats()
            }
        }
    }
    
    private fun calculateStats() {
        val pathPoints = _state.value.pathPoints
        var distanceInMeters = 0f
        if (pathPoints.isNotEmpty()) {
            pathPoints.forEach { polyline ->
                distanceInMeters += MapUtils.calculatePolylineLength(polyline)
            }
        }
        
        val curTimeInMillis = _state.value.timeInMillis
        val avgSpeed = if (curTimeInMillis > 0 && distanceInMeters > 0) {
            (distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 3600f)
        } else {
            0f
        }
        
        val caloriesBurned = ((distanceInMeters / 1000f) * 60).toInt()
        
        _state.update { 
            it.copy(
                distanceInMeters = distanceInMeters,
                avgSpeed = avgSpeed,
                caloriesBurned = caloriesBurned
            )
        }
    }

    fun onAction(action: ResultAction) {
        when(action) {
            ResultAction.OnDiscardClick -> {
                viewModelScope.launch {
                    _event.emit(ResultEvent.StopService(Constants.ACTION_STOP_SERVICE))
                    _event.emit(ResultEvent.Navigate(Screen.HomeScreen.route))
                }
            }
            ResultAction.OnSaveClick -> {
                saveRun()
            }
        }
    }

    private fun saveRun() {
        val currentState = _state.value
        val mapBitmap = generatePolylineBitmap()
        val timestamp = Calendar.getInstance().timeInMillis
        
        val run = Run(
            timestamp = timestamp,
            avgSpeedInKMH = currentState.avgSpeed,
            distanceInMeters = currentState.distanceInMeters.toInt(),
            timeInMillis = currentState.timeInMillis,
            caloriesBurned = currentState.caloriesBurned,
            img = ImageUtils.bitmapToBytes(mapBitmap)
        )
        
        viewModelScope.launch {
            mainRepository.insertRun(run)
            _event.emit(ResultEvent.StopService(Constants.ACTION_STOP_SERVICE))
            _event.emit(ResultEvent.Navigate(Screen.HomeScreen.route))
        }
    }

    private fun generatePolylineBitmap(): android.graphics.Bitmap {
        val pathPoints = _state.value.pathPoints
        val width = 800
        val height = 800
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Background
        canvas.drawColor(android.graphics.Color.BLACK) // Or RunningBlack if available, but pure black is fine for "dark mode" look
        
        if (pathPoints.isEmpty() || pathPoints.flatten().isEmpty()) return bitmap

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GREEN // RunningGreen equivalent
            strokeWidth = 10f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }

        val allPoints = pathPoints.flatten()
        val minLat = allPoints.minOf { it.latitude }
        val maxLat = allPoints.maxOf { it.latitude }
        val minLng = allPoints.minOf { it.longitude }
        val maxLng = allPoints.maxOf { it.longitude }

        val latDiff = maxLat - minLat
        val lngDiff = maxLng - minLng
        val maxDiff = maxOf(latDiff, lngDiff)
        
        // Add some padding
        val padding = 50f
        val scaleX = (width - padding * 2) / lngDiff
        val scaleY = (height - padding * 2) / latDiff
        
        // We want to maintain aspect ratio, so use the smaller scale, 
        // OR just normalize to the box. 
        // Using geographic coordinates directly on a flat canvas is a simple projection (Equirectangular).
        // For small distances (running) it's acceptable.
        
        // To properly center and scale:
        val scale = minOf(scaleX, scaleY)

        pathPoints.forEach { polyline ->
            val path = android.graphics.Path()
            if (polyline.isNotEmpty()) {
                val startPoint = polyline[0]
                // Normalize and scale:
                // x = (lng - minLng) * scale + padding
                // y = height - ((lat - minLat) * scale + padding)   (because canvas Y is down, lat Y is up)
                
                // Centering adjustment if aspects differ
                val offsetX = (width - (lngDiff * scale)) / 2
                val offsetY = (height - (latDiff * scale)) / 2

                var startX = ((startPoint.longitude - minLng) * scale + offsetX).toFloat()
                var startY = (height - ((startPoint.latitude - minLat) * scale + offsetY)).toFloat()
                
                path.moveTo(startX, startY)
                
                for (i in 1 until polyline.size) {
                    val p = polyline[i]
                     val x = ((p.longitude - minLng) * scale + offsetX).toFloat()
                     val y = (height - ((p.latitude - minLat) * scale + offsetY)).toFloat()
                     path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }
}
