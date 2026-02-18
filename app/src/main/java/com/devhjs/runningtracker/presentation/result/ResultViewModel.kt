package com.devhjs.runningtracker.presentation.result

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.MapUtils
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.domain.repository.MainRepository
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


    init {
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

    private fun generatePolylineBitmap(): Bitmap {
        val pathPoints = _state.value.pathPoints
        val width = 800
        val height = 800
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        
        // Background
        canvas.drawColor(Color.BLACK)
        
        if (pathPoints.isEmpty() || pathPoints.flatten().isEmpty()) return bitmap

        val paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val allPoints = pathPoints.flatten()
        val minLat = allPoints.minOf { it.latitude }
        val maxLat = allPoints.maxOf { it.latitude }
        val minLng = allPoints.minOf { it.longitude }
        val maxLng = allPoints.maxOf { it.longitude }

        val latDiff = maxLat - minLat
        val lngDiff = maxLng - minLng

        val padding = 50f
        val scaleX = (width - padding * 2) / lngDiff
        val scaleY = (height - padding * 2) / latDiff

        val scale = minOf(scaleX, scaleY)

        pathPoints.forEach { polyline ->
            val path = Path()
            if (polyline.isNotEmpty()) {
                val startPoint = polyline[0]
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
