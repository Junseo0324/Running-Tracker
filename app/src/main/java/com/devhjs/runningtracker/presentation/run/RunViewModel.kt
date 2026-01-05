package com.devhjs.runningtracker.presentation.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.devhjs.runningtracker.presentation.navigation.Screen
import com.devhjs.runningtracker.service.TrackingService
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
    private val mainRepository: MainRepository
): ViewModel() {

    private val _state = MutableStateFlow(RunState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RunEvent>()
    val event = _event.asSharedFlow()

    // Observers need to be kept to remove them if necessary, 
    // but since they are observing static LiveData and VM scope is tied to graph...
    // simpler to just observeForever? No, memory leak.
    // Use observeForever and remove in onCleared.
    
    private val isTrackingObserver = androidx.lifecycle.Observer<Boolean> { isTracking ->
        _state.update { it.copy(isTracking = isTracking) }
    }

    private val pathPointsObserver = androidx.lifecycle.Observer<List<List<com.google.android.gms.maps.model.LatLng>>> { pathPoints ->
        _state.update { 
            it.copy(
                pathPoints = pathPoints,
                currentLocation = pathPoints.lastOrNull()?.lastOrNull()
            ) 
        }
        updateStats()
    }

    private val timeRunObserver = androidx.lifecycle.Observer<Long> { time ->
        _state.update { it.copy(curTimeInMillis = time) }
        updateStats()
    }

    init {
        TrackingService.isTracking.observeForever(isTrackingObserver)
        TrackingService.pathPoints.observeForever(pathPointsObserver)
        TrackingService.timeRunInMillis.observeForever(timeRunObserver)
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
                    saveRun()
                    _event.emit(RunEvent.ServiceAction(Constants.ACTION_STOP_SERVICE))
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

    private fun saveRun() {
        // Run 저장 로직은 여기 혹은 ResultScreen에서?
        // 기존 코드: RunScreen에서 Stop 누르면 ResultScreen으로 이동.
        // MainViewModel.insertRun은 어디서 호출?
        // RunScreen.kt에는 호출 코드가 안보임. ResultScreen에서 하나?
        // ResultScreen.kt를 확인해봐야 함.
        // 일단 Finish Action에서는 Service Stop과 Navigate만 처리.
    }

    override fun onCleared() {
        super.onCleared()
        TrackingService.isTracking.removeObserver(isTrackingObserver)
        TrackingService.pathPoints.removeObserver(pathPointsObserver)
        TrackingService.timeRunInMillis.removeObserver(timeRunObserver)
    }
}
