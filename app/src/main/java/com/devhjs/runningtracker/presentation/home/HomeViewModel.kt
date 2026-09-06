package com.devhjs.runningtracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.domain.location.LocationClient
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.presentation.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val runningManager: RunningManager
): ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()
    
    init {
        viewModelScope.launch {
            runningManager.isGpsEnabled.collect { isGpsEnabled ->
                _state.update { it.copy(isGpsEnabled = isGpsEnabled) }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when(action) {
            is HomeAction.OnPermissionsResult -> {
                _state.update { it.copy(isPermissionGranted = action.isGranted) }
                if(action.isGranted) {
                    fetchCurrentLocation()
                }
            }
            HomeAction.OnStartClick -> {
                viewModelScope.launch {
                    _event.emit(HomeEvent.Navigate(Screen.RunScreen.route))
                }
            }
            HomeAction.OnHistoryClick -> {
                viewModelScope.launch {
                    _event.emit(HomeEvent.Navigate(Screen.RunHistoryScreen.route))
                }
            }
        }
    }

    private var locationJob: Job? = null

    /**
     * 현재 위치 수집을 시작합니다.
     *
     * 이전 수집 작업을 반드시 취소합니다. 취소하지 않으면 홈 화면에 재진입할 때마다
     * (컴포저블이 파괴됐다 다시 만들어지며 LaunchedEffect 가 재실행된다)
     * 1Hz 고정확도 위치 스트림이 viewModelScope 에 하나씩 영구히 쌓인다.
     */
    private fun fetchCurrentLocation() {
        if(!_state.value.isPermissionGranted) return

        locationJob?.cancel()

        _state.update { it.copy(isLocationLoading = true) }
        locationJob = viewModelScope.launch {
            try {
                locationClient.getLocationFlow().collect { location ->
                    _state.update { state -> 
                        state.copy(
                            currentLocation = LatLng(location.latitude, location.longitude),
                            isLocationLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                 _state.update { it.copy(isLocationLoading = false) }
            }
        }
    }
}
