package com.devhjs.runningtracker.presentation.result

import androidx.compose.runtime.Immutable
import com.devhjs.runningtracker.service.Polylines


@Immutable
data class ResultState(
    val distanceInMeters: Float = 0f,
    val timeInMillis: Long = 0L,
    val avgSpeed: Float = 0f,
    val caloriesBurned: Int = 0,
    val pathPoints: Polylines = mutableListOf()
)
