package com.devhjs.runningtracker.domain.repository

import com.devhjs.runningtracker.domain.model.BatteryStatus
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun getBatteryStatus(): Flow<BatteryStatus>
}
