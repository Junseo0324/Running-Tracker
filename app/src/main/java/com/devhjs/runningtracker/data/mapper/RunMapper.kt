package com.devhjs.runningtracker.data.mapper

import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.domain.model.Run

fun RunEntity.toDomain(): Run {
    return Run(
        id = id,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
        img = img
    )
}

fun Run.toEntity(): RunEntity {
    return RunEntity(
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
        img = img
    ).also {
        it.id = id
    }
}
