package com.devhjs.runningtracker.presentation.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.presentation.navigation.Screen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Math.round
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val trackingRepository: com.devhjs.runningtracker.domain.repository.TrackingRepository
): ViewModel() {

    private val _state = MutableStateFlow(RunState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RunEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            trackingRepository.isTracking.collect { isTracking ->
                 _state.update { it.copy(isTracking = isTracking) }
            }
        }
        
        viewModelScope.launch {
            trackingRepository.pathPoints.collect { pathPoints ->
                 // Create a new list structure to ensure Compose detects changes
                val newPathPoints = pathPoints.map { it.toList() }
                _state.update { 
                    it.copy(
                        pathPoints = newPathPoints,
                        currentLocation = pathPoints.lastOrNull()?.lastOrNull()
                    ) 
                }
                updateStats()                
            }
        }
        
        viewModelScope.launch {
            trackingRepository.timeRunInMillis.collect { time ->
                 _state.update { it.copy(curTimeInMillis = time) }
                updateStats()               
            }
        }
    }

    fun onAction(action: RunAction) {
        when(action) {
            RunAction.OnToggleLock -> {
                _state.update { it.copy(isLocked = !it.isLocked) }
            }
            RunAction.OnResume -> {
                viewModelScope.launch {
                    _event.emit(RunEvent.ServiceAction(Constants.ACTION_START_OR_RESUME_SERVICE))
                }
            }
            RunAction.OnPause -> {
                viewModelScope.launch {
                    _event.emit(RunEvent.ServiceAction(Constants.ACTION_PAUSE_SERVICE))
                }
            }
            RunAction.OnFinish -> {
                viewModelScope.launch {
                    _event.emit(RunEvent.ServiceAction(Constants.ACTION_PAUSE_SERVICE))
                    _event.emit(RunEvent.Navigate(Screen.ResultScreen.route))
                }
            }
        }
    }

    private fun updateStats() {
        val pathPoints = _state.value.pathPoints
        val curTimeInMillis = _state.value.curTimeInMillis
        
        val distanceInMeters = if (pathPoints.isNotEmpty()) {
            pathPoints.fold(0f) { acc, polyline ->
                acc + TrackingUtility.calculatePolylineLength(polyline)
            }
        } else 0f
        
        val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 3600f) * 10) / 10f
        val caloriesBurned = ((distanceInMeters / 1000f) * 60).toInt()

        _state.update {
            it.copy(
                distanceInMeters = distanceInMeters,
                avgSpeed = if(avgSpeed.isNaN() || avgSpeed.isInfinite()) 0f else avgSpeed,
                caloriesBurned = caloriesBurned
            )
        }
    }
}




