package com.devhjs.runningtracker.data.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsStatusMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackingRepository: TrackingRepository
) {

    private val gpsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                checkGpsStatus()
            }
        }
    }

    fun startMonitoring() {
        context.registerReceiver(
            gpsBroadcastReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
        // Initial check
        checkGpsStatus()
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(gpsBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
            e.printStackTrace()
        }
    }

    private fun checkGpsStatus() {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        CoroutineScope(Dispatchers.Main).launch {
            trackingRepository.setGpsEnabled(isGpsEnabled)
        }
    }
}
