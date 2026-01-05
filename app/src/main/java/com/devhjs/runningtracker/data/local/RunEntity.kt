package com.devhjs.runningtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devhjs.runningtracker.domain.model.Run

@Entity(tableName = "running_table")
data class RunEntity(
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
    var img: android.graphics.Bitmap? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}


