package com.devhjs.runningtracker.presentation.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.MapUtils
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class RunViewModel @Inject constructor(
    private val runningManager: RunningManager
): ViewModel() {

    private val _state = MutableStateFlow(RunState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RunEvent>()
    val event = _event.asSharedFlow()

    init {
        // 현재 러닝 여부 추적
        viewModelScope.launch {
            runningManager.isTracking.collect { isTracking ->
                 _state.update { it.copy(isTracking = isTracking) }
            }
        }

        // 이동 경로 좌표
        viewModelScope.launch {
            runningManager.pathPoints.collect { pathPoints ->
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

        // 러닝 진행 시간 추적
        viewModelScope.launch {
            runningManager.durationInMillis.collect { time ->
                 _state.update { it.copy(curTimeInMillis = time) }
                updateStats()               
            }
        }

        // GPS 활성 여부 추적
        viewModelScope.launch {
            runningManager.isGpsEnabled.collect { isGpsEnabled ->
                _state.update { it.copy(isGpsEnabled = isGpsEnabled) }
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

    // 통계 계산 (거리, 평균 속도, 칼로리) 계산
    private fun updateStats() {
        val pathPoints = _state.value.pathPoints
        val curTimeInMillis = _state.value.curTimeInMillis
        
        val distanceInMeters = if (pathPoints.isNotEmpty()) {
            pathPoints.fold(0f) { acc, polyline ->
                acc + MapUtils.calculatePolylineLength(polyline)
            }
        } else 0f
        
        val avgSpeed = if (curTimeInMillis > 0L) {
             ((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 3600f) * 10).roundToInt() / 10f
        } else {
            0f
        }

        // 칼로리 : 거리 x 60 으로 처리
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




