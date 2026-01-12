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

/**
 * [LocationRepository]의 구현체입니다.
 * Google Play Services의 [FusedLocationProviderClient]를 사용하여 위치 정보를 가져옵니다.
 *
 * 이 클래스는 안드로이드 시스템의 위치 서비스를 사용하여 실제 디바이스의 위치 데이터를
 * [Flow] 형태로 변환하여 도메인 계층에 제공하는 역할을 합니다.
 */
class LocationRepositoryImpl @Inject constructor(
    private val client: FusedLocationProviderClient
): LocationRepository {

    /**
     * FusedLocationProviderClient를 사용하여 주기적으로 위치 업데이트를 받습니다.
     *
     * - [callbackFlow]를 사용하여 콜백 기반의 위치 API를 코루틴 Flow로 변환합니다.
     * - [LocationRequest] 설정:
     *   - Priority.PRIORITY_HIGH_ACCURACY: 러닝 트래커이므로 가장 높은 정확도를 요청합니다.
     *   - Interval: 1초(1000ms)마다 위치 업데이트를 요청합니다.
     *
     * @suppress [SuppressLint("MissingPermission")] 이 함수를 호출하기 전에
     * 상위 레벨(UI 또는 ViewModel)에서 반드시 위치 권한(ACCESS_FINE_LOCATION 등)을 확인해야 합니다.
     */
    @SuppressLint("MissingPermission")
    override fun getLocationFlow(): Flow<Location> = callbackFlow {
        // 위치 요청 설정: 높은 정확도, 1초 간격
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        // 위치 업데이트 콜백 정의
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                // 가장 최근 위치를 Flow로 전송 (trySend 사용)
                result.locations.lastOrNull()?.let { location ->
                    trySend(location)
                }
            }
        }

        // 1. 초기 위치 확보 시도: 마지막으로 알려진 위치가 있다면 즉시 방출하여 반응성 향상
        client.lastLocation.addOnSuccessListener { location ->
            location?.let { trySend(it) }
        }

        // 2. 주기적인 위치 업데이트 요청 시작
        client.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )

        // 3. Flow 수집이 종료(cancel)되면 위치 업데이트 요청을 중단하여 리소스 낭비 방지
        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}
