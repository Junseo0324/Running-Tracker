package com.devhjs.runningtracker.presentation.run

import com.google.android.gms.maps.model.LatLng
import javax.annotation.concurrent.Immutable

@Immutable
data class RunState(
    val isTracking: Boolean = false,
    val pathPoints: List<List<LatLng>> = emptyList(),
    val curTimeInMillis: Long = 0L,
    val isLocked: Boolean = false,
    val distanceInMeters: Float = 0f,
    val caloriesBurned: Int = 0,
    val avgSpeed: Float = 0f,
    val currentLocation: LatLng? = null
)
