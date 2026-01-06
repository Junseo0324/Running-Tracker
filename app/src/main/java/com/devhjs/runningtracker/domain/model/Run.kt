package com.devhjs.runningtracker.domain.model

import android.graphics.Bitmap

data class Run(
    val id: Int? = null,
    val timestamp: Long,
    val avgSpeedInKMH: Float,
    val distanceInMeters: Int,
    val timeInMillis: Long,
    val caloriesBurned: Int,
    val img: Bitmap? = null
)
