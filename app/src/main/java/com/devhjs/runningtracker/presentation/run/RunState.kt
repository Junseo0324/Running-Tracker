package com.devhjs.runningtracker.presentation.run

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng

@Immutable
data class RunState(
    val isTracking: Boolean = false,
    val pathPoints: List<List<LatLng>> = emptyList(),
    val curTimeInMillis: Long = 0L,
    val isLocked: Boolean = false,
    val distanceInMeters: Float = 0f,
    val caloriesBurned: Int = 0,
    val avgSpeed: Float = 0f,
    val currentLocation: LatLng? = null,
    val isGpsEnabled: Boolean = true
)
