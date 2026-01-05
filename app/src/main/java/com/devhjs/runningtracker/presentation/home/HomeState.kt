package com.devhjs.runningtracker.presentation.home

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng

@Stable
data class HomeState(
    val isPermissionGranted: Boolean = false,
    val currentLocation: LatLng? = null,
    val isLocationLoading: Boolean = false
)
