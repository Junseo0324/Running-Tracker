package com.devhjs.runningtracker.data.location

import android.location.Location
import com.devhjs.runningtracker.domain.location.LocationClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

/**
 * 테스트 및 개발을 위한 [LocationClient]의 Mock 구현체입니다.
 * 실제 GPS 신호 없이 시뮬레이션된 위치 데이터를 제공합니다.
 */
class MockLocationClient @Inject constructor() : LocationClient {
    
    override fun getLocationFlow(): Flow<Location> = flow {
        var lat = 37.5665
        var lng = 126.9780
        
        while (true) {
            val location = Location("mock_provider").apply {
                latitude = lat
                longitude = lng
                accuracy = 5f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = System.nanoTime()
            }
            
            emit(location)
            delay(1000L) // 1초 간격
            
            // 약간의 랜덤 이동 시뮬레이션 (약 몇 미터 정도)
            lat += (Random.nextDouble() - 0.5) * 0.0002
            lng += (Random.nextDouble() - 0.5) * 0.0002
        }
    }
}
