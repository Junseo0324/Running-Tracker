package com.devhjs.runningtracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.runningtracker.domain.repository.LocationRepository
import com.devhjs.runningtracker.presentation.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.devhjs.runningtracker.domain.repository.TrackingRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val trackingRepository: TrackingRepository
): ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()
    
    init {
        viewModelScope.launch {
            trackingRepository.isGpsEnabled.collect { isGpsEnabled ->
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

    private fun fetchCurrentLocation() {
        if(!_state.value.isPermissionGranted) return
        
        _state.update { it.copy(isLocationLoading = true) }
        viewModelScope.launch {
            try {
                locationRepository.getLocationFlow().collect { location ->
                    _state.update { state -> 
                        state.copy(
                            currentLocation = LatLng(location.latitude, location.longitude),
                            isLocationLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                 _state.update { it.copy(isLocationLoading = false) }
            }
        }
    }
}
