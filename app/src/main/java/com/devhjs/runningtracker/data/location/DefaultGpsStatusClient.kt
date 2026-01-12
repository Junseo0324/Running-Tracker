package com.devhjs.runningtracker.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.devhjs.runningtracker.domain.location.GpsStatusClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGpsStatusClient @Inject constructor(
    @ApplicationContext private val context: Context
) : GpsStatusClient {
    /**
     * GPS 활성화 상태를 Flow로 방출합니다.
     * callbackFlow를 사용하여 BroadcastReceiver의 수명 주기를 수집(Collection) 수명 주기에 맞춥니다.
     */
    override fun getGpsStatusFlow(): Flow<Boolean> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    trySend(isGpsEnabled)
                }
            }
        }

        // 초기 상태 방출
        trySend(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))

        // 리시버 등록
        context.registerReceiver(
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )

        // Flow 수집이 끝나면 리시버 해제
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}
