package com.devhjs.runningtracker.data.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.devhjs.runningtracker.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val client: FusedLocationProviderClient
): LocationRepository {

    @SuppressLint("MissingPermission")
    override fun getLocationFlow(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    trySend(location)
                }
            }
        }

        // 먼저 마지막 위치를 시도해보고 있으면 바로 방출
        client.lastLocation.addOnSuccessListener { location ->
            location?.let { trySend(it) }
        }

        client.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}
