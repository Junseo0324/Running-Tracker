package com.devhjs.runningtracker.data.repository

import com.devhjs.runningtracker.data.connectivity.BatteryMonitor
import com.devhjs.runningtracker.domain.model.BatteryStatus
import com.devhjs.runningtracker.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    private val batteryMonitor: BatteryMonitor
): BatteryRepository {
    override fun getBatteryStatus(): Flow<BatteryStatus> {
        return batteryMonitor.getBatteryStatusFlow()
    }
}
