package com.devhjs.runningtracker.data.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.devhjs.runningtracker.domain.model.BatteryStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getBatteryStatusFlow(): Flow<BatteryStatus> = callbackFlow {
        val batteryBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                    val percentage = if (level != -1 && scale != -1) {
                        (level * 100 / scale.toFloat()).toInt()
                    } else {
                        0 // Default or error value
                    }

                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    trySend(BatteryStatus(percentage, isCharging))
                }
            }
        }

        context.registerReceiver(
            batteryBroadcastReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        awaitClose {
            context.unregisterReceiver(batteryBroadcastReceiver)
        }
    }
}
