package com.devhjs.runningtracker.data.mapper

import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.core.util.ImageUtils

fun RunEntity.toDomain(): Run {
    return Run(
        id = id,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
        img = img?.let { ImageUtils.bitmapToBytes(it) }
    )
}

fun Run.toEntity(): RunEntity {
    return RunEntity(
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
        img = img?.let { ImageUtils.bytesToBitmap(it) }
    ).also {
        it.id = id
    }
}
