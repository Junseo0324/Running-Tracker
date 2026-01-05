package com.devhjs.runningtracker.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.presentation.navigation.Screen
import com.devhjs.runningtracker.service.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResultState())
    val state = _state.asStateFlow()

    private val _event = kotlinx.coroutines.flow.MutableSharedFlow<ResultEvent>()
    val event = _event.asSharedFlow()

    // Assuming ResultScreen is navigated to AFTER tracking is done or just before saving.
    // However, if TrackingService is still running, we can read values from it.
    // If ResultScreen is shown, typically tracking is PAUSED or implicitly STOPPED capturing but service alive.
    
    init {
        // Observe Service Data to populate ResultState
        TrackingService.pathPoints.observeForever { pathPoints ->
            _state.update { it.copy(pathPoints = pathPoints) }
            calculateStats()
        }
        
        TrackingService.timeRunInMillis.observeForever { timeInMillis ->
             _state.update { it.copy(timeInMillis = timeInMillis) }
            calculateStats()
        }
    }
    
    private fun calculateStats() {
        val pathPoints = _state.value.pathPoints
        var distanceInMeters = 0f
        if (pathPoints.isNotEmpty()) {
            pathPoints.forEach { polyline ->
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline)
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
        val timestamp = Calendar.getInstance().timeInMillis
        
        val run = Run(
            timestamp = timestamp,
            avgSpeedInKMH = currentState.avgSpeed,
            distanceInMeters = currentState.distanceInMeters.toInt(),
            timeInMillis = currentState.timeInMillis,
            caloriesBurned = currentState.caloriesBurned
        )
        
        viewModelScope.launch {
            mainRepository.insertRun(run)
            _event.emit(ResultEvent.StopService(Constants.ACTION_STOP_SERVICE))
            _event.emit(ResultEvent.Navigate(Screen.HomeScreen.route))
        }
    }
}
