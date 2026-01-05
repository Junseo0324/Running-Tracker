package com.devhjs.runningtracker.domain.model

data class Run(
    val id: Int? = null,
    val timestamp: Long,
    val avgSpeedInKMH: Float,
    val distanceInMeters: Int,
    val timeInMillis: Long,
    val caloriesBurned: Int
)
